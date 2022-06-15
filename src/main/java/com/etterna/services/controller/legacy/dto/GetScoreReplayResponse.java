package com.etterna.services.controller.legacy.dto;

import java.util.List;

public class GetScoreReplayResponse {
	
	private Replay attributes;
	
	public Replay getAttributes() {
		return attributes;
	}

	public void setAttributes(Replay attributes) {
		this.attributes = attributes;
	}

	public class Replay {
		// these Objects are either Integer or Float
		private List<List<Object>> replay;

		public List<List<Object>> getReplay() {
			return replay;
		}

		public void setReplay(List<List<Object>> replay) {
			this.replay = replay;
		}
	}

}
