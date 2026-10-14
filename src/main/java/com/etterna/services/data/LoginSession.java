package com.etterna.services.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "login_sessions")
@Getter @Setter
public class LoginSession {
	
	@Id
	@Column(name = "session_jwt", nullable = false)
	private String sessionJwt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@Column(name = "username", nullable = false)
	private String username;	

}
