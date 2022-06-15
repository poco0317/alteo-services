package com.etterna.services.controller.legacy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.services.MiscService;
import com.etterna.services.PackService;
import com.etterna.services.ScoreService;
import com.etterna.services.SessionService;
import com.etterna.services.UserService;
import com.etterna.services.controller.legacy.dto.AddFavoriteRequest;
import com.etterna.services.controller.legacy.dto.AddGoalRequest;
import com.etterna.services.controller.legacy.dto.ChartLeaderboardDTO;
import com.etterna.services.controller.legacy.dto.CoreBundleDTO;
import com.etterna.services.controller.legacy.dto.CountryCodeDTO;
import com.etterna.services.controller.legacy.dto.GetClientVersionResponse;
import com.etterna.services.controller.legacy.dto.GetFavoritesResponse;
import com.etterna.services.controller.legacy.dto.GetScoreReplayResponse;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse;
import com.etterna.services.controller.legacy.dto.GetUserSkillsetRanksResponse;
import com.etterna.services.controller.legacy.dto.LoginRequest;
import com.etterna.services.controller.legacy.dto.LoginResponse;
import com.etterna.services.controller.legacy.dto.UpdateGoalRequest;
import com.etterna.services.controller.legacy.dto.UploadScoreRequest;
import com.etterna.services.controller.legacy.dto.UploadScoreResponse;

/**
 * API for the main version of EO
 */
@RestController
@RequestMapping("v1")
public class LegacyServicesApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(LegacyServicesApiController.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private ScoreService scoreService;
	
	@Autowired
	private PackService packService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private MiscService miscService;
	
	private String dummy() {
		return "{\"errors\":[{\"status\":404}]}";
	}
	
	@PostMapping("/user/{userName}/favorites")
	public void addFavorite(@PathVariable String userName, AddFavoriteRequest req) {
		userService.addFavorite();
	}
	
	@DeleteMapping("/user/{userName}/favorites/{chartkey}")
	public void removeFavorite(@PathVariable String userName, @PathVariable String chartkey) {
		userService.removeFavorite();
	}
	
	@GetMapping("/user/{userName}/favorites")
	public GetFavoritesResponse getFavorites(@PathVariable String userName) {
		userService.getFavorites();
		return null;
	}
	
	@DeleteMapping("/user/{userName}/goals/{chartkey}/{wifestr}/{ratestr}")
	public void removeGoal(@PathVariable String userName, @PathVariable String chartkey, @PathVariable String wifestr, @PathVariable String ratestr) {
		userService.removeGoal();
	}
	
	@PostMapping("/user/{userName}/goals")
	public void addGoal(@PathVariable String userName, AddGoalRequest req) {
		userService.addGoal();
	}
	
	@PostMapping("/user/{userName}/goals/update")
	public void updateGoal(@PathVariable String userName, UpdateGoalRequest req) {
		userService.updateGoal();
	}
	
	@PostMapping("/score")
	public UploadScoreResponse uploadScore(UploadScoreRequest req) {
		scoreService.intakeScore();
		return null;
	}
	
	@GetMapping("/user/{userName}/ranks")
	public GetUserSkillsetRanksResponse getUserSkillsetRanks(@PathVariable String userName) {
		userService.getUserSkillsetRanks();
		return null;
	}
	
	@GetMapping("/misc/countrycodes")
	public List<CountryCodeDTO> getCountryCodes() {
		miscService.getCountryCodes();
		return null;
	}
	
	@GetMapping("/replay/{userId}/{scoreId}")
	public GetScoreReplayResponse getScoreReplay(@PathVariable String userName, @PathVariable String scoreId) {
		scoreService.getScoreReplay();
		return null;
	}
	
	@GetMapping("/charts/{chartkey}/leaderboards")
	public List<ChartLeaderboardDTO> getChartLeaderboards(@PathVariable String chartkey) {
		scoreService.getChartLeaderboard();
		return null;
	}
	
	@GetMapping("/packs/collections")
	public List<CoreBundleDTO> getCoreBundles() {
		packService.getCoreBundles();
		return null;
	}
	
	@GetMapping("/client/version")
	public GetClientVersionResponse getClientVersion() {
		miscService.getClientVersion();
		return null;
	}
	
	@GetMapping("/user/{userName}/top/")
	public String getOverallTop25(@PathVariable String userName) {
		userService.getTop25();
		return dummy();
	}
	
	@GetMapping("/user/{userName}/top/{skillset}/{count}")
	public String getSkillsetTopX(@PathVariable String userName, @PathVariable String skillset, @PathVariable int count) {
		userService.getTop25();
		return dummy();
	}
	
	@GetMapping("/user/{userName}")
	public GetUserInfoResponse getUserInfo(@PathVariable String userName) {
		userService.getUserInfo();
		return null;
	}
	
	@PostMapping("/login")
	public LoginResponse startSession(LoginRequest req) {
		sessionService.login();
		return null;
	}

}
