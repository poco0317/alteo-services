package com.etterna.calc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.etterna.calc.jni.MinaCalcJNI;

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
		return Paths.get(noteInfoFolder, chartKey);
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
		return o;
	}

}
