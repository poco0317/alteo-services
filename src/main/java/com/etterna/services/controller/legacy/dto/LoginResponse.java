package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginResponse {
	
	private SessionTokenDTO attributes;

	@Getter @Setter
	public static class SessionTokenDTO {
		private String accessToken;
	}

}
