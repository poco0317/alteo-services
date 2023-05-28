package com.etterna.services.datamodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sessions")
@Getter @Setter
public class LoginSession {
	
	@Id
	@Column(name = "session_jwt")
	private String sessionJwt;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	

}
