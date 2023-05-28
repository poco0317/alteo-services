package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetUserInfoResponse {
	
	private UserInfoDTO attributes;

	@Getter @Setter
	public static class UserInfoDTO {
		private UserSkillsetDTO skillsets;
		private Double playerRating;

		@Getter @Setter
		public static class UserSkillsetDTO {
			private Double Overall;
			private Double Stream;
			private Double Jumpstream;
			private Double Handstream;
			private Double Stamina;
			private Double JackSpeed;
			private Double Chordjack;
			private Double Technical;
		}
	}
}
