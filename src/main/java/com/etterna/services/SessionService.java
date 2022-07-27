package com.etterna.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.controller.legacy.dto.LoginRequest;
import com.etterna.services.controller.legacy.dto.LoginResponse;
import com.etterna.services.controller.legacy.dto.LoginResponse.SessionTokenDTO;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.LoginSession;
import com.etterna.services.datamodel.User;
import com.etterna.services.repo.LoginSessionRepository;

import io.jsonwebtoken.Jwts;

@Service
public class SessionService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(SessionService.class);

	@Autowired
	private LoginSessionRepository loginRepo;
	
	@Autowired
	private UserDao users;
	
	@Transactional
	public LoginResponse login(LoginRequest req) {
		// TODO Auto-generated method stub
		String user = req.getUsername();
		String pw = req.getPassword();
		
		User u = users.get(user);
		if (u == null) {
			users.newUser(user, pw);
		}
		u = users.get(user);
		if (u == null) {
			m_logger.error("Failed to get user after making new user... {}", user);
			return null;
		}
		
		String accessToken = newSession(u);
		
		LoginResponse resp = new LoginResponse();
		SessionTokenDTO dto = new SessionTokenDTO();
		dto.setAccessToken(accessToken);
		resp.setAttributes(dto);
		return resp;
	}
	
	/**
	 * Primitive: just check to see the session is real
	 */
	@Transactional
	public boolean validateSession(String jwt) {
		LoginSession sessions = loginRepo.findById(jwt).orElse(null);
		if (sessions == null) {
			return false;
		}
		return true;
	}
	
	@Transactional
	public User sessionToUser(String jwt) {
		LoginSession sess = loginRepo.findById(jwt).orElse(null);
		if (sess != null) {
			return sess.getUser();
		}
		return null;
	}
	
	/**
	 * Given a valid User, return a unique session token
	 */
	private String newSession(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("username", user.getUsername());
		claims.put("uid", user.getUserId());
		Date now = new Date();
		String jwt = Jwts.builder().addClaims(claims).setIssuedAt(now).compact();
		LoginSession session = new LoginSession();
		session.setCreatedAt(now);
		session.setUser(user);
		session.setSessionJwt(jwt);
		loginRepo.save(session);
		return jwt;
	}

}
