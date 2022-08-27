package com.etterna.calc;

public enum Skillset {
	
	OVERALL,
	STREAM,
	JUMPSTREAM,
	HANDSTREAM,
	STAMINA,
	JACKSPEED,
	CHORDJACK,
	TECHNICAL;
	
	public static Skillset fromEttString(String ss) {
		switch (ss) {
		case "Overall":
			return OVERALL;
		case "Stream":
			return STREAM;
		case "Jumpstream":
			return JUMPSTREAM;
		case "Handstream":
			return HANDSTREAM;
		case "Stamina":
			return STAMINA;
		case "JackSpeed":
			return JACKSPEED;
		case "Chordjack":
			return CHORDJACK;
		case "Technical":
			return TECHNICAL;
		default:
			return OVERALL;
		}
	}
}
