package com.etterna.services.controller.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API for the main version of EO
 */
@RestController
@RequestMapping("v1")
public class LegacyServicesApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(LegacyServicesApiController.class);
	
	@GetMapping("/test")
	public String test() {
		m_logger.info("Successful test");
		return "Success";
	}
	
	
	@PostMapping("/user/{userName}/favorites")
	public void addFavorite(@PathVariable String userName) {
		
	}
	
	@DeleteMapping("/user/{userName}/favorites/{chartkey}")
	public void removeFavorite(@PathVariable String userName, @PathVariable String chartkey) {
		
	}
	
	@GetMapping("/user/{userName}/favorites")
	public void getFavorites(@PathVariable String userName) {
		
	}
	
	@DeleteMapping("/user/{userName}/goals/{chartkey}/{wifestr}/{ratestr}")
	public void removeGoal(@PathVariable String userName, @PathVariable String chartkey, @PathVariable String wifestr, @PathVariable String ratestr) {
		
	}
	
	@PostMapping("/user/{userName}/goals")
	public void addGoal(@PathVariable String userName) {
		
	}
	
	@PostMapping("/user/{userName}/goals/update")
	public void updateGoal(@PathVariable String userName) {
		
	}
	
	@PostMapping("/score")
	public void uploadScore() {
		
	}
	
	@GetMapping("/user/{userName}/ranks")
	public void getUserSkillsetRanks(@PathVariable String userName) {
		
	}
	
	@GetMapping("/misc/countrycodes")
	public void getCountryCodes() {
		
	}
	
	@GetMapping("/replay/{userId}/{scoreId}")
	public void getScoreReplay(@PathVariable String userName, @PathVariable String scoreId) {
		
	}
	
	@GetMapping("/charts/{chartkey}/leaderboards")
	public void getChartLeaderboards(@PathVariable String chartkey) {
		
	}
	
	@GetMapping("/packs/collections")
	public void getCoreBundles() {
		
	}
	
	@GetMapping("/client/version")
	public void getClientVersion() {
		
	}
	
	@GetMapping("/user/{userName}/top/")
	public void getOverallTop25(@PathVariable String userName) {
		
	}
	
	@GetMapping("/user/{userName}/top/{skillset}/{count}")
	public void getSkillsetTopX(@PathVariable String userName, @PathVariable String skillset, @PathVariable int count) {
		
	}
	
	@GetMapping("/user/{userName}")
	public void getUserInfo(@PathVariable String userName) {
		
	}
	
	@PostMapping("/login")
	public void startSession() {
		
	}

}
