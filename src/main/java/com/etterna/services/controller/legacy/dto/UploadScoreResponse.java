package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
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

	@Getter @Setter
	public static class SSRResultsDTO {
		private SSRs diff;

		@Getter @Setter
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
		}
	}
}
