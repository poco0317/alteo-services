package com.etterna.services.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_skillsets")
@Getter @Setter
public class UserSkillsets {
	
	@Id
	@Column(name = "userid")
	private Long userid;
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "userid")
	private User user;
	
	@Column(name = "Overall")
	private Float overall;
	
	@Column(name = "Stream")
	private Float stream;
	
	@Column(name = "Jumpstream")
	private Float jumpstream;
	
	@Column(name = "JackSpeed")
	private Float jackspeed;
	
	@Column(name = "Chordjack")
	private Float chordjack;
	
	@Column(name = "Technical")
	private Float technical;
	
	@Column(name = "Handstream")
	private Float handstream;
	
	@Column(name = "Stamina")
	private Float stamina;

}
