package com.etterna.services.dto;

import java.util.List;

import com.etterna.services.repo.projection.ChartProjection;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackPageDto {
	
	private List<ChartProjection> charts;
	private String packname;

}
