package com.etterna.site.dto;

import com.etterna.services.model.Chart;

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
	
	public ChartWithSkillsets(Chart c, Integer count) {
		this.scoreCount = count;
		this.chart = c;
		
		overall = c.getSs1Value();
		stream = c.getSs2Value();
		jumpstream = c.getSs3Value();
		handstream = c.getSs4Value();
		stamina = c.getSs5Value();
		jackspeed = c.getSs6Value();
		chordjack = c.getSs7Value();
		technical = c.getSs8Value();
	}

	public ChartWithSkillsets() {}
}
