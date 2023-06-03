package com.etterna.calc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.services.ScoreService;
import com.etterna.services.dao.RankingDao;
import com.etterna.services.dao.UserDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalcScheduler {
	
	@Autowired
	private RankingDao packRanking;
	
	@Autowired
	private UserDao users;
	
	@Autowired
	private ScoreService scores;
	
	private static final long USER_SKILLSET_TIMER = 1000L * 30L; // 30 secs
	private static final long SCORE_SSRS_TIMER = 1000L * 30L; // 30 secs
	private static final long CHART_MSD_TIMER = 1000L * 30L; // 30 secs
	
	@Value("${etterna.calc.scheduler.enabled:false}")
	private Boolean enabled;
	
	@PostConstruct
	void init() {
		m_logger.info("Started Calc Scheduler - scheduling {}", enabled);
	}
	
	@Scheduled(fixedDelay = USER_SKILLSET_TIMER)
	void maintainUserSkillsetRatings() {
		if (!enabled) return;
		users.maintainUserSkillsetRatings();
	}
	
	@Scheduled(fixedDelay = SCORE_SSRS_TIMER)
	void updateSSRs() {
		if (!enabled) return;
		scores.updateSSRs();
	}
	
	@Scheduled(fixedDelay = CHART_MSD_TIMER)
	void updateMSDs() {
		if (!enabled) return;
		packRanking.updateMSDs();
	}
	
	

}
