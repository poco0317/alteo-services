package com.etterna.multi.data;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * This is meant for alteo user logins basically
 *
 */
@Entity
@Table(name = "multi_user_logins")
@Getter @Setter
public class UserLogin {
	
	@Id
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "salt")
	private String salt;
	
	@OneToMany(mappedBy = "creator")
	private Set<GameLobby> createdLobbies;
	
	@ManyToMany(mappedBy = "users", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	private Set<GameLobby> lobbiesParticipatedIn;
	
	@OneToMany(mappedBy = "user")
	private Set<LobbyScore> scores;

}
