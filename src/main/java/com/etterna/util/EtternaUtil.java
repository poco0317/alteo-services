package com.etterna.util;

public class EtternaUtil {
	
	public static String stepsTypeToColumnCount(String st) {
		switch (st) {
			case "dance-threepanel":
				return "3k";
			case "dance-single":
				return "4k";
			case "pump-single":
			case "pnm-five":
				return "5k";
			case "bm-single5":
			case "dance-solo":
			case "pump-halfdouble":
				return "6k";
			case "kb7-single":
				return "7k";
			case "bm-single7":
			case "dance-double":
			case "dance-couple":
				return "8k";
			case "pnm-nine":
				return "9k";
			case "pump-double":
				return "10k";
			case "bm-double5":
				return "12k";
			case "bm-double7":
				return "16k";
			default:
				return st;
		}
	}

}
