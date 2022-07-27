package com.etterna.calc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.etterna.calc.jni.MinaCalcJNI;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.ChartDiffValue;

@Service
public class CalcManager {
	
	private static final Logger m_logger = LoggerFactory.getLogger(CalcManager.class);
	
	private static MinaCalcJNI minacalc;
	
	@Value("${etterna.note-info-folder-path}")
	private String noteInfoFolder;
	
	@PostConstruct
	private void init() {
		minacalc = new MinaCalcJNI();
	}
	
	/**
	 * Get the path to the NoteInfo binary file given a chartkey
	 */
	private Path noteInfoPath(String chartKey) {
		return Paths.get(noteInfoFolder, chartKey + ".cache");
	}
	
	public int getCalcVersion() {
		return Integer.parseInt(minacalc.getCalcVersion());
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
		
		m_logger.info("Calculating SSR - {} - {}x - {}%", chartkey, rate, goal * 100);
		float[] ssrs = minacalc.minaSDCalc(path.toString(), rate, goal);
		List<Float> o = new ArrayList<>(ssrs.length);
		for (float f : ssrs) {
			o.add(f);
		}
		printSkillsets(o);
		return o;
	}
	
	private void printSkillsets(List<Float> ssrs) {
		m_logger.info("Overall {} | Stream {} | Jumpstream {} | Handstream {} | Stamina {} | JackSpeed {} | Chordjack {} | Technical {}", ssrs.get(0), ssrs.get(1), ssrs.get(2), ssrs.get(3), ssrs.get(4), ssrs.get(5), ssrs.get(6), ssrs.get(7));
	}
	
	/**
	 * Will return calc diff values for a chart
	 */
	public Set<ChartDiffValue> calcDiffValues(Chart c, float rate, float goal) {
		m_logger.info("Getting MSD for file {}", c.getChartKey());
		List<Float> diffs = getSSR(c.getChartKey(), rate, goal);
		return new HashSet<>(Arrays.asList(new ChartDiffValue[] {
				new ChartDiffValue(c, diffs.get(0).doubleValue(), Skillset.OVERALL),
				new ChartDiffValue(c, diffs.get(1).doubleValue(), Skillset.STREAM),
				new ChartDiffValue(c, diffs.get(2).doubleValue(), Skillset.JUMPSTREAM),
				new ChartDiffValue(c, diffs.get(3).doubleValue(), Skillset.HANDSTREAM),
				new ChartDiffValue(c, diffs.get(4).doubleValue(), Skillset.STAMINA),
				new ChartDiffValue(c, diffs.get(5).doubleValue(), Skillset.JACKSPEED),
				new ChartDiffValue(c, diffs.get(6).doubleValue(), Skillset.CHORDJACK),
				new ChartDiffValue(c, diffs.get(7).doubleValue(), Skillset.TECHNICAL),
		}));
	}
}
