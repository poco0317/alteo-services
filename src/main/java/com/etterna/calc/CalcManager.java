package com.etterna.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.math3.special.Erf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.dao.NoteInfoDao;
import com.etterna.calc.jni.MinaCalcJNI;
import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartSkillsetValuesHistory;
import com.etterna.services.model.HighScore;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalcManager {
		
	private static ThreadLocal<MinaCalcJNI> minacalc = new ThreadLocal<>();
	
	@Autowired
	private NoteInfoDao noteInfo;
	
	public static final float MAX_SSR_GOAL = 0.965f;
	public static final float BASE_MSD_GOAL = 0.93f;
	
	@PostConstruct
	private void init() {
		minacalc.set(new MinaCalcJNI());
		
		m_logger.info("Initialized CalcManager - calc version {}", getCalcVersion());
		
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
		if (!noteInfo.exists(chartkey)) {
			m_logger.info("Tried to get SSR for unranked file - {}", chartkey);
			return new ArrayList<>();
		}
		
		m_logger.debug("Calculating SSR - {} - {}x - {}%", chartkey, rate, goal * 100);
		byte[] data = noteInfo.getData(chartkey);
		int ifThisIsntHereItCrashes = data.length;
		float[] ssrs = calc().minaSDCalcBytes(data, rate, goal);
		List<Float> o = new ArrayList<>(ssrs.length);
		for (float f : ssrs) {
			o.add(f);
		}
		while (o.size() < 8) {
			o.add(0.f);
		}
		printSkillsets(o);
		return o;
	}
	
	/**
	 * Runs SSRs on a single chartkey given a list of {rate, goal}, returning lists of ssrs in the same order
	 */
	public List<List<Float>> getSSRs(String chartkey, List<float[]> rategoals) {
		if (!noteInfo.exists(chartkey)) {
			m_logger.info("Tried to get SSRs for unranked file - {}", chartkey);
			return new ArrayList<>();
		}
		
		List<List<Float>> ssrs = new ArrayList<>();
		MinaCalcJNI calc = calc();
		byte[] data = noteInfo.getData(chartkey);
		int ifThisIsntHereItCrashes = data.length;
		for (float[] rg : rategoals) {
			float[] ssr = calc.minaSDCalcBytes(data, rg[0], rg[1]);
			List<Float> o = new ArrayList<>(ssr.length);
			for (float f : ssr) {
				o.add(f);
			}
			while (o.size() < 8) {
				o.add(0.f);
			}
			ssrs.add(o);
			printSkillsets(o);
		}
		return ssrs;
	}
	
	/**
	 * Runs SSRs on a single chartkey for a given list of scores, returning mapping of scores to ssrs
	 */
	public Map<HighScore, List<Float>> getSSRs(String chartkey, Collection<HighScore> hses) {
		if (!noteInfo.exists(chartkey)) {
			m_logger.info("Tried to get SSRs for unranked file - {}", chartkey);
			return new HashMap<>();
		}
		
		Map<HighScore, List<Float>> hsToSsrs = new HashMap<>();
		MinaCalcJNI calc = calc();
		byte[] data = noteInfo.getData(chartkey);
		int ifThisIsntHereItCrashes = data.length;
		hses.forEach(hs -> {
			float rate = hs.getMusicRate() / 100.F;
			float goal = hs.getSsrNorm() / 1000000.F;
			float[] ssr = calc.minaSDCalcBytes(data, rate, goal);
			List<Float> o = new ArrayList<>(ssr.length);
			for (float f : ssr) {
				o.add(f);
			}
			while (o.size() < 8) {
				o.add(0.f);
			}
			hsToSsrs.put(hs, o);
			//printSkillsets(o);
		});
		return hsToSsrs;
	}
	
	public static void printSkillsets(List<Float> ssrs) {
		if (ssrs.size() != 8) {
			m_logger.info("Can't print SSRs because list is not good");
			return;
		}
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
	@Deprecated
	@Transactional
	public String diffsToString(Set<ChartSkillsetValuesHistory> diffs, boolean inHTMLTableForm) {
		StringBuilder sb = new StringBuilder();
		
		/*
		List<ChartSkillsetValuesHistory> zzz = diffs.stream().filter(dv -> dv.getCalcVersion() == getCalcVersion()).collect(Collectors.toList());
		Collections.sort(zzz, new Comparator<ChartSkillsetValuesHistory>() {
			@Override
			public int compare(ChartSkillsetValuesHistory d1, ChartSkillsetValuesHistory d2) {
				return d1.getSkillset().compareTo(d2.getSkillset());
			}
		});
		if (!inHTMLTableForm) {
			zzz.forEach(ss -> {
				sb.append(ss.getSkillset().name() + " : "+String.format("%5.2f", ss.getValue()) + " - ");
			});
			if (zzz.size() > 0) {
				sb.delete(sb.length() - 3, sb.length());
			}
		} else {
			zzz.forEach(ss -> {
				sb.append("<td>"+String.format("%5.2f", ss.getValue())+"</td>");
			});
		}
		*/
		
		return sb.toString();
	}
	
	@Deprecated
	@Transactional
	public String ssrsToString(List<Float> diffs, boolean inHTMLTableForm) {
		StringBuilder sb = new StringBuilder();
		
		/*
		if (!inHTMLTableForm) {
			diffs.forEach(ss -> {
				sb.append(ss.getSkillset().name() + " : "+String.format("%5.2f", ss.getValue()) + " - ");
			});
			if (diffs.size() > 0) {
				sb.delete(sb.length() - 3, sb.length());
			}
		} else {
			diffs.forEach(ss -> {
				sb.append("<td>"+String.format("%5.2f", ss.getValue())+"</td>");
			});
		}
		*/
		
		return sb.toString();
	}
	
	/**
	 * Will return calc diff values for a chart
	 */
	public ChartSkillsetValuesHistory calcDiffValues(Chart c, float rate, float goal) {
		final String ck = c.getChartKey();
		m_logger.trace("Getting MSD for file {}", ck);
		byte[] data = noteInfo.getData(ck);
		int ifThisIsntHereItCrashes = data.length;
		float[] ssrs = calc().minaSDCalcBytes(data, rate, goal);
		List<Double> diffs = new ArrayList<>(ssrs.length);
		for (float f : ssrs) {
			diffs.add((double)f);
		}
		while (diffs.size() < 8) {
			diffs.add(0.0);
		}
		final Integer ver = getCalcVersion();
		return new ChartSkillsetValuesHistory(ck, ver, diffs.get(0), diffs.get(1), diffs.get(2), diffs.get(3), diffs.get(4), diffs.get(5), diffs.get(6), diffs.get(7));
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
