package com.etterna.site.dto;

public enum ChartLeaderboardSort {
	
	PLAYER,
	PERCENT,
	DATE,
	OVERALL, // default
	STREAM,
	JUMPSTREAM,
	HANDSTREAM,
	STAMINA,
	JACKSPEED,
	CHORDJACK,
	TECHNICAL;
	
	public static ChartLeaderboardSort fromString(String s) {
		final String ss = s.toLowerCase();
		switch (ss) {
			case "player":
				return PLAYER;
			case "percent":
				return PERCENT;
			case "date":
				return DATE;
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
			default:
				return OVERALL;
		}
	}

}
