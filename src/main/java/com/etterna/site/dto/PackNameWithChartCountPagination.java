package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackNameWithChartCountPagination {
	
	List<PackNameWithChartCount> pwcc = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public PackNameWithChartCountPagination(List<PackNameWithChartCount> pwcc, int currentPage, int totalPages) {
		this.pwcc = pwcc;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}
}
