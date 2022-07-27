package com.etterna.services.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.PasswordUtil;
import com.etterna.services.datamodel.User;
import com.etterna.services.repo.UserRepository;

@Service
public class UserDao {

	private static final Logger m_logger = LoggerFactory.getLogger(UserDao.class);
	
	@Autowired
	private UserRepository repo;

	@Transactional
	public User get(String username) {
		List<User> users = repo.findByUsername(username);
		if (users == null || users.isEmpty()) {
			return null;
		}
		return users.get(0);
	}
	
	@Transactional
	public boolean newUser(String username, String password) {
		if (get(username) != null) {
			return false;
		}
		m_logger.info("Created new user {}", username);
		User user = new User();
		user.setUsername(username);
		String salt = PasswordUtil.getSalt();
		String pwsalt = PasswordUtil.hashPassword(password, salt);
		
		user.setSalt(salt);
		user.setPassword(pwsalt);
		repo.save(user);
		return true;
	}

}
