package com.etterna.site.dto;

public enum PacksSort {
	
	NAME, // default
	COUNT,
	SCORES;

	public static PacksSort fromString(String s) {
		switch (s.toLowerCase()) {
			case "name":
			default:
				return NAME;
			case "count":
				return COUNT;
			case "scores":
				return SCORES;
		}
	}
}
