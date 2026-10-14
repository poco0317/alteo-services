package com.etterna.services.jpa.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.data.Role;
import com.etterna.services.repo.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleDao {
	
	@Autowired
	private RoleRepository roles;

	public Role save(Role role) {
		return roles.save(role);
	}

	public Role findById(String name) {
		return roles.findById(name).orElse(null);
	}

}
