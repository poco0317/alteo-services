package com.etterna.site.dto;

import com.etterna.services.datamodel.Chart;

public class ChartWithCount {

	private Chart chart;
	private Long count;
	
	public ChartWithCount(Chart chart, Long count) {
		this.chart = chart;
		this.count = count;
	}
	
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	
}
