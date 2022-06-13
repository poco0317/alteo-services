package com.etterna.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.etterna.calc.jni.MinaCalcJNI;

@Service
public class CalcManager {
	
	private static MinaCalcJNI minacalc;
	
	@PostConstruct
	private void init() {
		minacalc = new MinaCalcJNI();
	}
	
	public String getCalcVersion() {
		return minacalc.getCalcVersion();
	}
	
	public List<Float> calc() {
		String path = "C:\\Users\\Barinade\\Desktop\\Xfb7680d6e93f66c121cf731e9e3b92a757c36ea2.cache";
		float[] ssrs = minacalc.minaSDCalc(path, 1.0f, 0.93f);
		System.out.println(Arrays.toString(ssrs));
		List<Float> o = new ArrayList<>(ssrs.length);
		for (float f : ssrs) {
			o.add(f);
		}
		return o;
	}

}
