package com.etterna.services.opensearch.model;

import com.etterna.services.opensearch.model.Chart;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartWithCount {

	private Chart chart;
	private Long count;
	
	public ChartWithCount(Chart chart, Long count) {
		this.chart = chart;
		this.count = count;
	}
	
}
