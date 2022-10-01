package com.etterna.site.dto;

import com.etterna.services.datamodel.Chart;

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
	
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public Double getOverall() {
		return overall;
	}
	public void setOverall(Double overall) {
		this.overall = overall;
	}
	public Double getStream() {
		return stream;
	}
	public void setStream(Double stream) {
		this.stream = stream;
	}
	public Double getJumpstream() {
		return jumpstream;
	}
	public void setJumpstream(Double jumpstream) {
		this.jumpstream = jumpstream;
	}
	public Double getHandstream() {
		return handstream;
	}
	public void setHandstream(Double handstream) {
		this.handstream = handstream;
	}
	public Double getStamina() {
		return stamina;
	}
	public void setStamina(Double stamina) {
		this.stamina = stamina;
	}
	public Double getJackspeed() {
		return jackspeed;
	}
	public void setJackspeed(Double jackspeed) {
		this.jackspeed = jackspeed;
	}
	public Double getChordjack() {
		return chordjack;
	}
	public void setChordjack(Double chordjack) {
		this.chordjack = chordjack;
	}
	public Double getTechnical() {
		return technical;
	}
	public void setTechnical(Double technical) {
		this.technical = technical;
	}
	public Integer getScoreCount() {
		return scoreCount;
	}
	public void setScoreCount(Integer scoreCount) {
		this.scoreCount = scoreCount;
	}
}
