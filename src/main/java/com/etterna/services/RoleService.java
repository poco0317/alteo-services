package com.etterna.services;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Service;

import com.etterna.services.model.Role;
import com.etterna.services.model.RoleUser;
import com.etterna.services.opensearch.RoleIndexService;
import com.etterna.services.opensearch.RoleUserIndexService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleService {
	
	@Autowired
	private RoleIndexService roles;
	
	@Autowired
	private RoleUserIndexService userRoleIndex;
	
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
			roles.save(role, Refresh.True);
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
		urole.setRole(role);
		urole.setUsername(username);
		userRoleIndex.save(urole, Refresh.True);
	}
	
	

}
