package com.etterna.site.dto;

import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartSkillsetValuesHistory;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartWithSkillsets {

	private Chart chart;
	private Double overall = 0.0;
	private Double stream = 0.0;
	private Double jumpstream = 0.0;
	private Double handstream = 0.0;
	private Double stamina = 0.0;
	private Double jackspeed = 0.0;
	private Double chordjack = 0.0;
	private Double technical = 0.0;
	private Integer scoreCount = 0;
	
	public ChartWithSkillsets(Chart c, ChartSkillsetValuesHistory diffValues, Integer count) {
		this.scoreCount = count;
		this.chart = c;
		
		if (diffValues != null) {
			overall = diffValues.getSs1Value();
			stream = diffValues.getSs2Value();
			jumpstream = diffValues.getSs3Value();
			handstream = diffValues.getSs4Value();
			stamina = diffValues.getSs5Value();
			jackspeed = diffValues.getSs6Value();
			chordjack = diffValues.getSs7Value();
			technical = diffValues.getSs8Value();
		}
	}

	public ChartWithSkillsets() {}
}
