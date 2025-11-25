package com.etterna.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.etterna.services.dao.EO2Dao;
import com.etterna.services.dao.RankingDao;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private RoleService roles;
	
	@Autowired
	private RankingDao rankedCharts;
	
	@Autowired
	private EO2Dao eo2;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		m_logger.info("Beginning Application Init");
		
		roles.maintainRoles();
		rankedCharts.updateRankedChartkeys();
		eo2.refreshdata();
		
		m_logger.info("Finished Application Init");
	}

}
