package com.etterna.services.controller.legacy.dto;

import com.etterna.services.datamodel.User;

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
}
