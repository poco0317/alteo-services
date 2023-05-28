package com.etterna.site.dto;

import com.etterna.services.datamodel.Chart;

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
