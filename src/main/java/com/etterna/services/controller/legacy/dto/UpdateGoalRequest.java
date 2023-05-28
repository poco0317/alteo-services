package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateGoalRequest {
	
	private String chartkey;
	private String rate;
	private String wife;
	private String achieved;
	private String timeAssigned;
	private String timeAchieved;
}
