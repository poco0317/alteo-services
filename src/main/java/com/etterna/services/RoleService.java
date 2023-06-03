package com.etterna.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Service;

import com.etterna.services.datamodel.Role;
import com.etterna.services.repo.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleService {
	
	@Autowired
	private RoleRepository roles;
	
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_USER = "ROLE_USER";
	
	public void maintainRoles() {
		get(ROLE_ADMIN);
		get(ROLE_USER);
	}
	
	@Transactional
	public Role get(String name) {
		List<Role> l = roles.findByName(name);
		Role role = l != null && l.size() > 0 ? l.get(0): null;
		if (role == null) {
			m_logger.info("Created new role {}", name);
			role = new Role();
			role.setName(name);
			role = roles.save(role);
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

}
