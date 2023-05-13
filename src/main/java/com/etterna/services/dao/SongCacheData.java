package com.etterna.services.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongCacheData {

	public class ChartCacheData {
		private String chartkey;
		private String difficulty;
		private String stepstype;
		public ChartCacheData(String ck, String diff, String st) {
			chartkey = ck;
			difficulty = diff;
			stepstype = st;
		}
		public String getChartkey() {
			return chartkey;
		}
		public String getDifficulty() {
			return difficulty;
		}
		public String getStepstype() {
			return stepstype;
		}
		
	}
	
	private String title;
	private String translitTitle;
	private String subtitle;
	private String translitSubtitle;
	private String artist;
	private String translitArtist;
	private String credit;
	private List<ChartCacheData> charts = new LinkedList<>();
	
	private String data;
	
	public SongCacheData(String data) {
		this.data = data;
		title = getfirst("TITLE");
		translitTitle = getfirst("TITLETRANSLIT");
		subtitle = getfirst("SUBTITLE");
		translitSubtitle = getfirst("SUBTITLETRANSLIT");
		artist = getfirst("ARTIST");
		translitArtist = getfirst("ARTISTTRANSLIT");
		credit = getfirst("CREDIT");
		
		List<String> cks = getall("CHARTKEY");
		List<String> diffs = getall("DIFFICULTY");
		List<String> sts = getall("STEPSTYPE");
		if (cks.size() == diffs.size() && diffs.size() == sts.size()) {
			for (int i = 0; i < cks.size(); i++) {
				charts.add(new ChartCacheData(cks.get(i), diffs.get(i), sts.get(i)));
			}
		}
	}
	
	private Matcher msd(final String name) {
		return Pattern.compile(";?[\\s]*#"+name+":([^;]+);").matcher(data);
	}
	
	private String getfirst(final String name) {
		final Matcher m = msd(name);
		if (m.find())
			return m.group(1);
		return null;
	}
	
	private List<String> getall(final String name) {
		final Matcher m = msd(name);
		List<String> o = new LinkedList<>();
		while (m.find()) {
			o.add(m.group(1));
		}
		return o;
	}

	public String getTitle() {
		return title;
	}

	public String getTranslitTitle() {
		return translitTitle;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public String getTranslitSubtitle() {
		return translitSubtitle;
	}

	public String getArtist() {
		return artist;
	}

	public String getTranslitArtist() {
		return translitArtist;
	}

	public String getCredit() {
		return credit;
	}

	public List<ChartCacheData> getCharts() {
		return charts;
	}

	public String getData() {
		return data;
	}

	
}
