package com.etterna.site.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.etterna.services.model.User;
import com.etterna.services.opensearch.RoleIndexService;
import com.etterna.services.opensearch.UserIndexService;
import com.etterna.site.dto.NeoUserPrincipal;

@Service
public class NeoUserDetailsService implements UserDetailsService {
		
	@Autowired
	private UserIndexService users;
	
	@Autowired
	private RoleIndexService roles;
	
	private User get(String username) {
		List<User> us = users.findByUsername(username);
		if (us == null || us.isEmpty()) {
			return null;
		}
		return us.get(0);
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) {
		User u = get(username);
		if (u == null) {
			throw new UsernameNotFoundException(username);
		}
		return new NeoUserPrincipal(u, roles.findByUser(u));
	}

}
