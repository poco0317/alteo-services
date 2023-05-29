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

}
