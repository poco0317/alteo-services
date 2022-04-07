package com.etterna.calc;

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
	
	public String dothing() {
		return minacalc.test();
	}

}
