package com.etterna.site.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;

public enum AllLeaderboardSort {
	
	PLAYER,
	PERCENT,
	SONG,
	RATE,
	DATE,
	OVERALL, // default
	STREAM,
	JUMPSTREAM,
	HANDSTREAM,
	STAMINA,
	JACKSPEED,
	CHORDJACK,
	TECHNICAL;
	
	public static AllLeaderboardSort fromString(String s) {
		final String ss = s.toLowerCase();
		switch (ss) {
			case "player":
				return PLAYER;
			case "percent":
				return PERCENT;
			case "song":
				return SONG;
			case "rate":
				return RATE;
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
	
	
	public static Comparator<HighScoreWithSkillsets> HighScoreWithSkillsetsComparator(AllLeaderboardSort ls) {
		return new Comparator<HighScoreWithSkillsets>() {
			@Override
			public int compare(HighScoreWithSkillsets a, HighScoreWithSkillsets b) {
				switch (ls) {
					case OVERALL:
					case STREAM:
					case JUMPSTREAM:
					case HANDSTREAM:
					case STAMINA:
					case JACKSPEED:
					case CHORDJACK:
					case TECHNICAL:
					{
						Double av = 0.0;
						Double bv = 0.0;
						switch(ls) {
							case OVERALL:
								av = a.getOverall();
								bv = b.getOverall();
								break;
							case STREAM:
								av = a.getStream();
								bv = b.getStream();
								break;
							case JUMPSTREAM:
								av = a.getJumpstream();
								bv = b.getJumpstream();
								break;
							case HANDSTREAM:
								av = a.getHandstream();
								bv = b.getHandstream();
								break;
							case STAMINA:
								av = a.getStamina();
								bv = b.getStamina();
								break;
							case JACKSPEED:
								av = a.getJackspeed();
								bv = b.getJackspeed();
								break;
							case CHORDJACK:
								av = a.getChordjack();
								bv = b.getChordjack();
								break;
							case TECHNICAL:
								av = a.getTechnical();
								bv = b.getTechnical();
								break;
							default:
								break;
						}
						if (av.equals(bv)) {
							Integer ar = a.getScore().getMusicRate();
							Integer br = b.getScore().getMusicRate();
							if (ar == null || br == null || ar.equals(br)) {
								return b.getScore().getSsrNorm().compareTo(a.getScore().getSsrNorm());
							} else {
								return br.compareTo(ar);
							}
						} else {
							return bv.compareTo(av);
						}
					}
					case DATE:
					{
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String ads = a.getScore().getDateStr();
						String bds = b.getScore().getDateStr();
						try {
							Date ad = f.parse(ads);
							Date bd = f.parse(bds);
							return bd.compareTo(ad);
						} catch (ParseException e) {
							return bds.compareToIgnoreCase(ads);
						}
					}
					case RATE:
					{
						Integer ar = a.getScore().getMusicRate();
						Integer br = b.getScore().getMusicRate();
						if (ar == null || br == null || ar.equals(br)) {
							return b.getScore().getSsrNorm().compareTo(a.getScore().getSsrNorm());
						} else {
							return br.compareTo(ar);
						}
					}
					case PLAYER:
					{
						String an = a.getScore().getUser().getUsername();
						String bn = b.getScore().getUser().getUsername();
						// opposite direction sort vs the others
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
					}
					case SONG:
					{
						String an = a.getScore().getChart().getTitle();
						String bn = b.getScore().getChart().getTitle();
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					default:
					case PERCENT:
					{
						Integer as = a.getScore().getSsrNorm();
						Integer bs = b.getScore().getSsrNorm();
						int o = bs.compareTo(as);
						return o;
					}
					
				}
			}
		};
	}

}
