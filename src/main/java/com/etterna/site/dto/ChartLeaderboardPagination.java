package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.datamodel.Chart;

public class ChartLeaderboardPagination {
	
	Chart chart;
	List<HighScoreWithSkillsets> scores = new ArrayList<>();
	List<Integer> rates = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	int rate = -1; // -1 is all rates
	
	public ChartLeaderboardPagination(Chart chart, List<HighScoreWithSkillsets> scores, int currentPage, int totalPages,
			int rate) {
		this.chart = chart;
		this.scores = scores;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.rate = rate;
	}
	
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public List<HighScoreWithSkillsets> getScores() {
		return scores;
	}
	public void setScores(List<HighScoreWithSkillsets> scores) {
		this.scores = scores;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public List<Integer> getRates() {
		return rates;
	}
	public void setRates(List<Integer> rates) {
		this.rates = rates;
	}
	
}
