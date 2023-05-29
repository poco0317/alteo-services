package com.etterna.multi.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "multi_lobby_messages")
@Getter @Setter
public class LobbyMessage {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "sender")
	private String sender;
	
	@Lob
	@Column(name = "content", nullable = false)
	private String content;
	
	@Column(name = "sent", nullable = false)
	private Date sent;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lobby")
	private GameLobby lobby;

}
