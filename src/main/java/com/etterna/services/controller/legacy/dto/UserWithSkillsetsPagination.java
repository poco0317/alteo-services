package com.etterna.services.controller.legacy.dto;

import java.util.ArrayList;
import java.util.List;

public class UserWithSkillsetsPagination {

	List<UserWithSkillsets> uss = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public UserWithSkillsetsPagination(List<UserWithSkillsets> uss, int currentPage, int totalPages) {
		this.uss = uss;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}
	
	public List<UserWithSkillsets> getUss() {
		return uss;
	}
	public void setUss(List<UserWithSkillsets> uss) {
		this.uss = uss;
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
