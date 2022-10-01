package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

public class ChartsInPackPagination {
	
	List<ChartWithSkillsets> cwss = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public ChartsInPackPagination(List<ChartWithSkillsets> cwss, int currentPage, int maxPage) {
		this.cwss = cwss;
		this.currentPage = currentPage;
		this.totalPages = maxPage;
	}
	
	public List<ChartWithSkillsets> getCwss() {
		return cwss;
	}
	public void setCwss(List<ChartWithSkillsets> cwss) {
		this.cwss = cwss;
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
