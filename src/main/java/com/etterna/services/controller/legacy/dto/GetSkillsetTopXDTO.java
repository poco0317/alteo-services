package com.etterna.services.controller.legacy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetSkillsetTopXDTO {
	
	private ScoreDTO attributes;
	private String id;

	@Getter @Setter
	public static class ScoreDTO {
		private String songName;
		private Float wife;
		@JsonProperty("Overall")
		private Float Overall;
		private String chartKey;
		private Float rate;
		private String difficulty;
		private SkillsetDTO skillsets;

		@Getter @Setter
		public static class SkillsetDTO {
			@JsonProperty("Overall")
			private Float Overall;
			@JsonProperty("Stream")
			private Float Stream;
			@JsonProperty("Jumpstream")
			private Float Jumpstream;
			@JsonProperty("Handstream")
			private Float Handstream;
			@JsonProperty("Stamina")
			private Float Stamina;
			@JsonProperty("JackSpeed")
			private Float JackSpeed;
			@JsonProperty("Chordjack")
			private Float Chordjack;
			@JsonProperty("Technical")
			private Float Technical;
			
			public SkillsetDTO() {}
			public SkillsetDTO(Float overall, Float stream, Float jumpstream, Float handstream, Float stamina,
					Float jackSpeed, Float chordjack, Float technical) {
				Overall = overall;
				Stream = stream;
				Jumpstream = jumpstream;
				Handstream = handstream;
				Stamina = stamina;
				JackSpeed = jackSpeed;
				Chordjack = chordjack;
				Technical = technical;
			}
		}
	}
}
