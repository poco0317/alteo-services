package com.etterna.services.opensearch.model;

import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.model.User;
import com.etterna.site.dto.ChartWithSkillsets;

import lombok.Getter;
import lombok.Setter;

/**
 * HighScore containing the User, ChartWithDiffValues, and ScoreSpecificValues
 */
@Getter @Setter
public class HighScoreFullUnion {

	private HighScoreWithSkillsets hsUnion = new HighScoreWithSkillsets();
	private User user;
	private ChartWithSkillsets chartUnion = new ChartWithSkillsets();
	
}
