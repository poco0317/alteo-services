package com.etterna.services.dao;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.repo.ChartRepository;

@Service
public class ChartDao {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ChartDao.class);
	
	@Autowired
	private ChartRepository repo;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private DiffService chartDiffs;
	
	@Transactional
	public void init() {
		m_logger.info("Starting Chart Difficulty Updates");
		List<Chart> all = repo.findByCalcVersionLessThan(calc.getCalcVersion());
		if (all != null) {
			m_logger.info("Found {} charts to update out of {} ranked charts.", all.size(), repo.count());
			all.forEach(c -> {
				chartDiffs.updateDiffValues(c);
			});
		} else {
			m_logger.info("Found no charts to update and no ranked charts.");
		}
		m_logger.info("Finished Chart Difficulty Updates");
	}

	@Transactional
	public Chart get(String chartkey) {
		return repo.findById(chartkey).orElse(null);
	}
	
	@Transactional
	public boolean ranked(String chartkey) {
		return get(chartkey) != null;
	}
	
	@Transactional
	public boolean rankChart(String chartkey, String diffname, String packname, String songname) {
		if (ranked(chartkey))
			return false;
		
		m_logger.info("Ranking chart {}", chartkey);
		Chart c = new Chart();
		c.setChartKey(chartkey);
		c.setDifficulty(diffname);
		c.setPackName(packname);
		c.setSongName(songname);
		c.setCalcVersion(calc.getCalcVersion());
		repo.save(c);
		
		Set<ChartDiffValue> diffs = calc.calcDiffValues(c, 1.f, .93f);
		chartDiffs.commitDiffs(c, diffs);
		c.setDiffValues(diffs);
		
		return true;
	}

}
