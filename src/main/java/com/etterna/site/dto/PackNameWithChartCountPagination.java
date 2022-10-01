package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

public class PackNameWithChartCountPagination {
	
	List<PackNameWithChartCount> pwcc = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public PackNameWithChartCountPagination(List<PackNameWithChartCount> pwcc, int currentPage, int totalPages) {
		this.pwcc = pwcc;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}
	
	public List<PackNameWithChartCount> getPwcc() {
		return pwcc;
	}
	public void setPwcc(List<PackNameWithChartCount> pwcc) {
		this.pwcc = pwcc;
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
