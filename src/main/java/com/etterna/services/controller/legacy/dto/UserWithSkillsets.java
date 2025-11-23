package com.etterna.services.controller.legacy.dto;

import com.etterna.services.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserWithSkillsets {
	
	private User user;
	
	private Double overall = 0.0;
	private Double stream = 0.0;
	private Double jumpstream = 0.0;
	private Double handstream = 0.0;
	private Double stamina = 0.0;
	private Double jackspeed = 0.0;
	private Double chordjack = 0.0;
	private Double technical = 0.0;
	
	public UserWithSkillsets() {}
	public UserWithSkillsets(User u) {
		this.user = u;
		this.overall = u.getSs1Value();
		this.stream = u.getSs2Value();
		this.jumpstream = u.getSs3Value();
		this.handstream = u.getSs4Value();
		this.stamina = u.getSs5Value();
		this.jackspeed = u.getSs6Value();
		this.chordjack = u.getSs7Value();
		this.technical = u.getSs8Value();
	}
}
