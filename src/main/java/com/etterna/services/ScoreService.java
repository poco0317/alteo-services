package com.etterna.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO.LeaderboardScoreDTO;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO.LeaderboardScoreDTO.LeaderboardJudgmentsDTO;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO.LeaderboardScoreDTO.LeaderboardSkillsetDTO;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO.LeaderboardScoreDTO.LeaderboardUserDTO;
import com.etterna.services.controller.legacy.dto.UploadScoreRequest;
import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.RankingDao;
import com.etterna.services.model.Chart;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScoreService {
	
	@Autowired
	private HighScoreDao highScores;
	
	@Autowired
	private RankingDao chartRanking;
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private SessionService sessions;
	
	private ExecutorService bulkSsrExecutor = Executors.newWorkStealingPool();
	
	private static final boolean SAVE_OLD_SSRS = true;
	private static final long HS_METADATA_UPDATE_MILLIS = 1000L * 10L; // 10 secs
	private static final long START_DELAY = 1000L * 10L;
	
	public void updateSSRs() {
		List<HighScore> scores = highScores.getScoresToCalculate();
		if (scores.size() > 0) {
			m_logger.info("Updating queued scores: {} scores", scores.size());
			
			if (bulkSsrExecutor.isShutdown()) {
				bulkSsrExecutor = Executors.newWorkStealingPool();
			}
			
			if (SAVE_OLD_SSRS) {
				m_logger.info("Saving SSRs on old calc version...");
				highScores.saveHistoricScores(scores);
				m_logger.info("Saved {} SSRs on old calc versions", scores.size());
			}
			
			// chartkeys to scores
			ConcurrentHashMap<String, List<HighScore>> organizedScores = new ConcurrentHashMap<>();
			scores.forEach(hs -> {
				final String ck = hs.getChartKey();
				if (!organizedScores.containsKey(ck)) {
					organizedScores.put(ck, new ArrayList<>());
				}
				organizedScores.get(ck).add(hs);
			});
			
			List<Future<Map<HighScore, List<Float>>>> futures = new LinkedList<>();
			for (Entry<String, List<HighScore>> entry : organizedScores.entrySet()) {
				final String ck = entry.getKey();
				
				Future<Map<HighScore, List<Float>>> chartScoreSSRs = bulkSsrExecutor.submit(() -> {
					int sz = entry.getValue().size();
					if (sz > 10) {
						Map<HighScore, List<Float>> out = new HashMap<>();
						Map<Integer, List<HighScore>> byRate = new HashMap<>();
						entry.getValue().forEach(hs -> {
							Integer rate = hs.getMusicRate();
							if (!byRate.containsKey(rate)) {
								byRate.put(rate, new ArrayList<>());
							}
							byRate.get(rate).add(hs);
						});
						byRate.entrySet().forEach(rateEntry -> {
							float rate = rateEntry.getKey() / 100.F;
							List<HighScore> below93 = rateEntry.getValue().stream().filter(hs -> hs.getSsrNorm() < 930000).collect(Collectors.toList());
							if (!below93.isEmpty()) {
								out.putAll(calc.getSSRs(ck, below93));
							}
							List<Float> maxSSR = calc.getSSR(ck, rate, CalcManager.MAX_SSR_GOAL);
							List<Float> baseMSD = calc.getSSR(ck, rate, CalcManager.BASE_MSD_GOAL);
							Function<Integer, List<Float>> interpolate = intSsrnorm -> {
								float ssrnorm = intSsrnorm / 1000000.F;
								ssrnorm = Math.min(ssrnorm, CalcManager.MAX_SSR_GOAL);
								
								List<Float> o = new ArrayList<>();
								for (int i = 0; i < maxSSR.size(); i++) {
									float max = maxSSR.get(i);
									float min = baseMSD.get(i);
									float proportion = (ssrnorm - CalcManager.BASE_MSD_GOAL) / (CalcManager.MAX_SSR_GOAL - CalcManager.BASE_MSD_GOAL);
									o.add(proportion * (max - min) + min);
								}
								return o;
							};
							
							rateEntry.getValue().forEach(hs -> {
								if (hs.getSsrNorm() >= 930000) {
									out.put(hs, interpolate.apply(hs.getSsrNorm()));
								}
							});
						});
						return out;
					} else {
						return calc.getSSRs(ck, entry.getValue());
					}
				});
				futures.add(chartScoreSSRs);
			}
			
			m_logger.info("Waiting for {} tasks to complete...", futures.size());
			while (!futures.isEmpty()) {
				Iterator<Future<Map<HighScore, List<Float>>>> it = futures.iterator();
				while (it.hasNext()) {
					Future<Map<HighScore, List<Float>>> future = it.next();
					if (future.isCancelled()) {
						m_logger.warn("Found cancelled task, removed from queue");
						it.remove();
					} else if (future.isDone()) {
						try {
							Map<HighScore, List<Float>> results = future.get();
							m_logger.debug(" - Committing {} scores", results.size());
							for (Entry<HighScore, List<Float>> entry : results.entrySet()) {
								highScores.stageUpdatedSsrs(entry.getKey(), entry.getValue(), false);
							}
							m_logger.debug(" - Finished committing {} scores", results.size());
						} catch (InterruptedException | ExecutionException e) {
							m_logger.error("Error finishing task " + e.getMessage(), e);
						} finally {
							it.remove();
						}
					}
				}
			}
			highScores.flushStagedSsrs();
			
			m_logger.info("Finished updating queued scores");
		} else {
			if (!bulkSsrExecutor.isShutdown()) {
				m_logger.info("Shutting down BulkSSRExecutor to release unused resources");
				bulkSsrExecutor.shutdown();
			}
		}
	}
	
	@Scheduled(fixedDelay = HS_METADATA_UPDATE_MILLIS, initialDelay = START_DELAY)
	public void resolveHighScoreMetadata() {
		List<HighScore> scores = highScores.getScoresWithMissingChartMetadata();
		if (!scores.isEmpty()) {
			m_logger.info("Filling in missing metadata for {} scores", scores.size());
			
			Map<String, List<HighScore>> hsmap = new HashMap<>();
			scores.forEach(hs -> {
				final String ck = hs.getChartKey();
				if (!hsmap.containsKey(ck)) {
					hsmap.put(ck, new ArrayList<>());
				}
				hsmap.get(ck).add(hs);
			});
			
			Map<String, Chart> chartmap = charts.get(hsmap.keySet());
			m_logger.info("{} charts {} cks", chartmap.size(), hsmap.size());
			hsmap.entrySet().forEach(entry -> {
				final String ck = entry.getKey();
				Chart chart = chartmap.get(ck);
				if (chart != null) {
					final String title = chart.getTitle();
					final String artist = chart.getArtist();
					final String credit = chart.getCredit();
					entry.getValue().forEach(hs -> {
						hs.setSongTitle(title);
						hs.setSongArtist(artist);
						hs.setSongCredit(credit);
					});
				}
			});
			
			highScores.saveBulk(scores);
			
			m_logger.info("Finished filling in missing metadata for {} scores", scores.size());
		}
	}
	
	private void queue(UploadScoreRequest req, User user) {
		try {
			highScores.add(req, user);
		} catch (Exception e) {
			m_logger.warn("Threw exception when trying to queue score {} :: {}", req.getScorekey(), e.getMessage());
		}
	}
	
	/**
	 * Returns 404 if not ranked
	 * Returns 200 if okay
	 */
	public int intakeScore(UploadScoreRequest req, String auth) {
		String chartkey = req.getChartkey();
		
		User user = sessions.sessionToUser(auth);
		
		if (user != null && chartRanking.isRanked(chartkey)) {
			queue(req, user);
			return 200;
		} else {
			return 404;
		}
	}

	public void getScoreReplay() {
		// TODO Auto-generated method stub
		
	}

	public List<ChartLeaderboardDTO> getChartLeaderboard(String chartkey) {
		List<HighScore> scores = highScores.getLeaderboard(chartkey);
		
		return scores.stream().map(hs -> {
			ChartLeaderboardDTO dto = new ChartLeaderboardDTO();
			LeaderboardScoreDTO attr = new LeaderboardScoreDTO();
			LeaderboardJudgmentsDTO judgments = new LeaderboardJudgmentsDTO();
			judgments.setBad(hs.getBadCount());
			judgments.setGood(hs.getGoodCount());
			judgments.setGreat(hs.getGreatCount());
			judgments.setHeldHold(hs.getHeldCount());
			judgments.setHitMines(hs.getHitMineCount());
			judgments.setLetGoHold(hs.getNgCount());
			judgments.setMarvelous(hs.getMarvCount());
			judgments.setMiss(hs.getMissCount());
			judgments.setPerfect(hs.getPerfCount());
			LeaderboardSkillsetDTO ssrs = new LeaderboardSkillsetDTO();
			ssrs.setOverall(hs.getOverall().floatValue());
			ssrs.setStream(hs.getStream().floatValue());
			ssrs.setJumpstream(hs.getJumpstream().floatValue());
			ssrs.setHandstream(hs.getHandstream().floatValue());
			ssrs.setStamina(hs.getStamina().floatValue());
			ssrs.setJackSpeed(hs.getJackspeed().floatValue());
			ssrs.setChordjack(hs.getChordjack().floatValue());
			ssrs.setTechnical(hs.getTechnical().floatValue());
			LeaderboardUserDTO user = new LeaderboardUserDTO();
			User realUser = highScores.getUser(hs);
			user.setAvatar(null);
			user.setCountryCode(null);
			user.setPlayerRating(0.f);
			user.setUserId(1);
			user.setUserName("user");
			if (realUser != null) {
				user.setUserId(user.getUserId());
				user.setUserName(user.getUserName());
			}
			
			attr.setDatetime(hs.getDateStr());
			attr.setHasReplay(false);
			attr.setId("1"); // TODO
			attr.setJudgements(judgments);
			attr.setMaxCombo(hs.getMaxCombo());
			attr.setModifiers(hs.getModString());
			attr.setNoCC(hs.getNoCC());
			attr.setRate(hs.getMusicRate().floatValue() / 100.0f);
			attr.setSkillsets(ssrs);
			attr.setSongId("1"); // TODO
			attr.setUser(user);
			attr.setValid(hs.getEtternaValid() == 1);
			attr.setWife(hs.getWifePercent().floatValue());
			attr.setWifeVersion(hs.getWifeVersion());			
			
			dto.setAttributes(attr);
			return dto;
		}).collect(Collectors.toList());
	}

}
