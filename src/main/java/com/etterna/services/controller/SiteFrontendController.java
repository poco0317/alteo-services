package com.etterna.services.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.etterna.site.dto.UserDTO;

@Controller
@RequestMapping("/")
public class SiteFrontendController {

	private static final Logger m_logger = LoggerFactory.getLogger(SiteFrontendController.class);
	
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
		
		m_logger.info("user account {} made", userDto.getUsername());
		return new ModelAndView("redirect:/login?registered");
	}
	
	
}
