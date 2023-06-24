package com.etterna.site.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.etterna.services.model.HighScore;
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
	
	
	public static Comparator<HighScore> HighScoreComparator(AllLeaderboardSort ls) {
		return new Comparator<HighScore>() {
			@Override
			public int compare(HighScore a, HighScore b) {
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
						Integer ar = a.getMusicRate();
						Integer br = b.getMusicRate();
						if (ar == null || br == null || ar.equals(br)) {
							return b.getSsrNorm().compareTo(a.getSsrNorm());
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
					String ads = a.getDateStr();
					String bds = b.getDateStr();
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
					Integer ar = a.getMusicRate();
					Integer br = b.getMusicRate();
					if (ar == null || br == null || ar.equals(br)) {
						return b.getSsrNorm().compareTo(a.getSsrNorm());
					} else {
						return br.compareTo(ar);
					}
				}
				case PLAYER:
				{
					String an = a.getUsername();
					String bn = b.getUsername();
					// opposite direction sort vs the others
					int o = an.compareToIgnoreCase(bn);
					if (o != 0) {
						return o;
					}
				}
				case SONG:
				default:
				case PERCENT:
				{
					Integer as = a.getSsrNorm();
					Integer bs = b.getSsrNorm();
					int o = bs.compareTo(as);
					return o;
				}
				
			}
		}
		};
	}
	
	public static Comparator<HighScoreFullUnion> HighScoreFullUnionComparator(AllLeaderboardSort ls) {
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
								av = a.getHsUnion().getOverall();
								bv = b.getHsUnion().getOverall();
								break;
							case STREAM:
								av = a.getHsUnion().getStream();
								bv = b.getHsUnion().getStream();
								break;
							case JUMPSTREAM:
								av = a.getHsUnion().getJumpstream();
								bv = b.getHsUnion().getJumpstream();
								break;
							case HANDSTREAM:
								av = a.getHsUnion().getHandstream();
								bv = b.getHsUnion().getHandstream();
								break;
							case STAMINA:
								av = a.getHsUnion().getStamina();
								bv = b.getHsUnion().getStamina();
								break;
							case JACKSPEED:
								av = a.getHsUnion().getJackspeed();
								bv = b.getHsUnion().getJackspeed();
								break;
							case CHORDJACK:
								av = a.getHsUnion().getChordjack();
								bv = b.getHsUnion().getChordjack();
								break;
							case TECHNICAL:
								av = a.getHsUnion().getTechnical();
								bv = b.getHsUnion().getTechnical();
								break;
							default:
								break;
						}
						if (av.equals(bv)) {
							Integer ar = a.getHsUnion().getMusicRate();
							Integer br = b.getHsUnion().getMusicRate();
							if (ar == null || br == null || ar.equals(br)) {
								return b.getHsUnion().getSsrNorm().compareTo(a.getHsUnion().getSsrNorm());
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
						String ads = a.getHsUnion().getDateStr();
						String bds = b.getHsUnion().getDateStr();
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
						Integer ar = a.getHsUnion().getMusicRate();
						Integer br = b.getHsUnion().getMusicRate();
						if (ar == null || br == null || ar.equals(br)) {
							return b.getHsUnion().getSsrNorm().compareTo(a.getHsUnion().getSsrNorm());
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
						Integer as = a.getHsUnion().getSsrNorm();
						Integer bs = b.getHsUnion().getSsrNorm();
						int o = bs.compareTo(as);
						return o;
					}
					
				}
			}
		};
	}

}
