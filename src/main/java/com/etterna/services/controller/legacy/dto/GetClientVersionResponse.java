package com.etterna.services.controller.legacy.dto;

public class GetClientVersionResponse {
	
	private VersionDTO attributes;
	
	public VersionDTO getAttributes() {
		return attributes;
	}

	public void setAttributes(VersionDTO attributes) {
		this.attributes = attributes;
	}

	public class VersionDTO {
		private String version;

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}

}
