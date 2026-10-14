package com.etterna.services.dto;

import java.util.List;

import com.etterna.services.data.Chart;
import com.etterna.services.data.Pack;
import com.etterna.services.data.Song;
import com.etterna.services.repo.projection.ChartProjection;
import com.etterna.services.repo.projection.ScoreProjection;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartInfo {
	
	private List<Pack> packs;
	private ChartProjection chart;
	private List<ScoreProjection> scores;

}
