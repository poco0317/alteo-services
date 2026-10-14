package com.etterna.multi.data;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "multi_game_lobbies")
@Getter @Setter
public class GameLobby {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "description", nullable = true)
	private String description;
	
	@Column(name = "passworded", nullable = false)
	private Boolean passworded;
	
	@Column(name = "active")
	private Boolean active = false;
	
	@Column(name = "created")
	private Date created = new Date();
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator", nullable = false)
	private UserLogin creator;
	
	@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	@JoinTable(
			name = "multi_game_lobby_users",
			joinColumns = @JoinColumn(name = "lobby"),
			inverseJoinColumns = @JoinColumn(name = "username"))
	private Set<UserLogin> users;
	
	@OneToMany(mappedBy = "lobby")
	private Set<LobbyScore> scores;
	
	@OneToMany(mappedBy = "lobby")
	private Set<LobbyMessage> messages;

	@Override
	public int hashCode() {
		return Objects.hash(active, created, creator, description, id, name, passworded, scores, users);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameLobby other = (GameLobby) obj;
		return Objects.equals(active, other.active) && Objects.equals(created, other.created)
				&& Objects.equals(creator, other.creator) && Objects.equals(description, other.description)
				&& Objects.equals(id, other.id) && Objects.equals(name, other.name)
				&& Objects.equals(passworded, other.passworded) && Objects.equals(scores, other.scores)
				&& Objects.equals(users, other.users);
	}
	
	

}
