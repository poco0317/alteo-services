package com.etterna.services.jpa.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.data.RoleUser;
import com.etterna.services.repo.RoleUserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleUserDao {
	
	@Autowired
	private RoleUserRepository roleusers;

	public RoleUser save(RoleUser urole) {
		return roleusers.save(urole);
	}

}
