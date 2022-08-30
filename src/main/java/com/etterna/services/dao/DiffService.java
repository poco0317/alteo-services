package com.etterna.services.dao;

import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.repo.ChartDiffValueRepository;
import com.etterna.services.repo.ChartRepository;

@Service
public class DiffService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(DiffService.class);
	
	@Autowired
	private ChartRepository charts;
	
	@Autowired
	private ChartDiffValueRepository chartDiffs;
	
	@Autowired
	private CalcManager calc;
	
	private static final boolean DELETE_OLD_DIFFS = false;
	
	/**
	 * The way this is usually used, Transactional is not necessary
	 */
	public void updateDiffValues(Chart c, Set<ChartDiffValue> newDiffs) {
		Set<ChartDiffValue> diffs = c.getDiffValues();
		if (diffs != null && c.getCalcVersion() < calc.getCalcVersion()) {
			m_logger.debug("Updating diffs for {}", c.getChartKey());
			
			if (DELETE_OLD_DIFFS) {
				diffs.forEach(diff -> {
					chartDiffs.delete(diff);
				});
			}
			
			c.setDiffValues(null);
			charts.save(c);
			c.setDiffValues(newDiffs);
			c.setCalcVersion(calc.getCalcVersion());
			chartDiffs.saveAll(newDiffs);
		}
	}

	@Transactional
	public void commitDiffs(Chart c, Set<ChartDiffValue> diffs) {
		chartDiffs.saveAll(diffs);
	}

}
