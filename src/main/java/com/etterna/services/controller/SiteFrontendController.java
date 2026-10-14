package com.etterna.services.controller;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.etterna.multi.data.GameLobby;
import com.etterna.services.LegacySiteFrontendService;
import com.etterna.services.MultiplayerDataService;
import com.etterna.services.MultiplayerRequestService;
import com.etterna.services.dto.ChartInfo;
import com.etterna.services.dto.PackPageDto;
import com.etterna.services.dto.UserInfoDto;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class SiteFrontendController {
	
	@Autowired
	private LegacySiteFrontendService frontendService;
	
	@Autowired
	private MultiplayerRequestService multiplayerApi;
	
	@Autowired
	private MultiplayerDataService multiplayerData;

	
	@GetMapping("/")
	public String getHomeModel(Model model) {
		m_logger.info("FRONTEND API :: HOME");
		//model.addAttribute("multiPlayers", multiplayerApi.getOnlinePlayers());
		return "home";
	}
	
	@GetMapping("/home")
	public String getHomeModel2(Model model) {
		return getHomeModel(model);
	}
	
	@GetMapping("/leaderboard")
	public String getLeaderboardModel(Model model, @RequestParam("sort") Optional<String> sort) {
		
		model.addAttribute("users", frontendService.getLeaderboard(sort));
		
		return "leaderboard";
	}
	
	@GetMapping("/user/{username}")
	public String getUserPage(Model model, @PathVariable("username") String username, @RequestParam("sort") Optional<String> sort) {
		
		UserInfoDto userinfo = frontendService.getUser(username, sort);
		model.addAttribute("user", userinfo.getUserAndSkillsets());
		model.addAttribute("scores", userinfo.getScores());
		
		return "profile";
	}
	
	@GetMapping("/userscores/{username}/{scorekey}")
	public String getScorePage(Model model, @PathVariable("username") String username, @PathVariable("scorekey") String scorekey) {
		
		UserInfoDto userinfo = frontendService.getScoreInfo(username, scorekey);
		model.addAttribute("user", userinfo.getUserAndSkillsets());
		model.addAttribute("score", userinfo.getScore());
		
		return "score";
	}
	
	@GetMapping("/allscores")
	public String getAllScores(Model model, @RequestParam("page") Optional<String> page, @RequestParam("sort") Optional<String> sort) {
		
		int pagenum = 1;
		if (page.isPresent()) {
			try {
				pagenum = Integer.parseInt(page.get());
			} catch (Exception e) {}
		}
		String cursort = "DATE";
		if (sort.isPresent()) {
			cursort = sort.get();
		}
		
		UserInfoDto userinfo = frontendService.getAllScores(pagenum, cursort);
		final int directionaldistance = 2;
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, pagenum - directionaldistance), Math.min(userinfo.getMaxPage(), pagenum + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("maxPage", userinfo.getMaxPage());
		model.addAttribute("currentPage", pagenum);
		model.addAttribute("currentSort", cursort);
		model.addAttribute("scores", userinfo.getScores());
		model.addAttribute("pageRange", pagenumbers);
		
		return "allscores";
	}
	
	@GetMapping("/chart/{chartkey}")
	public String getChartPage(Model model, @PathVariable("chartkey") String chartkey) {
		
		ChartInfo c = frontendService.getChartInfo(chartkey);
		
		model.addAttribute("chart", c.getChart());
		model.addAttribute("packs", c.getPacks());
		model.addAttribute("scores", c.getScores());
		model.addAttribute("maxPage", 1);
		model.addAttribute("currentPage", 1);
		
		return "chart";
	}
	
	@GetMapping("/packs")
	public String getPacks(Model model) {
		
		model.addAttribute("packs", frontendService.getPackList());
		model.addAttribute("currentPage", 1);
		model.addAttribute("maxPage", 1);
		
		return "packs";
	}
	
	@GetMapping("/packs/{packname}")
	public String getPackPage(Model model, @PathVariable("packname") String packname) {
		
		PackPageDto p = frontendService.getPackPage(packname);
		
		model.addAttribute("pack", p.getPackname());
		model.addAttribute("charts", p.getCharts());
		model.addAttribute("currentPage", 1);
		model.addAttribute("maxPage", 1);
		
		return "packContent";
	}
	
	@GetMapping("/multiplayer")
	public String getMultiHome(Model model) {
		m_logger.info("FRONTEND API :: Multiplayer");
		
		model.addAttribute("multiPlayers", multiplayerApi.getOnlinePlayers());
		
		return "multiplayer";
	}
	
	@GetMapping("/multiplayer/players")
	public String getMultiplayerPlayers(Model model) {
		m_logger.info("FRONTEND API :: Multiplayer Players");
		
		model.addAttribute("multiPlayers", multiplayerApi.getOnlinePlayers());
		
		return "multiplayers";
	}
	
	@GetMapping("/multiplayer/sessions")
	public String getMultiplayerSessions(Model model) {
		m_logger.info("FRONTEND API :: Multiplayer Sessions");
		
		model.addAttribute("sessions", multiplayerData.getMultiplayerSessions());
		
		return "multisessions";
	}
	
	@GetMapping("/multiplayer/session/{sessionid}")
	public String getMultiplayerSession(Model model, @PathVariable("sessionid") String sessionId) {
		m_logger.info("FRONTEND API :: Multiplayer Session {}", sessionId);
		
		Long id = 0L;
		try {
			id = Long.parseLong(sessionId);
		} catch (Exception e) {}
		
		GameLobby lobby = multiplayerData.getSession(id);
		if (lobby == null) {
			return getMultiplayerSessions(model);
		}
		
		model.addAttribute("sessionId", sessionId);
		model.addAttribute("lobby", lobby);
		model.addAttribute("players", multiplayerData.getPlayersInSession(id));
		model.addAttribute("messages", multiplayerData.getMessagesInSession(id));		
		model.addAttribute("scores", multiplayerData.getScoresInSession(id));
		
		return "multisession";
	}
	
	@GetMapping("/admin")
	public String adminPage(Model model) {
		
		//model.addAttribute("rankedcharts", chartRanking.getTotalRankedCharts());
		//model.addAttribute("pendingpacks", chartRanking.getPackQueueSize());
		
		return "admin";
	}
	
}
