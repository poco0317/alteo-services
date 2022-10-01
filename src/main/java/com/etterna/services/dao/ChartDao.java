package com.etterna.services.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.datamodel.RankedChartkey;
import com.etterna.services.repo.ChartRepository;
import com.etterna.site.dto.PackNameWithChartCount;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;

@Service
public class ChartDao {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ChartDao.class);
	
	@Autowired
	private ChartRepository repo;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private DiffService chartDiffs;
	
	private static Set<String> rankedChartkeys = ConcurrentHashMap.newKeySet();
	private static ConcurrentHashMap<String, List<String>> packRankQueue = new ConcurrentHashMap<>();
	
	@Scheduled(fixedDelay = 1000L * 10L)
	void handlePackRankQueue() {
		if (packRankQueue.size() == 0) {
			return;
		}
		m_logger.info("Handling pack ranking queue - {} to do", packRankQueue.size());
		
		Iterator<Entry<String, List<String>>> it = packRankQueue.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			m_logger.info("Ranking {}", entry.getKey());
			rankSongDatas(entry.getValue(), entry.getKey());
			it.remove();
		}
		
		m_logger.info("Finished handling pack ranking queue");
	}
	
	public void queuePackForRanking(List<String> songdatas, String packname) {
		if (packRankQueue.containsKey(packname) || repo.findByPackName(packname).size() > 0) {
			m_logger.warn("Attempted to rank pack already in queue or already ranked : {}", packname);
		} else {
			packRankQueue.put(packname, songdatas);
			m_logger.info("Queued pack for ranking: {}", packname);
		}
	}
	
	public int getPackQueueSize() {
		return packRankQueue.size();
	}
	
	public int getTotalRankedCharts() {
		return rankedChartkeys.size();
	}
	
	private void rankSongDatas(List<String> songdatas, String packname) {
		final Pattern titlepattern = Pattern.compile(";[\\s]*#TITLE:([^;]+);");
		final Pattern ckpattern = Pattern.compile(";[\\s]*#CHARTKEY:([^;]+);");
		final Pattern diffpattern = Pattern.compile(";[\\s]*#DIFFICULTY:([^;]+);");
		for (String contents : songdatas) {
			Matcher titlematch = titlepattern.matcher(contents);
			Matcher ckmatcher = ckpattern.matcher(contents);
			Matcher diffmatcher = diffpattern.matcher(contents);
			
			if (!titlematch.find()) {
				m_logger.warn("Skipped song due to missing title in {}", packname);
				m_logger.warn("{}", contents);
			} else {
				String songname = titlematch.group(1);
				List<String> cks = new LinkedList<>();
				while (ckmatcher.find()) {
					cks.add(ckmatcher.group(1));
				}
				List<String> diffs = new LinkedList<>();
				while (diffmatcher.find()) {
					diffs.add(diffmatcher.group(1));
				}
				if (diffs.size() != cks.size()) {
					m_logger.warn("Chartkey and Diff count is not the same!!! ck {} - diff {} - Skipped ranking {}", cks.size(), diffs.size(), songname);
				} else {
					m_logger.info("Found {} cks and {} diffs in song {} - pack {}", cks.size(), diffs.size(), songname, packname);
					for (int i = 0; i < cks.size(); i++) {
						rankChart(cks.get(i), diffs.get(i), packname, songname);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public void init() {
		m_logger.info("Starting Chart Difficulty Updates");
		List<Chart> all = repo.findByCalcVersionLessThan(calc.getCalcVersion());
		List<RankedChartkey> allRankedChartkeys = repo.findChartKeyByChartKeyNotNull();
		if (allRankedChartkeys != null) {
			rankedChartkeys.addAll(allRankedChartkeys.stream().map(c -> c.getChartKey()).collect(Collectors.toSet()));
		}
		
		if (all != null) {
			m_logger.info("Found {} charts to update out of {} ranked charts.", all.size(), repo.count());
			
			ExecutorService bulkCalc = Executors.newWorkStealingPool();
			// Object[] is [Chart, Set<ChartDiffValue>]
			List<Future<Object[]>> futures = new LinkedList<>();
			all.forEach(c -> {
				futures.add(bulkCalc.submit(() -> {
					return new Object[] {c, calc.calcDiffValues(c, 1.f,.93f)};
				}));
			});
			
			while (!futures.isEmpty()) {
				Iterator<Future<Object[]>> it = futures.iterator();
				while (it.hasNext()) {
					Future<Object[]> f = it.next();
					if (f.isCancelled()) {
						m_logger.warn("Found cancelled task, removed from queue");
						it.remove();
					} else if (f.isDone()) {
						try {
							chartDiffs.updateDiffValues((Chart)f.get()[0], (Set<ChartDiffValue>)f.get()[1]);
						} catch (InterruptedException | ExecutionException e) {
							m_logger.error("Error finishing task " + e.getMessage(), e);
						} finally {
							it.remove();
						}
					}
				}
			}
		} else {
			m_logger.info("Found no charts to update and no ranked charts.");
		}
		m_logger.info("Finished Chart Difficulty Updates");
	}

	@Transactional
	public Chart get(String chartkey) {
		return repo.findById(chartkey).orElse(null);
	}
	
	public boolean ranked(String chartkey) {
		return rankedChartkeys.contains(chartkey);
	}
	
	@Transactional
	public boolean rankChart(String chartkey, String diffname, String packname, String songname) {
		if (ranked(chartkey))
			return false;
		
		m_logger.info("Ranking chart {}", chartkey);
		Chart c = new Chart();
		c.setChartKey(chartkey);
		c.setDifficulty(diffname);
		c.setPackName(packname);
		c.setSongName(songname);
		c.setCalcVersion(calc.getCalcVersion());
		repo.save(c);
		rankedChartkeys.add(chartkey);
		
		Set<ChartDiffValue> diffs = calc.calcDiffValues(c, 1.f, .93f);
		chartDiffs.commitDiffs(c, diffs);
		c.setDiffValues(diffs);
		
		return true;
	}
	
	@Transactional
	public List<String> getAllPacks() {
		List<String> packs = repo.findDistinctPackName();
		Collections.sort(packs, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		return packs;
	}
	
	@Transactional
	public PackNameWithChartCountPagination getPacksAndChartCounts(PacksSort ps, int page, int itemsPerPage) {
		List<PackNameWithChartCount> pncc = repo.getPackNamesWithChartCounts();
		
		int sliceStart = Math.min(itemsPerPage * (page-1), pncc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, pncc.size());
		m_logger.debug("{} {} {}", sliceStart, sliceEnd, pncc.size());
		
		if (pncc.size() == 0) {
			return new PackNameWithChartCountPagination(pncc, 1, 1);
		}
		
		Collections.sort(pncc, new Comparator<PackNameWithChartCount>() {
			@Override
			public int compare(PackNameWithChartCount a, PackNameWithChartCount b) {
				switch (ps) {
					case COUNT:
						return b.getCount().compareTo(a.getCount());
					case NAME:
					default:
						return a.getPack().compareToIgnoreCase(b.getPack());
				}
			}
		});
		return new PackNameWithChartCountPagination(pncc.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(pncc.size() / (float)itemsPerPage)));
	}
	
	@Transactional
	public List<Chart> getChartsInPack(String pack) {
		List<Chart> charts = repo.findByPackName(pack);
		
		Collections.sort(charts, new Comparator<Chart>() {
			@Override
			public int compare(Chart c1, Chart c2) {
				int songname = c1.getSongName().compareToIgnoreCase(c2.getSongName());
				if (songname == 0) {
					int diff = c1.getDifficulty().compareToIgnoreCase(c2.getDifficulty());
					return diff;
				} else {
					return songname;
				}
			}
		});
		
		return charts;
	}

}
