package com.etterna.services.controller.legacy.dto;

public class GetUserSkillsetRanksResponse {
	
	private Ranks attributes;
	
	public Ranks getAttributes() {
		return attributes;
	}

	public void setAttributes(Ranks attributes) {
		this.attributes = attributes;
	}

	public static class Ranks {
		private Integer Overall;
		private Integer Stream;
		private Integer Jumpstream;
		private Integer Handstream;
		private Integer Stamina;
		private Integer JackSpeed;
		private Integer Chordjack;
		private Integer Technical;
		public Integer getOverall() {
			return Overall;
		}
		public void setOverall(Integer overall) {
			Overall = overall;
		}
		public Integer getStream() {
			return Stream;
		}
		public void setStream(Integer stream) {
			Stream = stream;
		}
		public Integer getJumpstream() {
			return Jumpstream;
		}
		public void setJumpstream(Integer jumpstream) {
			Jumpstream = jumpstream;
		}
		public Integer getHandstream() {
			return Handstream;
		}
		public void setHandstream(Integer handstream) {
			Handstream = handstream;
		}
		public Integer getStamina() {
			return Stamina;
		}
		public void setStamina(Integer stamina) {
			Stamina = stamina;
		}
		public Integer getJackSpeed() {
			return JackSpeed;
		}
		public void setJackSpeed(Integer jackSpeed) {
			JackSpeed = jackSpeed;
		}
		public Integer getChordjack() {
			return Chordjack;
		}
		public void setChordjack(Integer chordjack) {
			Chordjack = chordjack;
		}
		public Integer getTechnical() {
			return Technical;
		}
		public void setTechnical(Integer technical) {
			Technical = technical;
		}
	}

}
