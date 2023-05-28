package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetClientVersionResponse {
	
	private VersionDTO attributes;
	
	public GetClientVersionResponse() {}
	public GetClientVersionResponse(String version) {
		attributes = new VersionDTO();
		attributes.setVersion(version);
	}

	@Getter @Setter
	public static class VersionDTO {
		private String version;
	}
}
