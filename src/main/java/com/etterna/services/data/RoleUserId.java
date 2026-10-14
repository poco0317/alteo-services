package com.etterna.services.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter @EqualsAndHashCode
public class RoleUserId implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "username", nullable = false)
	private String username;
	
	@Column(name = "role", nullable = false)
	private String role;

}
