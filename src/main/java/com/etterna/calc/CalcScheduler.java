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
	
	private static final long TIME_BETWEEN = 1000L * 30L; // 30 secs
	private static final long START_DELAY = 1000L * 5L;
	
	@Value("${etterna.calc.scheduler.enabled:false}")
	private Boolean enabled;
	
	@PostConstruct
	void init() {
		m_logger.info("Started Calc Scheduler - scheduling {} - execution buffer {}ms", enabled, TIME_BETWEEN);
	}
	
	@Scheduled(fixedDelay = TIME_BETWEEN, initialDelay = START_DELAY)
	void maintain() {
		if (!enabled) return;
		if (packRanking.getPackQueueSize() > 0) {
			m_logger.info("Skipped MSD/SSR/User Ratings update because pack ranking is in progress");
			return;
		}
		packRanking.updateMSDs();
		scores.updateSSRs();
		users.maintainUserSkillsetRatings();
	}

}
