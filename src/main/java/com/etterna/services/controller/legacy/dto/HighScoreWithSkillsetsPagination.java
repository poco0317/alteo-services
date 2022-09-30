package com.etterna.services.controller.legacy.dto;

import java.util.ArrayList;
import java.util.List;

public class HighScoreWithSkillsetsPagination {
	
	List<HighScoreWithSkillsets> hss = new ArrayList<>();;
	int currentPage = 1;
	int totalPages = 1;
	
	public HighScoreWithSkillsetsPagination(List<HighScoreWithSkillsets> hss, int currentPage, int totalPages) {
		this.hss = hss;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}
	public List<HighScoreWithSkillsets> getHss() {
		return hss;
	}
	public void setHss(List<HighScoreWithSkillsets> hss) {
		this.hss = hss;
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

}
