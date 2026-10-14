package com.etterna.services.jpa.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.data.User;
import com.etterna.services.repo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserDao {
	
	@Autowired
	private UserRepository users;

	public List<User> findByMustRecalcRatingTrue() {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(User user) {
		// TODO Auto-generated method stub
		
	}

	public User get(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, User> getByUserNamesMap(Collection<String> usernames) {
		// TODO Auto-generated method stub
		return null;
	}

	public void syncUsers(Collection<User> userList) {
		// TODO Auto-generated method stub
		
	}

	public void newUser(String user, String pw) {
		// TODO Auto-generated method stub
		
	}

	public User getByUserId(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<User> findByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

}
