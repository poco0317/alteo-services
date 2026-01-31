package com.etterna.services.jpa.model;

import java.util.List;

import com.etterna.services.data.HighScore;

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
	public HighScoreCollection(List<HighScore> hses) {
		this.hses = hses;
		this.count = hses.size();
	}

}
