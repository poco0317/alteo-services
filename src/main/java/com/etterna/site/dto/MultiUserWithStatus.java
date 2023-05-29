package com.etterna.site.dto;

import com.etterna.multi.data.UserLogin;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MultiUserWithStatus {
	
	private UserLogin user;
	private Boolean active;

}
