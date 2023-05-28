package com.etterna.services.controller.legacy.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HighScoreWithSkillsetsPagination {
	
	List<HighScoreWithSkillsets> hss = new ArrayList<>();;
	int currentPage = 1;
	int totalPages = 1;
	
	public HighScoreWithSkillsetsPagination(List<HighScoreWithSkillsets> hss, int currentPage, int totalPages) {
		this.hss = hss;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}

}
