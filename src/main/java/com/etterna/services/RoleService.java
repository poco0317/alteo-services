package com.etterna.services;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Service;

import com.etterna.services.data.Role;
import com.etterna.services.data.RoleUser;
import com.etterna.services.data.RoleUserId;
import com.etterna.services.data.User;
import com.etterna.services.jpa.dao.RoleDao;
import com.etterna.services.jpa.dao.RoleUserDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleService {
	
	@Autowired
	private RoleDao roles;
	
	@Autowired
	private RoleUserDao userRoles;
	
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";
	
	public void maintainRoles() {
		get(ROLE_ADMIN);
		get(ROLE_USER);
		
		grantRole("poco0317", ROLE_ADMIN);
	}
	
	@Transactional
	public Role get(String name) {
		Role role = roles.findById(name);
		if (role == null) {
			m_logger.info("Created new role {}", name);
			role = new Role();
			role.setName(name);
			roles.save(role);
		}
		return role;
	}
	
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl hier = new RoleHierarchyImpl();
		hier.setHierarchy(String.format(
				"%s > %s", ROLE_ADMIN, ROLE_USER));
		return hier;
	}
	
	@Transactional
	public Set<Role> getDefaultRole() {
		Role r = get(ROLE_USER);
		Set<Role> s = new HashSet<>();
		s.add(r);
		return s;
	}
	
	@Transactional
	public void grantRole(String username, String role) {
		RoleUser urole = new RoleUser();
		RoleUserId id = new RoleUserId();
		id.setRole(role);
		id.setUsername(username);
		userRoles.save(urole);
	}

	public Set<Role> findByUser(User u) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
