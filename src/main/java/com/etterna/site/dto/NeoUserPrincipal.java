package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.etterna.services.model.Role;
import com.etterna.services.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NeoUserPrincipal implements UserDetails {
	private static final long serialVersionUID = 1L;
	
	private User user;
	private Set<Role> roles;
	
	public NeoUserPrincipal(User user, Set<Role> roles) {
		this.user = user;
		this.roles = roles;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> auths = new ArrayList<>();
		for (Role role : roles) {
			auths.add(new SimpleGrantedAuthority(role.getName()));
		}
		
		return auths;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
