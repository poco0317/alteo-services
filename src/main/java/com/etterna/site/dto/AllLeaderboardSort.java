package com.etterna.site.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.etterna.services.opensearch.model.HighScoreFullUnion;

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
	
	
	public static Comparator<HighScoreFullUnion> HighScoreWithSkillsetsComparator(AllLeaderboardSort ls) {
		return new Comparator<HighScoreFullUnion>() {
			@Override
			public int compare(HighScoreFullUnion a, HighScoreFullUnion b) {
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
								av = a.getChartUnion().getOverall();
								bv = b.getChartUnion().getOverall();
								break;
							case STREAM:
								av = a.getChartUnion().getStream();
								bv = b.getChartUnion().getStream();
								break;
							case JUMPSTREAM:
								av = a.getChartUnion().getJumpstream();
								bv = b.getChartUnion().getJumpstream();
								break;
							case HANDSTREAM:
								av = a.getChartUnion().getHandstream();
								bv = b.getChartUnion().getHandstream();
								break;
							case STAMINA:
								av = a.getChartUnion().getStamina();
								bv = b.getChartUnion().getStamina();
								break;
							case JACKSPEED:
								av = a.getChartUnion().getJackspeed();
								bv = b.getChartUnion().getJackspeed();
								break;
							case CHORDJACK:
								av = a.getChartUnion().getChordjack();
								bv = b.getChartUnion().getChordjack();
								break;
							case TECHNICAL:
								av = a.getChartUnion().getTechnical();
								bv = b.getChartUnion().getTechnical();
								break;
							default:
								break;
						}
						if (av.equals(bv)) {
							Integer ar = a.getHsUnion().getScore().getMusicRate();
							Integer br = b.getHsUnion().getScore().getMusicRate();
							if (ar == null || br == null || ar.equals(br)) {
								return b.getHsUnion().getScore().getSsrNorm().compareTo(a.getHsUnion().getScore().getSsrNorm());
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
						String ads = a.getHsUnion().getScore().getDateStr();
						String bds = b.getHsUnion().getScore().getDateStr();
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
						Integer ar = a.getHsUnion().getScore().getMusicRate();
						Integer br = b.getHsUnion().getScore().getMusicRate();
						if (ar == null || br == null || ar.equals(br)) {
							return b.getHsUnion().getScore().getSsrNorm().compareTo(a.getHsUnion().getScore().getSsrNorm());
						} else {
							return br.compareTo(ar);
						}
					}
					case PLAYER:
					{
						String an = a.getUser().getUsername();
						String bn = b.getUser().getUsername();
						// opposite direction sort vs the others
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
					}
					case SONG:
					{
						String an = a.getChartUnion().getChart().getTitle();
						String bn = b.getChartUnion().getChart().getTitle();
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					default:
					case PERCENT:
					{
						Integer as = a.getHsUnion().getScore().getSsrNorm();
						Integer bs = b.getHsUnion().getScore().getSsrNorm();
						int o = bs.compareTo(as);
						return o;
					}
					
				}
			}
		};
	}

}
