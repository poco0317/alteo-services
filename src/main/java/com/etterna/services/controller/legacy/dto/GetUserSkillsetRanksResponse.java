package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetUserSkillsetRanksResponse {
	
	private Ranks attributes;

	@Getter @Setter
	public static class Ranks {
		private Integer Overall;
		private Integer Stream;
		private Integer Jumpstream;
		private Integer Handstream;
		private Integer Stamina;
		private Integer JackSpeed;
		private Integer Chordjack;
		private Integer Technical;
	}
}
