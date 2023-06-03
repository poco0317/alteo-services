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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiffDao {
	
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
	@SuppressWarnings("unused")
	public void updateDiffValues(Chart c, Set<ChartDiffValue> newDiffs) {
		Set<ChartDiffValue> diffs = c.getDiffValues();
		if (c.getCalcVersion() != calc.getCalcVersion()) {
			m_logger.debug("Updating diffs for {}", c.getChartKey());
			
			if (DELETE_OLD_DIFFS && diffs != null) {
				diffs.forEach(diff -> {
					chartDiffs.delete(diff);
				});
				c.setDiffValues(null);
				charts.save(c);
			}
			
			c.setDiffValues(newDiffs);
			c.setCalcVersion(calc.getCalcVersion());
			chartDiffs.saveAll(newDiffs);
			charts.save(c);
		}
	}

	@Transactional
	public void commitDiffs(Set<ChartDiffValue> diffs) {
		chartDiffs.saveAll(diffs);
	}

}
