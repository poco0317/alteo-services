package com.etterna.services.controller.legacy.dto;

import java.util.Collection;

import com.etterna.services.model.HighScore;
import com.etterna.services.model.ScoreSpecificValue;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HighScoreWithSkillsets {

	private HighScore score;
	private Double overall = 0.0;
	private Double stream = 0.0;
	private Double jumpstream = 0.0;
	private Double handstream = 0.0;
	private Double stamina = 0.0;
	private Double jackspeed = 0.0;
	private Double chordjack = 0.0;
	private Double technical = 0.0;
	
	public HighScoreWithSkillsets(HighScore hs, Collection<ScoreSpecificValue> diffValues) {
		this.score = hs;
		
		if (diffValues != null) {
			diffValues.forEach(cdv -> {
				final Double v = cdv.getValue();
				switch (cdv.getSkillset()) {
					case OVERALL:
						this.overall = v;
						break;
					case STREAM:
						this.stream = v;
						break;
					case JUMPSTREAM:
						this.jumpstream = v;
						break;
					case HANDSTREAM:
						this.handstream = v;
						break;
					case STAMINA:
						this.stamina = v;
						break;
					case JACKSPEED:
						this.jackspeed = v;
						break;
					case CHORDJACK:
						this.chordjack = v;
						break;
					case TECHNICAL:
						this.technical = v;
						break;
					default:
						break;
				}
			});
		}
	}

	public HighScoreWithSkillsets() {
	}
}
