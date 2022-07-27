package com.etterna.services.datamodel;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	@Column(name = "username", nullable = false)
	private String username;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "salt", nullable = false)
	private String salt;
	
	@OneToMany(mappedBy = "user")
	private Set<HighScore> scores;
	
	@OneToMany(mappedBy = "user")
	private Set<LoginSession> loginSessions;
	
	@OneToMany(mappedBy = "id.user")
	private Set<UserSkillsetValue> skillsetValues;

	public Set<UserSkillsetValue> getSkillsetValues() {
		return skillsetValues;
	}

	public void setSkillsetValues(Set<UserSkillsetValue> skillsetValues) {
		this.skillsetValues = skillsetValues;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<HighScore> getScores() {
		return scores;
	}

	public void setScores(Set<HighScore> scores) {
		this.scores = scores;
	}

	public Set<LoginSession> getLoginSessions() {
		return loginSessions;
	}

	public void setLoginSessions(Set<LoginSession> loginSessions) {
		this.loginSessions = loginSessions;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

}
