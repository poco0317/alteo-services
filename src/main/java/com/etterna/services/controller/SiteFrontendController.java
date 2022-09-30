package com.etterna.services.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.etterna.calc.Skillset;
import com.etterna.services.XmlProfileParsingService;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsetsPagination;
import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.User;
import com.etterna.site.dto.NeoUserPrincipal;
import com.etterna.site.dto.ProfileSort;
import com.etterna.site.dto.UserDTO;

@Controller
@RequestMapping("/")
public class SiteFrontendController {

	private static final Logger m_logger = LoggerFactory.getLogger(SiteFrontendController.class);
	
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
	
	@GetMapping("/admin")
	public String adminPage(Model model) {
		
		model.addAttribute("rankedcharts", charts.getTotalRankedCharts());
		model.addAttribute("pendingpacks", charts.getPackQueueSize());
		
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
			
			charts.queuePackForRanking(songdatas, packname);
		}
		m_logger.info("Finished queueing {} packs to rank", packs.length);
		
		return new ModelAndView("redirect:/admin?packsuploaded");
	}
	
}
