package com.etterna.site.dto;

public class PackNameWithChartCount {
	
	private String pack = "";
	private Integer count = 0;
	private Integer scoreCount = 0;
	
	public PackNameWithChartCount(String pack, Integer count) {
		this.pack = pack;
		this.count = count;
	}
	
	public PackNameWithChartCount(String pack, Long count) {
		this.pack = pack;
		this.count = count.intValue();
	}
	
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getScoreCount() {
		return scoreCount;
	}
	public void setScoreCount(Integer scoreCount) {
		this.scoreCount = scoreCount;
	}

}
