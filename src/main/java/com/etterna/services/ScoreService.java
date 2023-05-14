package com.etterna.services;

import java.util.ArrayList;
import java.util.HashMap;
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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.RankingDao;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;

@Service
public class ScoreService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ScoreService.class);

	@Autowired
	private HighScoreDao highScores;
	
	@Autowired
	private RankingDao chartRanking;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private SessionService sessions;
	
	private ExecutorService bulkSsrExecutor = Executors.newWorkStealingPool();
	
	private static final boolean DELETE_OLD_SSRS = false;
	
	@Scheduled(fixedDelay = 30L * 1000L)
	private void updateSSRs() {
		List<HighScore> scores = highScores.getScoresToCalculate();
		if (scores.size() > 0) {
			m_logger.info("Updating queued scores: {} scores", scores.size());
			
			if (bulkSsrExecutor.isShutdown()) {
				bulkSsrExecutor = Executors.newWorkStealingPool();
			}
			
			if (DELETE_OLD_SSRS) {
				m_logger.info("Deleting SSRs on old calc version...");
				long deleted = highScores.deleteSsrsOlderThan(calc.getCalcVersion());
				m_logger.info("Deleted {} SSRs on old calc versions", deleted);
			}
			
			// chartkeys to scores
			ConcurrentHashMap<String, List<HighScore>> organizedScores = new ConcurrentHashMap<>();
			scores.forEach(hs -> {
				final String ck = hs.getChart().getChartKey();
				if (!organizedScores.containsKey(ck)) {
					organizedScores.put(ck, new ArrayList<>());
				}
				organizedScores.get(ck).add(hs);
			});
			
			List<Future<HashMap<HighScore, List<Float>>>> futures = new LinkedList<>();
			for (Entry<String, List<HighScore>> entry : organizedScores.entrySet()) {
				final String ck = entry.getKey();
				
				Future<HashMap<HighScore, List<Float>>> chartScoreSSRs = bulkSsrExecutor.submit(() -> {
					HashMap<HighScore, List<Float>> scoreSsrs = new HashMap<>();
					entry.getValue().forEach(hs -> {
						float rate = hs.getMusicRate() / 100.F;
						float goal = hs.getSsrNorm() / 1000000.F;
						List<Float> ssrs = calc.getSSR(ck, rate, goal);
						if (ssrs.size() > 0) {
							scoreSsrs.put(hs, ssrs);
						}
					});
					return scoreSsrs;
				});
				futures.add(chartScoreSSRs);
			}
			
			m_logger.info("Waiting for {} tasks to complete...", futures.size());
			while (!futures.isEmpty()) {
				Iterator<Future<HashMap<HighScore, List<Float>>>> it = futures.iterator();
				while (it.hasNext()) {
					Future<HashMap<HighScore, List<Float>>> future = it.next();
					if (future.isCancelled()) {
						m_logger.warn("Found cancelled task, removed from queue");
						it.remove();
					} else if (future.isDone()) {
						try {
							HashMap<HighScore, List<Float>> results = future.get();
							m_logger.debug(" - Committing {} scores", results.size());
							for (Entry<HighScore, List<Float>> entry : results.entrySet()) {
								highScores.updateSsrs(entry.getKey(), entry.getValue());
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
			
			m_logger.info("Finished updating queued scores");
		} else {
			if (!bulkSsrExecutor.isShutdown()) {
				m_logger.info("Shutting down BulkSSRExecutor to release unused resources");
				bulkSsrExecutor.shutdown();
			}
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
			Set<ScoreSpecificValue> hsssrs = hs.getSsrs();
			if (hsssrs != null) {
				hsssrs.forEach(ssr -> {
					switch (ssr.getId().getSkillset()) {
						case OVERALL:
							ssrs.setOverall(ssr.getValue().floatValue());
							break;
						case STREAM:
							ssrs.setStream(ssr.getValue().floatValue());
							break;
						case JUMPSTREAM:
							ssrs.setJumpstream(ssr.getValue().floatValue());
							break;
						case HANDSTREAM:
							ssrs.setHandstream(ssr.getValue().floatValue());
							break;
						case STAMINA:
							ssrs.setStamina(ssr.getValue().floatValue());
							break;
						case JACKSPEED:
							ssrs.setJackSpeed(ssr.getValue().floatValue());
							break;
						case CHORDJACK:
							ssrs.setChordjack(ssr.getValue().floatValue());
							break;
						case TECHNICAL:
							ssrs.setTechnical(ssr.getValue().floatValue());
							break;
						default: break;
					}
				});
			}
			LeaderboardUserDTO user = new LeaderboardUserDTO();
			User realUser = hs.getUser();
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
