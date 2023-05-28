package com.etterna.services.controller.legacy.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GetScoreReplayResponse {
	
	private Replay attributes;

	@Getter @Setter
	public class Replay {
		// these Objects are either Integer or Float
		private List<List<Object>> replay;
	}
}
