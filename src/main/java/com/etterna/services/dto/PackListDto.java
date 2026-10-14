package com.etterna.services.dto;

import com.etterna.services.repo.projection.PackListProjection;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackListDto {
	
	private String packname;
	private Long chartcount;
	private Long scorecount;
	private Double averagescores = 0.0;
	
	public PackListDto() {}
	public PackListDto(PackListProjection p) {
		
		packname = p.getPackname();
		chartcount = p.getChartcount();
		scorecount = p.getScorecount();
		
		if (chartcount != null && chartcount > 0) {
			averagescores = scorecount.doubleValue() / chartcount.doubleValue();
		}
		
	}

}
