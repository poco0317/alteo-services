package com.etterna.services.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.etterna.calc.Skillset;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.User;
import com.etterna.site.dto.UserDTO;

@Controller
@RequestMapping("/")
public class SiteFrontendController {

	private static final Logger m_logger = LoggerFactory.getLogger(SiteFrontendController.class);
	
	@Autowired
	private UserDao users;
	
	@Autowired
	private HighScoreDao scores;
	
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
	public String getUsernameModelAndPage(Model model, @PathVariable("username") String username) {
		User u = users.get(username);
		if (u == null) {
			return "home";
		}
		m_logger.info("FRONTEND API :: User Page {}", username);
		
		model.addAttribute("user", u);
		model.addAttribute("skillsets", users.getUserSkillsets(u));
		model.addAttribute("scores", scores.getUserScores(u, Skillset.OVERALL));
		
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
	
}
