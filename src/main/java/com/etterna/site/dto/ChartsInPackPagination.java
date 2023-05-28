package com.etterna.site.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartsInPackPagination {
	
	List<ChartWithSkillsets> cwss = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public ChartsInPackPagination(List<ChartWithSkillsets> cwss, int currentPage, int maxPage) {
		this.cwss = cwss;
		this.currentPage = currentPage;
		this.totalPages = maxPage;
	}

}
