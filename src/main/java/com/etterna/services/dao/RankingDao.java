package com.etterna.services.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
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
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.dao.SongCacheData.ChartCacheData;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.datamodel.RankedChartkey;

@Service
public class RankingDao {
	
	private static final Logger m_logger = LoggerFactory.getLogger(RankingDao.class);

	@Autowired
	private ChartDao charts;
	
	@Autowired
	private PackDao packs;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private DiffDao chartDiffs;
	
	private static Set<String> rankedChartkeys = ConcurrentHashMap.newKeySet();
	private static ConcurrentHashMap<String, List<String>> packRankQueue = new ConcurrentHashMap<>();
	
	public void handlePackRankQueue() {
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
	
	@SuppressWarnings("unchecked")
	@Transactional
	public void init() {
		m_logger.info("Starting Chart Difficulty Updates");
		List<Chart> all = charts.findByCalcVersionNotEqual(calc.getCalcVersion());
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

	public boolean isRanked(String chartkey) {
		return rankedChartkeys.contains(chartkey);
	}
	
	@Transactional
	public boolean rankChart(String packname, SongCacheData songCache, ChartCacheData chartCache) {
		Pack pack = packs.getNewPackByName(packname, true);
		m_logger.info("Ranking chart {} - {}", chartCache.getChartkey(), songCache.getTitle());
		Chart c = charts.get(chartCache.getChartkey(), true);
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
			c.setStepsType(chartCache.getStepstype());
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
	
}
