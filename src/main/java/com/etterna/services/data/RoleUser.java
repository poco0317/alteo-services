package com.etterna.services.data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_users")
@Getter @Setter
public class RoleUser {
	
	@EmbeddedId
	private RoleUserId id;

}
