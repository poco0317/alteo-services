package com.etterna.services.controller.modern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Alternative API for the rewrite of EO
 */
@RestController
@RequestMapping("v2")
public class ModernServicesApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ModernServicesApiController.class);
	
	@GetMapping("/test")
	public String test() {
		m_logger.info("Successful test");
		return "Success";
	}

}
