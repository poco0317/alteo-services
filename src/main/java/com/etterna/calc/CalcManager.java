package com.etterna.calc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.math3.special.Erf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.etterna.calc.jni.MinaCalcJNI;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;
import com.etterna.services.datamodel.ScoreSpecificValue;

@Service
public class CalcManager {
	
	private static final Logger m_logger = LoggerFactory.getLogger(CalcManager.class);
	
	private static ThreadLocal<MinaCalcJNI> minacalc = new ThreadLocal<>();
	
	@Value("${etterna.note-info-folder-path}")
	private String noteInfoFolder;
	
	@PostConstruct
	private void init() {
		minacalc.set(new MinaCalcJNI());
		
		m_logger.info("Initialized CalcManager - calc version {}", getCalcVersion());
		
	}
	
	/**
	 * Get the path to the NoteInfo binary file given a chartkey
	 */
	private Path noteInfoPath(String chartKey) {
		return Paths.get(noteInfoFolder, chartKey + ".cache");
	}
	
	private MinaCalcJNI calc() {
		if (minacalc.get() == null) {
			minacalc.set(new MinaCalcJNI());
		}
		return minacalc.get();
	}
	
	public int getCalcVersion() {
		return Integer.parseInt(calc().getCalcVersion());
	}
	
	/**
	 * Get SSRs for a chart at a rate for a goal
	 * Usually the ordering is: Overall, Stream, Jumpstream, Handstream, Stamina, JackSpeed, Chordjack, Technical
	 * Returns null if the chart is not found (basically not ranked)
	 */
	public List<Float> getSSR(String chartkey, float rate, float goal) {
		final Path path = noteInfoPath(chartkey);
		if (Files.notExists(path)) {
			m_logger.info("Tried to get SSR for unranked file - {}", chartkey);
			return null;
		}
		
		m_logger.debug("Calculating SSR - {} - {}x - {}%", chartkey, rate, goal * 100);
		float[] ssrs = calc().minaSDCalc(path.toString(), rate, goal);
		List<Float> o = new ArrayList<>(ssrs.length);
		for (float f : ssrs) {
			o.add(f);
		}
		printSkillsets(o);
		return o;
	}
	
	public static void printSkillsets(List<Float> ssrs) {
		m_logger.debug("Overall {} | Stream {} | Jumpstream {} | Handstream {} | Stamina {} | JackSpeed {} | Chordjack {} | Technical {}",
				String.format("%5.2f", ssrs.get(0)),
				String.format("%5.2f", ssrs.get(1)),
				String.format("%5.2f", ssrs.get(2)),
				String.format("%5.2f", ssrs.get(3)),
				String.format("%5.2f", ssrs.get(4)),
				String.format("%5.2f", ssrs.get(5)),
				String.format("%5.2f", ssrs.get(6)),
				String.format("%5.2f", ssrs.get(7)));
	}
	
	/**
	 * Outputs diff values in basic string or HTML table row form
	 * Will only display values that are of the latest calc version
	 */
	@Transactional
	public String diffsToString(Set<ChartDiffValue> diffs, boolean inHTMLTableForm) {
		StringBuilder sb = new StringBuilder();
		
		List<ChartDiffValue> zzz = diffs.stream().filter(dv -> dv.getId().getCalcVersion() == getCalcVersion()).collect(Collectors.toList());
		Collections.sort(zzz, new Comparator<ChartDiffValue>() {
			@Override
			public int compare(ChartDiffValue d1, ChartDiffValue d2) {
				return d1.getId().getSkillset().compareTo(d2.getId().getSkillset());
			}
		});
		if (!inHTMLTableForm) {
			zzz.forEach(ss -> {
				sb.append(ss.getId().getSkillset().name() + " : "+String.format("%5.2f", ss.getValue()) + " - ");
			});
			if (zzz.size() > 0) {
				sb.delete(sb.length() - 3, sb.length());
			}
		} else {
			zzz.forEach(ss -> {
				sb.append("<td>"+String.format("%5.2f", ss.getValue())+"</td>");
			});
		}
		
		return sb.toString();
	}
	
