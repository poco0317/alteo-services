package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.dao.NoteInfoDao;
import com.etterna.calc.datamodel.NoteInfo;
import com.etterna.services.dao.SongCacheData.ChartCacheData;
import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartSkillsetValuesHistory;
import com.etterna.services.model.Pack;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RankingDao {
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private PackDao packs;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private DiffDao chartDiffs;
	
	@Autowired
	private NoteInfoDao noteInfoStorage;
	
	private static final long RANKING_QUEUE_TIMER = 1000L * 10L; // 10 secs
	
	private static Set<String> rankedChartkeys = ConcurrentHashMap.newKeySet();
	private static Map<String, List<String>> packRankQueue = new ConcurrentHashMap<>();
	private static Map<String, byte[]> noteinfoQueue = new ConcurrentHashMap<>();
	
	@Scheduled(fixedDelay = RANKING_QUEUE_TIMER)
	void handlePackRankQueue() {
		if (packRankQueue.size() == 0) {
			return;
		}
		m_logger.info("Handling pack ranking queue - {} packs and {} charts to handle", packRankQueue.size(), noteinfoQueue.size());
		
		Iterator<Entry<String, List<String>>> it = packRankQueue.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			m_logger.info("Ranking {}", entry.getKey());
			rankSongDatas(entry.getValue(), entry.getKey());
			it.remove();
		}
		
		noteinfoQueue.clear();
		
		m_logger.info("Finished handling pack ranking queue");
	}
	
	public void queuePackForRanking(List<String> songdatas, Map<String, byte[]> noteinfos, String packname) {
		if (packs.isRanked(packname)) {
			m_logger.info("Tried to rank pack {} after it was already ranked", packname);
			return;
		}
		
		int queueBefore = noteinfoQueue.size();
		noteinfoQueue.putAll(noteinfos);
		packRankQueue.put(packname, songdatas);
		m_logger.info("Added {} new charts to the NoteInfo Ranking Queue due to ranking {} which contains {} songs", noteinfoQueue.size() - queueBefore, packname, songdatas.size());
		
	}
	
	@Transactional
	public void updateRankedChartkeys() {
		int countBefore = rankedChartkeys.size();
		Set<String> allRankedChartkeys = charts.findChartKeyByChartKeyNotNull();
		if (allRankedChartkeys != null) {
			rankedChartkeys.addAll(allRankedChartkeys);
		}
		int countAfter = rankedChartkeys.size();
		
		if (countBefore != countAfter) {
			m_logger.info("Updated ranked chartkey list - before {} - after {} - difference {}", countBefore, countAfter, countAfter - countBefore);
		}
	}
	
	@Transactional
	public void updateMSDs() {
		m_logger.info("Starting Chart Difficulty Updates");
		List<Chart> all = charts.findByCalcVersionNotEqual(calc.getCalcVersion());
		updateRankedChartkeys();
		
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
							chartDiffs.stageUpdatedDiffValues((Chart)f.get()[0], (ChartSkillsetValuesHistory)f.get()[1], false);
						} catch (InterruptedException | ExecutionException e) {
							m_logger.error("Error finishing task " + e.getMessage(), e);
						} finally {
							it.remove();
						}
					}
				}
			}
			chartDiffs.flushStagedDiffValues();
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
		Pack pack = packs.getNewPackByName(packname);
		List<Chart> newCharts = new ArrayList<>();
		List<NoteInfo> newNoteInfo = new ArrayList<>();
		List<SongCacheData> songCacheDatas = songdatas.stream().map(content -> new SongCacheData(content)).collect(Collectors.toList());
		Set<String> chartkeys = new HashSet<>();
		songCacheDatas.forEach(data -> data.getCharts().forEach(c -> chartkeys.add(c.getChartkey())));
		Map<String, Chart> alreadyRankedCharts = charts.get(chartkeys);
		if (pack.getChartKeys() == null) {
			pack.setChartKeys(new ArrayList<>());
		}
		pack.getChartKeys().addAll(alreadyRankedCharts.values().stream().map(c -> c.getChartKey()).collect(Collectors.toList()));
		rankedChartkeys.addAll(chartkeys);
		
		for (SongCacheData cacheData : songCacheDatas) {
			
			if (cacheData.getTitle() == null) {
				m_logger.warn("Skipped song due to missing title in {}", packname);
				m_logger.warn("{}", cacheData.toString());
			} else {
				if (cacheData.getCharts().isEmpty()) {
					m_logger.warn("Chartkey and Diff count is not the same!!! Skipped ranking {}", cacheData.getTitle());
				} else {
					m_logger.info("Found {} charts in song {} - pack {}", cacheData.getCharts().size(), cacheData.getTitle(), packname);
					
					cacheData.getCharts().forEach(c -> {
						final String ck = c.getChartkey();
						if (!alreadyRankedCharts.containsKey(ck)) {
							newCharts.add(rankChart(pack, cacheData, c));
							NoteInfo ni = new NoteInfo();
							ni.setChartKey(c.getChartkey());
							ni.setNoteinfo(noteinfoQueue.get(c.getChartkey()));
							newNoteInfo.add(ni);
						}
					});
				}
			}
		}
		
		m_logger.info("Finished setting up pack {} for ranking ... {} new charts", packname, newCharts.size());
		m_logger.info(" Saving pack changes and new charts");
		packs.save(pack);
		charts.saveBulk(newCharts);
		noteInfoStorage.saveBulk(newNoteInfo);
	}

	public boolean isRanked(String chartkey) {
		return rankedChartkeys.contains(chartkey);
	}
	
	/**
	 * Returns a new chart that was created (never before ranked). Modifies the given Pack, which needs to also be saved.
	 */
	@Transactional
	public Chart rankChart(Pack pack, SongCacheData songCache, ChartCacheData chartCache) {
		m_logger.info("Ranking chart {} - {}", chartCache.getChartkey(), songCache.getTitle());
		Chart c = new Chart();
		c.setChartKey(chartCache.getChartkey());
		c.setDifficulty(chartCache.getDifficulty());
		//c.setCalcVersion(calc.getCalcVersion());
		c.setArtist(songCache.getArtist());
		c.setCredit(songCache.getCredit());
		c.setSubtitle(songCache.getSubtitle());
		c.setTitle(songCache.getTitle());
		c.setTranslitArtist(songCache.getTranslitArtist());
		c.setTranslitSubtitle(songCache.getTranslitSubtitle());
		c.setTranslitTitle(songCache.getTranslitTitle());
		c.setStepsType(chartCache.getStepstype());
		
		if (pack.getChartKeys() == null) {
			pack.setChartKeys(new ArrayList<>());
		}
		pack.getChartKeys().add(c.getChartKey());
		
		return c;
	}
	
}
