package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartDiffValue;
import com.etterna.services.opensearch.ChartDiffValueIndexService;
import com.etterna.services.opensearch.ChartIndexService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DiffDao {
	
	@Autowired
	private ChartIndexService chartIndex;
	
	@Autowired
	private ChartDiffValueIndexService chartDiffIndex;
	
	@Autowired
	private CalcManager calc;
	
	private static final boolean DELETE_OLD_DIFFS = false;
	private static final int AUTO_COMMIT_CHUNK_SIZE = 100;
	
	private Queue<Object[]> stagedChartsAndDiffs = new ConcurrentLinkedQueue<>();
	
	/**
	 * This prepares new difficulty values to be committed but does not save anything.
	 * This may delete old difficulty values under certain circumstances.
	 */
	@SuppressWarnings("unused")
	public void stageUpdatedDiffValues(Chart c, Set<ChartDiffValue> newDiffs, boolean instantCommit) {
		Set<ChartDiffValue> diffs = chartDiffIndex.getDiffValues(c);
		if (c.getCalcVersion() != calc.getCalcVersion()) {
			m_logger.debug("Updating diffs for {}", c.getChartKey());
			
			if (DELETE_OLD_DIFFS && diffs != null) {
				diffs.forEach(diff -> {
					chartDiffIndex.delete(diff, Refresh.False);
				});
			}
			
			c.setCalcVersion(calc.getCalcVersion());
			
			if (instantCommit) {
				chartDiffIndex.saveBulk(newDiffs, Refresh.False);
				chartIndex.save(c, Refresh.False);
			} else {
				stagedChartsAndDiffs.add(new Object[] {c, newDiffs});
				if (stagedChartsAndDiffs.size() >= AUTO_COMMIT_CHUNK_SIZE) {
					flushStagedDiffValues();
				}
			}
		}
	}
	
	/**
	 * Clears the difficulty values that are staged to be committed, if any. This usually takes place automatically in chunks of 100 charts
	 */
	@SuppressWarnings("unchecked")
	public void flushStagedDiffValues() {
		if (stagedChartsAndDiffs.isEmpty()) return;
		
		m_logger.info("Flushing to commit {} entries from the staged chart diff queue", stagedChartsAndDiffs.size());
		List<Chart> charts = new ArrayList<>();
		Set<ChartDiffValue> diffValues = new HashSet<>();
		while (!stagedChartsAndDiffs.isEmpty()) {
			Object[] entry = stagedChartsAndDiffs.poll();
			charts.add((Chart)entry[0]);
			diffValues.addAll((Set<ChartDiffValue>)entry[1]);
		}
		chartDiffIndex.saveBulk(diffValues, Refresh.False);
		chartIndex.saveBulk(charts, Refresh.False);
		m_logger.info("Finished flushing staged chart diff queue");
	}
	
	@Transactional
	public Set<ChartDiffValue> getDiffValues(Chart c) {
		return chartDiffIndex.getDiffValues(c);
	}

}
