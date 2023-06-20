package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

import com.etterna.services.model.Chart;
import com.etterna.services.opensearch.model.HighScoreFullUnion;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartLeaderboardPagination {
	
	Chart chart;
	List<HighScoreFullUnion> scores = new ArrayList<>();
	List<Integer> rates = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	int rate = -1; // -1 is all rates
	
	public ChartLeaderboardPagination(Chart chart, List<HighScoreFullUnion> scores, int currentPage, int totalPages,
			int rate) {
		this.chart = chart;
		this.scores = scores;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.rate = rate;
	}
	
}
