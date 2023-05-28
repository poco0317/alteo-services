package com.etterna.services.datamodel;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	@Column(name = "username", nullable = false)
	private String username;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "must_recalc_rating")
	private Boolean mustRecalcRating = true;
	
	@OneToMany(mappedBy = "user")
	private Set<HighScore> scores;
	
	@OneToMany(mappedBy = "user")
	private Set<LoginSession> loginSessions;
	
	@OneToMany(mappedBy = "id.user")
	private Set<UserSkillsetValue> skillsetValues;

}
