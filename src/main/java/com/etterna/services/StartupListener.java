package com.etterna.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.etterna.services.dao.RankingDao;

@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger m_logger = LoggerFactory.getLogger(StartupListener.class);
	
	@Autowired
	private RankingDao chartRanking;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		m_logger.info("Beginning Application Init");
		
		chartRanking.init();
		
		m_logger.info("Finished Application Init");
	}

}
