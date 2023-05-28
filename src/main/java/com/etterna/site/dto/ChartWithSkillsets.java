package com.etterna.site.dto;

import com.etterna.services.datamodel.Chart;

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
	
	public ChartWithSkillsets(Chart c, Integer scoreCount) {
		this.scoreCount = scoreCount;
		this.chart = c;
		
		if (chart.getDiffValues() != null) {
			chart.getDiffValues().forEach(cdv -> {
				final Double v = cdv.getValue();
				switch (cdv.getId().getSkillset()) {
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
}
