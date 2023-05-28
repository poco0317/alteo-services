package com.etterna.site.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackNameWithChartCount {
	
	private String pack = "";
	private Integer count = 0;
	private Integer scoreCount = 0;
	private Double averageScores = 0.0;
	
	public PackNameWithChartCount(String pack, Integer count) {
		this.pack = pack;
		this.count = count;
	}
	
	public PackNameWithChartCount(String pack, Long count) {
		this.pack = pack;
		this.count = count.intValue();
	}
	
	public void setCount(Integer count) {
		this.count = count;
		updateAvgscores();
	}
	public void setScoreCount(Integer scoreCount) {
		this.scoreCount = scoreCount;
		updateAvgscores();
	}
	
	private void updateAvgscores() {
		if (count != 0) {
			averageScores = scoreCount.doubleValue() / count.doubleValue();
		}
	}

}
