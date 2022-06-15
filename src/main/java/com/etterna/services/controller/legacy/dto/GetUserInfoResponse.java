package com.etterna.services.controller.legacy.dto;

public class GetUserInfoResponse {
	
	private UserInfoDTO attributes;
	
	public UserInfoDTO getAttributes() {
		return attributes;
	}

	public void setAttributes(UserInfoDTO attributes) {
		this.attributes = attributes;
	}

	public class UserInfoDTO {
		private UserSkillsetDTO skillsets;
		private Double playerRating;
		
		public UserSkillsetDTO getSkillsets() {
			return skillsets;
		}

		public void setSkillsets(UserSkillsetDTO skillsets) {
			this.skillsets = skillsets;
		}

		public Double getPlayerRating() {
			return playerRating;
		}

		public void setPlayerRating(Double playerRating) {
			this.playerRating = playerRating;
		}

		public class UserSkillsetDTO {
			private Double Overall;
			private Double Stream;
			private Double Jumpstream;
			private Double Handstream;
			private Double Stamina;
			private Double JackSpeed;
			private Double Chordjack;
			private Double Technical;
			public Double getOverall() {
				return Overall;
			}
			public void setOverall(Double overall) {
				Overall = overall;
			}
			public Double getStream() {
				return Stream;
			}
			public void setStream(Double stream) {
				Stream = stream;
			}
			public Double getJumpstream() {
				return Jumpstream;
			}
			public void setJumpstream(Double jumpstream) {
				Jumpstream = jumpstream;
			}
			public Double getHandstream() {
				return Handstream;
			}
			public void setHandstream(Double handstream) {
				Handstream = handstream;
			}
			public Double getStamina() {
				return Stamina;
			}
			public void setStamina(Double stamina) {
				Stamina = stamina;
			}
			public Double getJackSpeed() {
				return JackSpeed;
			}
			public void setJackSpeed(Double jackSpeed) {
				JackSpeed = jackSpeed;
			}
			public Double getChordjack() {
				return Chordjack;
			}
			public void setChordjack(Double chordjack) {
				Chordjack = chordjack;
			}
			public Double getTechnical() {
				return Technical;
			}
			public void setTechnical(Double technical) {
				Technical = technical;
			}
		}
	}

}
