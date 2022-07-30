package com.etterna.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;

@Service
public class ScoreService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ScoreService.class);

	@Autowired
	private HighScoreDao highScores;
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private SessionService sessions;
	
	@Scheduled(fixedDelay = 30L * 1000L)
	private void updateSSRs() {
		List<HighScore> scores = highScores.getScoresToCalculate();
		if (scores.size() > 0) {
			m_logger.info("Eating queued scores: {} scores", scores.size());
			
			scores.forEach(hs -> {
				try {
					float rate = hs.getMusicRate() / 100.F;
					float goal = hs.getSsrNorm() / 1000000.F;
					
					List<Float> ssrs = calc.getSSR(hs.getChart().getChartKey(), rate, goal);
					if (ssrs.size() > 0) {
						highScores.updateSsrs(hs, ssrs);
					}
				} catch (Exception e) {
					m_logger.error(e.getMessage(), e);
				}
			});
			
			m_logger.info("Ate queued scores");
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
		
		if (user != null && charts.ranked(chartkey)) {
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