	@Transactional
	public String ssrsToString(Set<ScoreSpecificValue> diffs, boolean inHTMLTableForm) {
		StringBuilder sb = new StringBuilder();
		
		List<ScoreSpecificValue> zzz = diffs.stream().filter(ssv -> ssv.getId().getCalcVersion() == getCalcVersion()).collect(Collectors.toList());
		Collections.sort(zzz, new Comparator<ScoreSpecificValue>() {
			@Override
			public int compare(ScoreSpecificValue d1, ScoreSpecificValue d2) {
				return d1.getId().getSkillset().compareTo(d2.getId().getSkillset());
			}
		});
		if (!inHTMLTableForm) {
			zzz.forEach(ss -> {
				sb.append(ss.getId().getSkillset().name() + " : "+String.format("%5.2f", ss.getValue()) + " - ");
			});
			if (zzz.size() > 0) {
				sb.delete(sb.length() - 3, sb.length());
			}
		} else {
			zzz.forEach(ss -> {
				sb.append("<td>"+String.format("%5.2f", ss.getValue())+"</td>");
			});
		}
		
		return sb.toString();
	}
	
	/**
	 * Will return calc diff values for a chart
	 */
	public Set<ChartDiffValue> calcDiffValues(Chart c, float rate, float goal) {
		m_logger.debug("Getting MSD for file {}", c.getChartKey());
		final List<Float> diffs = getSSR(c.getChartKey(), rate, goal);
		final int ver = getCalcVersion();
		return new HashSet<>(Arrays.asList(new ChartDiffValue[] {
				new ChartDiffValue(c, diffs.get(0).doubleValue(), Skillset.OVERALL, ver),
				new ChartDiffValue(c, diffs.get(1).doubleValue(), Skillset.STREAM, ver),
				new ChartDiffValue(c, diffs.get(2).doubleValue(), Skillset.JUMPSTREAM, ver),
				new ChartDiffValue(c, diffs.get(3).doubleValue(), Skillset.HANDSTREAM, ver),
				new ChartDiffValue(c, diffs.get(4).doubleValue(), Skillset.STAMINA, ver),
				new ChartDiffValue(c, diffs.get(5).doubleValue(), Skillset.JACKSPEED, ver),
				new ChartDiffValue(c, diffs.get(6).doubleValue(), Skillset.CHORDJACK, ver),
				new ChartDiffValue(c, diffs.get(7).doubleValue(), Skillset.TECHNICAL, ver),
		}));
	}
	
	/**
	 * Sigmoidal aggregation or whatever I forget the name
	 * Given a bunch of values give a skill output
	 * default rating = 0
	 * default resolution = 10.24
	 */
	public Double aggregateSkill(List<Double> vals, double deltaMult, double resultMult, double rating, double resolution) {
		for (int i = 0; i < 11; i++) {
			double sum = 0.0;
			do {
				rating += resolution;
				sum = 0.0;
				for (Double v : vals) {
					sum += Math.max(0.0, 2.0 / Erf.erfc(deltaMult * (v - rating)) - 2);
				}
			} while (Math.pow(2, rating * 0.1) < sum);
			rating -= resolution;
			resolution /= 2.0;
		}
		rating += resolution * 2.0;
		return rating * resultMult;
	}
	
	/*
	 with diffs as (select a.skillset, (b.value - a.value) as 'diff', a.chart_key from chart_diff_values a, chart_diff_values b where a.skillset = b.skillset and a.chart_key = b.chart_key and a.calc_version = '500' and b.calc_version = '505')
     select d.skillset, d.diff, ifnull(c.translit_title, c.title), c.difficulty, c.chart_key from diffs d, charts c where d.chart_key = c.chart_key and skillset = '0' order by d.diff limit 50;
	 */
}
