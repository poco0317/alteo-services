package com.etterna.site.dto;

import com.etterna.multi.data.LobbyScore;
import com.etterna.services.datamodel.Chart;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LobbyScoreWithChart {
	
	private LobbyScore score;
	private Chart chart;
	private String chartkey;
	
	public LobbyScoreWithChart(LobbyScore score, String chartkey, Chart chart) {
		this.score = score;
		this.chart = chart;
		this.chartkey = chartkey;
	}
	public LobbyScoreWithChart(LobbyScore score, String chartkey) {
		this.score = score;
		this.chartkey = chartkey;
		this.chart = null;
	}
	public LobbyScoreWithChart(Object[] objs) {
		if (objs.length == 2) {
			this.score = (LobbyScore)objs[0];
			this.chartkey = (String)objs[1];
			this.chart = null;
		} else if (objs.length == 3) {
			this.score = (LobbyScore)objs[0];
			this.chartkey = (String)objs[1];
			this.chart = (Chart)objs[2];
		} else {
			throw new RuntimeException("Wrong input object array size, expecting 2 or 3 and got "+objs.length);
		}
	}

}
