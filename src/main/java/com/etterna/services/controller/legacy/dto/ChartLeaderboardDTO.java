package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartLeaderboardDTO {
	
	private LeaderboardScoreDTO attributes;

	@Getter @Setter
	public static class LeaderboardScoreDTO {
		private Boolean hasReplay;
		private LeaderboardUserDTO user;
		private LeaderboardJudgmentsDTO judgements;
		private LeaderboardSkillsetDTO skillsets;
		private String songId;
		private Float wife;
		private String modifiers;
		private Integer maxCombo;
		private String datetime;
		private String id;
		private Float rate;
		private Boolean noCC;
		private Boolean valid;
		private Integer wifeVersion;
		
		@Getter @Setter
		public static class LeaderboardUserDTO {
			private String userName;
			private String avatar;
			private Integer userId;
			private String countryCode;
			private Float playerRating;
		}
		
		@Getter @Setter
		public static class LeaderboardJudgmentsDTO {
			private Integer marvelous;
			private Integer perfect;
			private Integer great;
			private Integer good;
			private Integer bad;
			private Integer miss;
			private Integer hitMines;
			private Integer heldHold;
			private Integer letGoHold;
		}
		
		@Getter @Setter
		public static class LeaderboardSkillsetDTO {
			private Float Overall;
			private Float Stream;
			private Float Jumpstream;
			private Float Handstream;
			private Float Stamina;
			private Float JackSpeed;
			private Float Chordjack;
			private Float Technical;
		}
	}

}
