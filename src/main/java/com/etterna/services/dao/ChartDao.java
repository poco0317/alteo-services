package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.dao.SongCacheData.ChartCacheData;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.datamodel.RankedChartkey;
import com.etterna.services.repo.ChartRepository;
import com.etterna.site.dto.ChartWithCount;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.ChartsInPackPagination;
import com.etterna.site.dto.PackContentSort;
import com.etterna.site.dto.PackNameWithChartCount;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;

@Service
public class ChartDao {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ChartDao.class);
	
	@Autowired
	private ChartRepository charts;
	
	@Autowired
	private PackDao packs;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private DiffDao chartDiffs;
	
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
		init();
		
		m_logger.info("Finished handling pack ranking queue");
	}
	
	public void queuePackForRanking(List<String> songdatas, String packname) {
		if (packRankQueue.containsKey(packname) || packs.isRanked(packname)) {
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
		for (String contents : songdatas) {
			SongCacheData cacheData = new SongCacheData(contents);
			
			if (cacheData.getTitle() == null) {
				m_logger.warn("Skipped song due to missing title in {}", packname);
				m_logger.warn("{}", contents);
			} else {
				if (cacheData.getCharts().isEmpty()) {
					m_logger.warn("Chartkey and Diff count is not the same!!! Skipped ranking {}", cacheData.getTitle());
				} else {
					m_logger.info("Found {} charts in song {} - pack {}", cacheData.getCharts().size(), cacheData.getTitle(), packname);
					cacheData.getCharts().forEach(c -> {
						rankChart(packname, cacheData, c);
					});
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public void init() {
		m_logger.info("Starting Chart Difficulty Updates");
		List<Chart> all = charts.findByCalcVersionLessThan(calc.getCalcVersion());
		List<RankedChartkey> allRankedChartkeys = charts.findChartKeyByChartKeyNotNull();
		if (allRankedChartkeys != null) {
			rankedChartkeys.addAll(allRankedChartkeys.stream().map(c -> c.getChartKey()).collect(Collectors.toSet()));
		}
		
		if (all != null) {
			m_logger.info("Found {} charts to update out of {} ranked charts.", all.size(), charts.count());
			
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
			bulkCalc.shutdown();
		} else {
			m_logger.info("Found no charts to update and no ranked charts.");
		}
		m_logger.info("Finished Chart Difficulty Updates");
	}

	@Transactional
	public Chart get(String chartkey) {
		return charts.findById(chartkey).orElse(null);
	}
	
	public boolean isRanked(String chartkey) {
		return rankedChartkeys.contains(chartkey);
	}
	
	@Transactional
	public boolean rankChart(String packname, SongCacheData songCache, ChartCacheData chartCache) {
		Pack pack = packs.getNewPackByName(packname, true);
		m_logger.info("Ranking chart {} - {}", chartCache.getChartkey(), songCache.getTitle());
		Chart c = get(chartCache.getChartkey());
		if (c == null) {
			c = new Chart();
			c.setChartKey(chartCache.getChartkey());
			c.setDifficulty(chartCache.getDifficulty());
			//c.setCalcVersion(calc.getCalcVersion());
			c.setPacks(new HashSet<>());
			c.setArtist(songCache.getArtist());
			c.setCredit(songCache.getCredit());
			c.setSubtitle(songCache.getSubtitle());
			c.setTitle(songCache.getTitle());
			c.setTranslitArtist(songCache.getTranslitArtist());
			c.setTranslitSubtitle(songCache.getTranslitSubtitle());
			c.setTranslitTitle(songCache.getTranslitTitle());
		}
		c.getPacks().add(pack);
		pack.getCharts().add(c);
		
		charts.save(c);
		rankedChartkeys.add(chartCache.getChartkey());
		
		/*
		Set<ChartDiffValue> diffs = calc.calcDiffValues(c, 1.f, .93f);
		chartDiffs.commitDiffs(c, diffs);
		c.setDiffValues(diffs);
		*/
		
		return true;
	}
	
	@Transactional
	public PackNameWithChartCountPagination getPacksAndChartCounts(PacksSort ps, int page, int itemsPerPage) {
		List<PackNameWithChartCount> pncc = charts.getPackNamesWithChartCounts();
		
		int sliceStart = Math.min(itemsPerPage * (page-1), pncc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, pncc.size());
		m_logger.debug("pncc {} {} {}", sliceStart, sliceEnd, pncc.size());
		
		if (pncc.size() == 0) {
			return new PackNameWithChartCountPagination(pncc, 1, 1);
		}
		
		Map<String, Integer> packScoreCounts = charts.getPackNamesWithScoreCountsMap();
		pncc.forEach(c -> {
			c.setScoreCount(packScoreCounts.getOrDefault(c.getPack(), 0));
		});
		
		Collections.sort(pncc, new Comparator<PackNameWithChartCount>() {
			@Override
			public int compare(PackNameWithChartCount a, PackNameWithChartCount b) {
				switch (ps) {
					case COUNT:
					case AVG:
					case SCORES:
					{
						int o = 0;
						switch (ps) {
							default:
							case COUNT:
								o = b.getCount().compareTo(a.getCount());
								break;
							case SCORES:
								o = b.getScoreCount().compareTo(a.getScoreCount());
								break;
							case AVG:
								o = b.getAverageScores().compareTo(a.getAverageScores());
								break;
						}
						if (o != 0) {
							return o;
						}
						// fallthrough to compare by name if equal counts
					}
					case NAME:
					default:
						return a.getPack().compareToIgnoreCase(b.getPack());
				}
			}
		});
		return new PackNameWithChartCountPagination(pncc.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(pncc.size() / (float)itemsPerPage)));
	}
	
	@Transactional
	public List<Chart> getChartsInPack(String packName) {
		Pack pack = packs.get(packName);
		if (pack == null) {
			return new ArrayList<>();
		}
		
		List<Chart> chartList = packs.orderedChartList(pack);
		
		return chartList;
	}
	
	@Transactional
	public ChartsInPackPagination getChartsInPackPagination(String pack, PackContentSort ps, int page, int itemsPerPage) {
		Set<String> chartkeys = charts.getChartKeysInPack(packs.get(pack));
		List<ChartWithCount> cwc = charts.getChartsAndScoreCounts(chartkeys);
		cwc.addAll(charts.getChartsWithNoScores(chartkeys));
		
		int sliceStart = Math.min(itemsPerPage * (page-1), cwc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, cwc.size());
		m_logger.debug("cwc {} {} {}", sliceStart, sliceEnd, cwc.size());
		
		if (cwc.size() == 0) {
			return new ChartsInPackPagination(new ArrayList<>(), 1, 1);
		}
		
		List<ChartWithSkillsets> cip = cwc.stream().map(c -> new ChartWithSkillsets(c.getChart(), c.getCount().intValue())).collect(Collectors.toList());
		
		Collections.sort(cip, new Comparator<ChartWithSkillsets>() {
			@Override
			public int compare(ChartWithSkillsets a, ChartWithSkillsets b) {
				int o = 0;
				switch (ps) {
					case SCORES:
						o = b.getScoreCount().compareTo(a.getScoreCount());
						break;
					case OVERALL:
						o = b.getOverall().compareTo(a.getOverall());
						break;
					case STREAM:
						o = b.getStream().compareTo(a.getStream());
						break;
					case JUMPSTREAM:
						o = b.getJumpstream().compareTo(a.getJumpstream());
						break;
					case HANDSTREAM:
						o = b.getHandstream().compareTo(a.getHandstream());
						break;
					case STAMINA:
						o = b.getStamina().compareTo(a.getStamina());
						break;
					case JACKSPEED:
						o = b.getJackspeed().compareTo(a.getJackspeed());
						break;
					case CHORDJACK:
						o = b.getChordjack().compareTo(a.getChordjack());
						break;
					case TECHNICAL:
						o = b.getTechnical().compareTo(a.getTechnical());
						break;
					case NAME:
					default: {
						int oo = a.getChart().getTitle().compareToIgnoreCase(b.getChart().getTitle());
						if (oo == 0) {
							return a.getChart().getDifficulty().compareToIgnoreCase(b.getChart().getDifficulty());
						} else {
							return oo;
						}
					}
				}
				if (o == 0) {
					int oo = a.getChart().getTitle().compareToIgnoreCase(b.getChart().getTitle());
					if (oo == 0) {
						return a.getChart().getDifficulty().compareToIgnoreCase(b.getChart().getDifficulty());
					} else {
						return oo;
					}
				} else {
					return o;
				}
			}
		});
		
		return new ChartsInPackPagination(cip.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(cip.size() / (float)itemsPerPage)));
	}

}
