package com.etterna.site.dto;

public enum PackContentSort {
	
	NAME, // default
	SCORES,
	OVERALL,
	STREAM,
	JUMPSTREAM,
	HANDSTREAM,
	STAMINA,
	JACKSPEED,
	CHORDJACK,
	TECHNICAL;
	
	public static PackContentSort fromString(String s) {
		switch (s.toLowerCase()) {
			case "name":
			default:
				return NAME;
			case "scores":
				return SCORES;
			case "overall":
				return OVERALL;
			case "stream":
				return STREAM;
			case "jumpstream":
				return JUMPSTREAM;
			case "handstream":
				return HANDSTREAM;
			case "stamina":
				return STAMINA;
			case "jackspeed":
				return JACKSPEED;
			case "chordjack":
				return CHORDJACK;
			case "technical":
				return TECHNICAL;
		}
	}

}
