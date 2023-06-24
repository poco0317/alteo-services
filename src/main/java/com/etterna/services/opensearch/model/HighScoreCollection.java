package com.etterna.services.opensearch.model;

import java.util.List;

import com.etterna.services.model.HighScore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HighScoreCollection {
	
	private List<HighScore> hses;
	private long count;
	
	public HighScoreCollection(List<HighScore> hses, long count) {
		this.hses = hses;
		this.count = count;
	}

}
