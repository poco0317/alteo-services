package com.etterna.services.datamodel;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sessions")
public class LoginSession {
	
	@Id
	@Column(name = "session_jwt")
	private String sessionJwt;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public String getSessionJwt() {
		return sessionJwt;
	}

	public void setSessionJwt(String sessionJwt) {
		this.sessionJwt = sessionJwt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

}
