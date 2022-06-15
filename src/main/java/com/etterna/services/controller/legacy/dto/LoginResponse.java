package com.etterna.services.controller.legacy.dto;

public class LoginResponse {
	
	private SessionTokenDTO attributes;
	
	public SessionTokenDTO getAttributes() {
		return attributes;
	}

	public void setAttributes(SessionTokenDTO attributes) {
		this.attributes = attributes;
	}

	public class SessionTokenDTO {
		private String accessToken;

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
	}

}
