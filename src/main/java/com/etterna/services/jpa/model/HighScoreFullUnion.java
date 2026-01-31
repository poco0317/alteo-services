package com.etterna.services.jpa.model;

import com.etterna.services.data.HighScore;
import com.etterna.services.data.User;
import com.etterna.site.dto.ChartWithSkillsets;

import lombok.Getter;
import lombok.Setter;

/**
 * HighScore containing the User, ChartWithDiffValues, HighScore
 */
@Getter @Setter
public class HighScoreFullUnion {

	private HighScore hsUnion = new HighScore();
	private User user;
	private ChartWithSkillsets chartUnion = new ChartWithSkillsets();
	
}
