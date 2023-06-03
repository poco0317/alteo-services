package com.etterna.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private RoleService roles;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		m_logger.info("Beginning Application Init");
		
		roles.maintainRoles();
		
		m_logger.info("Finished Application Init");
	}

}
