package com.etterna.services.controller.legacy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.etterna.services.controller.legacy.dto.GetUserSkillsetRanksResponse.Ranks;
import com.etterna.services.controller.legacy.dto.LoginRequest;
import com.etterna.services.controller.legacy.dto.LoginResponse;
import com.etterna.services.controller.legacy.dto.ResponseData;
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
	
	private static final String GAME_VERSION = "0.71.2";
	
	private String dummy() {
		return "{\"errors\":[{\"status\":404}]}";
	}
	
	private String auth(String bearer) {
		return bearer.replace("Bearer ","");
	}
	
	@PostMapping("/user/{userName}/favorites")
	public void addFavorite(@PathVariable String userName, AddFavoriteRequest req) {
		m_logger.info("API CALLED :: AddFavorite");
		userService.addFavorite();
	}
	
	@DeleteMapping("/user/{userName}/favorites/{chartkey}")
	public void removeFavorite(@PathVariable String userName, @PathVariable String chartkey) {
		m_logger.info("API CALLED :: RemoveFavorite");
		userService.removeFavorite();
	}
	
	@GetMapping("/user/{userName}/favorites")
	public ResponseData<GetFavoritesResponse> getFavorites(@PathVariable String userName) {
		m_logger.info("API CALLED :: GetFavorites");
		userService.getFavorites();
		GetFavoritesResponse dto = new GetFavoritesResponse();
		dto.setAttributes(new ArrayList<>());
		return ResponseData.ok(dto);
	}
	
	@DeleteMapping("/user/{userName}/goals/{chartkey}/{wifestr}/{ratestr}")
	public void removeGoal(@PathVariable String userName, @PathVariable String chartkey, @PathVariable String wifestr, @PathVariable String ratestr) {
		m_logger.info("API CALLED :: RemoveGoal");
		userService.removeGoal();
	}
	
	@PostMapping("/user/{userName}/goals")
	public void addGoal(@PathVariable String userName, AddGoalRequest req) {
		m_logger.info("API CALLED :: AddGoal");
		userService.addGoal();
	}
	
	@PostMapping("/user/{userName}/goals/update")
	public void updateGoal(@PathVariable String userName, UpdateGoalRequest req) {
		m_logger.info("API CALLED :: UpdateGoal");
		userService.updateGoal();
	}
	
	@PostMapping("/score")
	public ResponseEntity<ResponseData<UploadScoreResponse>> uploadScore(@RequestHeader("Authorization") String authJwt, UploadScoreRequest req) {
		m_logger.info("API CALLED :: UploadScore");
		int status = scoreService.intakeScore(req, auth(authJwt));
		if (status == HttpStatus.OK.value()) {
			UploadScoreResponse r = new UploadScoreResponse();
			r.setType("ssrResults");
			r.setAttributes(UploadScoreResponse.dummyDTO());
			return ResponseEntity.ok(ResponseData.ok(r));
		} else {
			ResponseData<UploadScoreResponse> r = new ResponseData<>();
			r.error(status);
			return ResponseEntity.status(404).body(r);
		}
	}
	
	@GetMapping("/user/{userName}/ranks")
	public ResponseData<GetUserSkillsetRanksResponse> getUserSkillsetRanks(@PathVariable String userName) {
		m_logger.info("API CALLED :: GetUserSkillsetRanks");
		userService.getUserSkillsetRanks();
		GetUserSkillsetRanksResponse dto = new GetUserSkillsetRanksResponse();
		Ranks attr = new Ranks();
		attr.setChordjack(1);
		attr.setHandstream(1);
		attr.setJackSpeed(1);
		attr.setJumpstream(1);
		attr.setOverall(1);
		attr.setStamina(1);
		attr.setStream(1);
		attr.setTechnical(1);
		dto.setAttributes(attr);
		return ResponseData.ok(dto);
	}
	
	@GetMapping("/misc/countrycodes")
	public ResponseData<List<CountryCodeDTO>> getCountryCodes() {
		m_logger.info("API CALLED :: GetCountryCodes");
		miscService.getCountryCodes();
		return ResponseData.ok(new ArrayList<>());
	}
	
	@GetMapping("/replay/{userId}/{scoreId}")
	public ResponseData<GetScoreReplayResponse> getScoreReplay(@PathVariable String userName, @PathVariable String scoreId) {
		m_logger.info("API CALLED :: GetScoreReplay");
		scoreService.getScoreReplay();
		ResponseData<GetScoreReplayResponse> r = new ResponseData<>();
		r.error(404);
		return r;
	}
	
	@GetMapping("/charts/{chartkey}/leaderboards")
	public ResponseData<List<ChartLeaderboardDTO>> getChartLeaderboards(@PathVariable String chartkey) {
		m_logger.info("API CALLED :: GetChartLeaderboards");
		List<ChartLeaderboardDTO> leaderboard = scoreService.getChartLeaderboard(chartkey);
		ResponseData<List<ChartLeaderboardDTO>> r = new ResponseData<>();
		if (leaderboard == null) {
			r.error(404);
		} else {
			r.setData(leaderboard);
		}
		return r;
	}
	
	@GetMapping("/packs/collections")
	public ResponseData<List<CoreBundleDTO>> getCoreBundles() {
		m_logger.info("API CALLED :: GetCoreBundles");
		packService.getCoreBundles();
		ResponseData<List<CoreBundleDTO>> r = new ResponseData<>();
		r.error(404);
		return r;
	}
	
	@GetMapping("/client/version")
	public ResponseData<GetClientVersionResponse> getClientVersion() {
		m_logger.info("API CALLED :: GetClientVersion");
		miscService.getClientVersion();
		return ResponseData.ok(new GetClientVersionResponse(GAME_VERSION));
	}
	
	@GetMapping("/user/{userName}/top/")
	public String getOverallTop25(@PathVariable String userName) {
		m_logger.info("API CALLED :: GetOverallTop25");
		userService.getTop25();
		return dummy();
	}
	
	@GetMapping("/user/{userName}/top/{skillset}/{count}")
	public String getSkillsetTopX(@PathVariable String userName, @PathVariable String skillset, @PathVariable int count) {
		m_logger.info("API CALLED :: GetSkillsetTopX");
		userService.getTop25();
		return dummy();
	}
	
	@GetMapping("/user/{userName}")
	public ResponseData<GetUserInfoResponse> getUserInfo(@PathVariable String userName) {
		m_logger.info("API CALLED :: GetUserInfo");
		GetUserInfoResponse dto = userService.getUserInfo(userName);
		ResponseData<GetUserInfoResponse> r = new ResponseData<>();
		if (dto == null) {
			r.error(404);
		} else {
			r.setData(dto);
		}
		return r;
	}
	
	@PostMapping("/login")
	public ResponseData<LoginResponse> startSession(LoginRequest req) {
		m_logger.info("API CALLED :: StartSession");
		LoginResponse dto = sessionService.login(req);
		ResponseData<LoginResponse> r = new ResponseData<>();
		if (dto == null) {
			r.error(404);
		} else {
			r.setData(dto);
		}
		return r;
	}

}
