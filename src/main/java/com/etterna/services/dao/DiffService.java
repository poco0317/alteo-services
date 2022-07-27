package com.etterna.services.dao;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.repo.ChartDiffValueRepository;
import com.etterna.services.repo.ChartRepository;

@Service
public class DiffService {
	
	@Autowired
	private ChartRepository charts;
	
	@Autowired
	private ChartDiffValueRepository chartDiffs;
	
	@Autowired
	private CalcManager calc;
	
	@Transactional
	public void updateDiffValues(Chart c) {
		Set<ChartDiffValue> diffs = c.getDiffValues();
		if (diffs != null && c.getCalcVersion() < calc.getCalcVersion()) {
			diffs.forEach(diff -> {
				chartDiffs.delete(diff);
			});
			chartDiffs.flush();
			c.setDiffValues(null);
			charts.save(c);
			diffs = calc.calcDiffValues(c, 1.f,.93f);
			c.setDiffValues(diffs);
			c.setCalcVersion(calc.getCalcVersion());
			chartDiffs.saveAll(diffs);
			charts.save(c);
		}
	}

	@Transactional
	public void commitDiffs(Chart c, Set<ChartDiffValue> diffs) {
		chartDiffs.deleteByIdChart(c);
		chartDiffs.saveAll(diffs);
	}

}
