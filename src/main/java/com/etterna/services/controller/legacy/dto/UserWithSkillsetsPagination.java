package com.etterna.services.controller.legacy.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserWithSkillsetsPagination {

	List<UserWithSkillsets> uss = new ArrayList<>();
	int currentPage = 1;
	int totalPages = 1;
	
	public UserWithSkillsetsPagination(List<UserWithSkillsets> uss, int currentPage, int totalPages) {
		this.uss = uss;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
	}
}
