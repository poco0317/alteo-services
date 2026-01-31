package com.etterna.services.opensearch.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HighScorePagination {
	
	List<HighScoreFullUnion> hses = new ArrayList<>();;
	int currentPage = 1;
	int totalPages = 1;
	
	public HighScorePagination(List<HighScoreFullUnion> hss, int currentPage, int totalPages) {
		this.hses = hss;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}

}
