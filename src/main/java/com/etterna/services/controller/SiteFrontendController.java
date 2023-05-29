package com.etterna.services.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.etterna.multi.data.GameLobby;
import com.etterna.services.MultiplayerDataService;
import com.etterna.services.MultiplayerRequestService;
import com.etterna.services.XmlProfileParsingService;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsetsPagination;
import com.etterna.services.controller.legacy.dto.UserWithSkillsetsPagination;
import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.PackDao;
import com.etterna.services.dao.RankingDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.datamodel.User;
import com.etterna.site.dto.AllLeaderboardSort;
import com.etterna.site.dto.ChartLeaderboardPagination;
import com.etterna.site.dto.ChartLeaderboardSort;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.ChartsInPackPagination;
import com.etterna.site.dto.LeaderboardSort;
import com.etterna.site.dto.NeoUserPrincipal;
import com.etterna.site.dto.PackContentSort;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;
import com.etterna.site.dto.ProfileSort;
import com.etterna.site.dto.UserDTO;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class SiteFrontendController {
	
	@Value("${etterna.note-info-folder-path}")
	private String rootNoteinfoPath;
	
	@Autowired
	private UserDao users;
	
	@Autowired
	private HighScoreDao scores;
	
	@Autowired
	private XmlProfileParsingService xmls;
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private RankingDao chartRanking;
	
	@Autowired
	private PackDao packs;
	
	@Autowired
	private MultiplayerRequestService multiplayerApi;
	
	@Autowired
	private MultiplayerDataService multiplayerData;
	
	private int parseRate(Optional<String> rate) {
		String rt = rate.orElse("-1");
		
		try {
			Float d = Float.parseFloat(rt);
			return Math.round(d);
		} catch (Exception e) {
			// if it cant be parsed it isnt worth caring about
			return -1;
		}
		
	}
	
	@GetMapping("/register")
	public String getRegisterModel(Model model) {
		model.addAttribute("userinfo", new UserDTO());
		return "registerPage";
	}
	
	@PostMapping("/register")
	public ModelAndView handleRegistry(Model model, @ModelAttribute("userinfo") UserDTO userDto, HttpServletRequest request) {		
		m_logger.info("FRONTEND API :: Registry Attempted");
		
		if (!userDto.validUsername()) {
			return new ModelAndView("redirect:/register?baduser");
		}
		if (!userDto.validPassword()) {
			return new ModelAndView("redirect:/register?badpass");
		}
		if (!userDto.validConfirmedPassword()) {
			// fails if mismatching password
			return new ModelAndView("redirect:/register?mismatch");
		}
		
		if (users.newUser(userDto.getUsername(), userDto.getPassword())) {
			m_logger.info("Successfully made new account for {}");
			return new ModelAndView("redirect:/login?registered");
		} else {
			m_logger.info("Failed to make new account for {}");
			return new ModelAndView("redirect:/register?duplicate");
		}
	}
	
	@GetMapping("/")
	public String getHomeModel(Model model) {
		m_logger.info("FRONTEND API :: HOME");
		model.addAttribute("multiPlayers", multiplayerApi.getOnlinePlayers());
		return "home";
	}
	
	@GetMapping("/user/{username}")
	public String getUsernameModelAndPage(Model model, @PathVariable("username") String username, @RequestParam("page") Optional<Integer> page, @RequestParam("sort") Optional<String> sort) {
		User u = users.get(username);
		if (u == null) {
			return "home";
		}
		m_logger.info("FRONTEND API :: User Page {}", username);
		
		int currentPage = page.orElse(1);
		ProfileSort ps = ProfileSort.fromString(sort.orElse("date"));
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		HighScoreWithSkillsetsPagination hspage = scores.getUserScores(u, ps, currentPage, itemsperpage);
		int actualcurrentpage = hspage.getCurrentPage();
		int maxpage = hspage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("user", u);
		model.addAttribute("skillsets", users.getUserSkillsets(u));
		model.addAttribute("scores", hspage.getHss());
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ps.name());
		model.addAttribute("uncalculatedScores", scores.countUncalculatedScores(u));
		model.addAttribute("incalculableScores", scores.countIncalculableScores(u));
		
		return "profile";
	}
	
	@GetMapping("/user/{username}/passreset")
	public String resetPassword(@PathVariable("username") String username) {
		User u = users.get(username);
		if (u == null) {
			return "home";
		}
		m_logger.info("FRONTEND API :: User Password Reset {}", username);
		
		users.resetPassword(u);
		
		return "home";
	}
	
	@PostMapping("/user/{username}/xml")
	public ModelAndView uploadXml(@PathVariable("username") String username, @RequestParam("xml") MultipartFile file) throws IOException {
		NeoUserPrincipal user = (NeoUserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!user.getUsername().equalsIgnoreCase(username)) {
			m_logger.info("user {} tried to upload to {}", user.getUsername(), username);
			// deny people uploading to the wrong profile
			return new ModelAndView("redirect:/");
		}
		
		m_logger.info("FRONTEND API :: User XML Upload {}", username);
		xmls.intakeProfile(file.getInputStream(), user.getUser());
		
		return new ModelAndView("redirect:/user/"+username+"?uploaded");
	}
	
	@GetMapping("/leaderboard")
	public String getLeaderboard(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("sort") Optional<String> sort) {
		m_logger.info("FRONTEND API :: Leaderboard");
		
		int currentPage = page.orElse(1);
		LeaderboardSort ls = LeaderboardSort.fromString(sort.orElse("overall"));
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		UserWithSkillsetsPagination uspage = users.getUserLeaderboard(ls, currentPage, itemsperpage);
		int actualcurrentpage = uspage.getCurrentPage();
		int maxpage = uspage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("users", uspage.getUss());
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ls.name());
		
		return "leaderboard";
	}
	
	@GetMapping("/packs")
	public String getPacks(Model model, @RequestParam("page") Optional<Integer> page, @RequestParam("sort") Optional<String> sort) {
		m_logger.info("FRONTEND API :: Packs");
		
		int currentPage = page.orElse(1);
		PacksSort ps = PacksSort.fromString(sort.orElse("name"));
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		PackNameWithChartCountPagination ppage = charts.getPacksAndChartCounts(ps, currentPage, itemsperpage);
		int actualcurrentpage = ppage.getCurrentPage();
		int maxpage = ppage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("packs", ppage.getPwcc());
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ps.name());
		
		return "packs";
	}
	
	@GetMapping("/packs/{pack}")
	public String getPackContents(Model model, @PathVariable("pack") String pack, @RequestParam("page") Optional<Integer> page, @RequestParam("sort") Optional<String> sort) {
		pack = URLDecoder.decode(pack, StandardCharsets.UTF_8);
		m_logger.info("FRONTEND API :: Pack Contents {}", pack);
		
		int currentPage = page.orElse(1);
		PackContentSort ps = PackContentSort.fromString(sort.orElse("name"));
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		Pack packObj = packs.get(pack);
		ChartsInPackPagination ppage = charts.getChartsInPackPagination(pack, ps, currentPage, itemsperpage);
		int actualcurrentpage = ppage.getCurrentPage();
		int maxpage = ppage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("charts", ppage.getCwss());
		model.addAttribute("pack", pack);
		model.addAttribute("packObj", packObj);
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ps.name());
		
		return "packContent";
	}
	
	@GetMapping("/chart/{chartkey}")
	public String getChartLeaderboard(Model model,
			@PathVariable("chartkey") String chartkey,
			@RequestParam("page") Optional<Integer> page,
			@RequestParam("sort") Optional<String> sort,
			@RequestParam("rate") Optional<String> rate) {
		m_logger.info("FRONTEND API :: Chart Leaderboard {}", chartkey);
		
		int currentPage = page.orElse(1);
		ChartLeaderboardSort ls = ChartLeaderboardSort.fromString(sort.orElse("overall"));
		int selectedrate = parseRate(rate); // -1 if "all rates" leaderboard, 100 if 1.0x leaderboard
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		ChartLeaderboardPagination ppage = scores.getChartLeaderboardPagination(chartkey, selectedrate, ls, currentPage, itemsperpage);
		int actualcurrentpage = ppage.getCurrentPage();
		int maxpage = ppage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("chart", new ChartWithSkillsets(ppage.getChart(), 0));
		model.addAttribute("scores", ppage.getScores());
		model.addAttribute("currentRate", ppage.getRate());
		model.addAttribute("rates", ppage.getRates());
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ls.name());
		
		return "chart";
	}
	
	@GetMapping("/score/{scorekey}")
	public String getScorePage(Model model, @PathVariable("scorekey") String scorekey) {
		m_logger.info("FRONTEND API :: Score Page {}", scorekey);
		
		model.addAttribute("score", scores.getScoreWithSkillsets(scorekey));
		
		return "score";
	}
	
	@GetMapping("/allscores")
	public String getAllScores(Model model,
			@RequestParam("page") Optional<Integer> page,
			@RequestParam("sort") Optional<String> sort,
			@RequestParam("rate") Optional<String> rate) {
		m_logger.info("FRONTEND API :: All Scores");
		
		int currentPage = page.orElse(1);
		AllLeaderboardSort ls = AllLeaderboardSort.fromString(sort.orElse("date"));
		int selectedrate = parseRate(rate); // -1 if "all rates" leaderboard, 100 if 1.0x leaderboard
		final int directionaldistance = 2;
		final int itemsperpage = 200;
		
		ChartLeaderboardPagination ppage = scores.getLeaderboardForAllChartsPagination(selectedrate, ls, currentPage, itemsperpage);
		int actualcurrentpage = ppage.getCurrentPage();
		int maxpage = ppage.getTotalPages();
		List<Integer> pagenumbers = IntStream.rangeClosed(Math.max(1, actualcurrentpage - directionaldistance), Math.min(maxpage, actualcurrentpage + directionaldistance)).boxed().collect(Collectors.toList());
		
		model.addAttribute("scores", ppage.getScores());
		model.addAttribute("currentRate", ppage.getRate());
		model.addAttribute("rates", ppage.getRates());
		model.addAttribute("currentPage", actualcurrentpage);
		model.addAttribute("pageRange", pagenumbers);
		model.addAttribute("maxPage", maxpage);
		model.addAttribute("currentSort", ls.name());
		
		return "allscores";
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
		
		model.addAttribute("sessionId", sessionId);
		model.addAttribute("lobby", lobby);
		model.addAttribute("players", multiplayerData.getPlayersInSession(id));
		model.addAttribute("messages", multiplayerData.getMessagesInSession(id));		
		model.addAttribute("scores", multiplayerData.getScoresInSession(id));
		
		return "multisession";
	}
	
	@GetMapping("/admin")
	public String adminPage(Model model) {
		
		model.addAttribute("rankedcharts", chartRanking.getTotalRankedCharts());
		model.addAttribute("pendingpacks", chartRanking.getPackQueueSize());
		
		return "admin";
	}
	
	@PostMapping("/admin/rankpacks")
	public ModelAndView adminRankPacks(@RequestParam("packs") MultipartFile[] packs) {
		NeoUserPrincipal user = (NeoUserPrincipal)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		m_logger.info("FRONTEND API :: Admin Pack Ranking Upload - {} files by {}", packs.length, user.getUsername());
		
		for (MultipartFile f : packs) {
			ArrayList<String> songdatas = new ArrayList<>();
			final Pattern rootnamer = Pattern.compile("([^\\\\/]*)[\\\\/]");
			String packname = "No Pack Name";
			
			try (ZipInputStream zipin = new ZipInputStream(f.getInputStream())) {
				
				ZipEntry entry = zipin.getNextEntry();
				while (entry != null) {
					
					String name = entry.getName();
					if (name.endsWith(".cache")) {
						String filename = new File(name).getName();
						Matcher rooter = rootnamer.matcher(name);
						if (rooter.find()) {
							packname = rooter.group(1);
						}
						
						m_logger.trace("File path {}", name);
						m_logger.info("Extracting filename {}", filename);
						
						if (name.contains("songdata")) {
							// should be song data
							String content = new String(zipin.readAllBytes(), StandardCharsets.UTF_8);
							songdatas.add(content);
						} else {
							// should be noteinfo
							OutputStream fileout = new FileOutputStream(rootNoteinfoPath + "/" + filename, false);
							fileout.write(zipin.readAllBytes());
							fileout.close();
						}
						
					} else {
						m_logger.info("Skipped filename {}", name);
					}
					
					
					zipin.closeEntry();
					entry = zipin.getNextEntry();
				}
			} catch (Exception e) {
				m_logger.warn("Attempted to parse upload and failed. {}", e);
			}
			
			chartRanking.queuePackForRanking(songdatas, packname);
		}
		m_logger.info("Finished queueing {} packs to rank", packs.length);
		
		return new ModelAndView("redirect:/admin?packsuploaded");
	}
	
}
