package com.etterna.services;

import org.springframework.stereotype.Service;

import com.etterna.site.dto.ChartsInPackPagination;
import com.etterna.site.dto.PackContentSort;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;

@Service
public class ChartService {

	public PackNameWithChartCountPagination getPacksAndChartCounts(PacksSort ps, int currentPage, int itemsperpage) {
		// TODO Auto-generated method stub
		return null;
	}

	public ChartsInPackPagination getChartsInPackPagination(String pack, PackContentSort ps, int currentPage,
			int itemsperpage) {
		// TODO Auto-generated method stub
		return null;
	}

}
