package com.etterna.services.controller.legacy.dto;

public class UploadScoreResponse {
	
	// set to "ssrResults" if a proper ssr return
	private String type;
	private SSRResultsDTO attributes;
	
	public static SSRResultsDTO dummyDTO() {
		SSRResultsDTO dto = new SSRResultsDTO();
		SSRResultsDTO.SSRs ssrs = new SSRResultsDTO.SSRs(1, 1, 1, 1, 1, 1, 1, 1);
		dto.setDiff(ssrs);
		return dto;
	}
	
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public SSRResultsDTO getAttributes() {
		return attributes;
	}


	public void setAttributes(SSRResultsDTO attributes) {
		this.attributes = attributes;
	}


	public static class SSRResultsDTO {
		private SSRs diff;
		
		public SSRs getDiff() {
			return diff;
		}

		public void setDiff(SSRs diff) {
			this.diff = diff;
		}

		public static class SSRs {
			private Float Overall;
			private Float Stream;
			private Float Jumpstream;
			private Float Handstream;
			private Float Stamina;
			private Float JackSpeed;
			private Float Chordjack;
			private Float Technical;
			
			// how much the player's overall increased by when given this score
			private Float Rating;
			
			public SSRs() {}
			public SSRs(float overall, float stream, float jumpstream, float handstream, float stamina, float jackspeed, float chordjack, float technical) {
				this.Overall = overall;
				this.Stream = stream;
				this.Jumpstream = jumpstream;
				this.Handstream = handstream;
				this.Stamina = stamina;
				this.JackSpeed = jackspeed;
				this.Chordjack = chordjack;
				this.Technical = technical;
				this.Rating = 0.f;
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

			public Float getRating() {
				return Rating;
			}

			public void setRating(Float rating) {
				Rating = rating;
			}
		}
	}

}
