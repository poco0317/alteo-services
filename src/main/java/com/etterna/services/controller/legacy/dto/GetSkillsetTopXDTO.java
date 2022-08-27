package com.etterna.services.controller.legacy.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetSkillsetTopXDTO {
	
	private ScoreDTO attributes;
	private String id;

	public ScoreDTO getAttributes() {
		return attributes;
	}

	public void setAttributes(ScoreDTO attributes) {
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static class ScoreDTO {
		private String songName;
		private Float wife;
		@JsonProperty("Overall")
		private Float Overall;
		private String chartKey;
		private Float rate;
		private String difficulty;
		private SkillsetDTO skillsets;
		
		public String getSongName() {
			return songName;
		}

		public void setSongName(String songName) {
			this.songName = songName;
		}

		public Float getWife() {
			return wife;
		}

		public void setWife(Float wife) {
			this.wife = wife;
		}

		public Float getOverall() {
			return Overall;
		}

		public void setOverall(Float overall) {
			Overall = overall;
		}

		public String getChartKey() {
			return chartKey;
		}

		public void setChartKey(String chartKey) {
			this.chartKey = chartKey;
		}

		public Float getRate() {
			return rate;
		}

		public void setRate(Float rate) {
			this.rate = rate;
		}

		public String getDifficulty() {
			return difficulty;
		}

		public void setDifficulty(String difficulty) {
			this.difficulty = difficulty;
		}

		public SkillsetDTO getSkillsets() {
			return skillsets;
		}

		public void setSkillsets(SkillsetDTO skillsets) {
			this.skillsets = skillsets;
		}

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
			public Float getOverall() {
				return Overall;
			}
			public void setOverall(Float overall) {
				Overall = overall;
			}
			public Float getStream() {
				return Stream;
			}
			public void setStream(Float stream) {
				Stream = stream;
			}
			public Float getJumpstream() {
				return Jumpstream;
			}
			public void setJumpstream(Float jumpstream) {
				Jumpstream = jumpstream;
			}
			public Float getHandstream() {
				return Handstream;
			}
			public void setHandstream(Float handstream) {
				Handstream = handstream;
			}
			public Float getStamina() {
				return Stamina;
			}
			public void setStamina(Float stamina) {
				Stamina = stamina;
			}
			public Float getJackSpeed() {
				return JackSpeed;
			}
			public void setJackSpeed(Float jackSpeed) {
				JackSpeed = jackSpeed;
			}
			public Float getChordjack() {
				return Chordjack;
			}
			public void setChordjack(Float chordjack) {
				Chordjack = chordjack;
			}
			public Float getTechnical() {
				return Technical;
			}
			public void setTechnical(Float technical) {
				Technical = technical;
			}
		}
	}

}
