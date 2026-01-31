package com.etterna.services.opensearch.model;

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
