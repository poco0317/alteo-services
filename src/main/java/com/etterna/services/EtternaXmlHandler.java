package com.etterna.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.RankingDao;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.User;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
@Getter @Setter
public class EtternaXmlHandler extends DefaultHandler {
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private RankingDao chartRanking;
	
	private StringBuilder currentValue = new StringBuilder();
	private List<HighScore> highscores;
	private HighScore currentHighScore;
	private Chart currentChart;
	
	private boolean inJudgments = false;
	private boolean inHolds = false;
	private Integer currentRate = 100;
	private Integer ngcount = 0;
	private Integer lgcount = 0;
	
	private User user;
	
	// xml elements
	private static final String CHART = "Chart";
	private static final String SCORESAT = "ScoresAt";
	private static final String SCORE = "Score";
	
	// hs elements
	private static final String DATE = "DateTime";
	private static final String ETTERNAVALID = "EtternaValid";
	private static final String GRADE = "Grade";
	private static final String GUID = "MachineGuid";
	private static final String JUDGE = "JudgeScale";
	private static final String COMBO = "MaxCombo";
	private static final String MODS = "Modifiers";
	private static final String NOCC = "NoChordCohesion";
	private static final String SSRNORM = "SSRNormPercent";
	private static final String TOPSCORE = "TopScore";
	private static final String WIFEPERCENT = "WifeScore";
	private static final String WIFEVER = "wv";
	private static final String WIFEPOINTS = "WifePoints";
	private static final String JUDGMENTS = "TapNoteScores";
	private static final String MARV = "W1";
	private static final String PERF = "W2";
	private static final String GREAT = "W3";
	private static final String GOOD = "W4";
	private static final String BAD = "W5";
	private static final String MISS = "Miss";
	private static final String HITMINE = "HitMine";
	private static final String HOLDS = "HoldNoteScores";
	private static final String HELD = "Held";
	private static final String HOLDMISSED = "MissedHold";
	private static final String LETGO = "LetGo";
	
	@Override
	public void startDocument() {
		highscores = new ArrayList<>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		currentValue.setLength(0);
		
		switch (qName) {
			case CHART: {
				currentChart = null;
				String chartkey = attributes.getValue("Key");
				if (chartRanking.isRanked(chartkey)) {
					currentChart = charts.get(chartkey);
				}
				break;
			}
			case SCORESAT: {
				if (currentChart == null) return;
				currentRate = Math.round(100.f * Float.parseFloat(attributes.getValue("Rate")));
				break;
			}
			case SCORE: {
				if (currentChart == null) return;
				String scoreKey = attributes.getValue("Key");
				currentHighScore = new HighScore();
				currentHighScore.setMusicRate(currentRate);
				currentHighScore.setChart(currentChart);
				currentHighScore.setCalcVersion(0);
				currentHighScore.setManuallyInvalid(false);
				currentHighScore.setNegBpm(null);
				currentHighScore.setScoreKey(scoreKey);
				currentHighScore.setUser(user);
				currentHighScore.setWifeGrade(null);
				break;
			}
			case JUDGMENTS: {
				inJudgments = true;
				break;
			}
			case HOLDS: {
				inHolds = true;
				break;
			}
			default:
				break;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		if (currentChart == null || currentHighScore == null) return;
		
		switch (qName) {
			case SCORE: {
				int ngtotal = 0;
				int lgtotal = 0;
				if (ngcount != null) {
					ngtotal += ngcount;
				}
				if (lgcount != null) {
					lgtotal += lgcount;
				}
				currentHighScore.setNgCount(ngtotal);
				currentHighScore.setLetgoCount(lgtotal);
				
				highscores.add(currentHighScore);
				break;
			}
			case DATE: {
				currentHighScore.setDateStr(currentValue.toString());
				break;
			}
			case ETTERNAVALID: {
				currentHighScore.setEtternaValid(nullint(currentValue.toString()));
				break;
			}
			case GRADE: {
				currentHighScore.setGrade(currentValue.toString());
				break;
			}
			case GUID: {
				currentHighScore.setGuid(currentValue.toString());
				break;
			}
			case JUDGE: {
				currentHighScore.setJudgeScale(nulldouble(currentValue.toString()));
				break;
			}
			case COMBO: {
				currentHighScore.setMaxCombo(nullint(currentValue.toString()));
				break;
			}
			case MODS: {
				currentHighScore.setModString(currentValue.toString());
				break;
			}
			case NOCC: {
				currentHighScore.setNoCC("1".equals(currentValue.toString()));
				break;
			}
			case SSRNORM: {
				Double ssrnorm = nulldouble(currentValue.toString());
				if (ssrnorm != null) {
					currentHighScore.setSsrNorm((int)Math.round(1000000.0 * ssrnorm));
				} else {
					currentHighScore.setSsrNorm(null);
				}
				break;
			}
			case TOPSCORE: {
				currentHighScore.setTopScore(nullint(currentValue.toString()));
				break;
			}
			case WIFEVER: {
				currentHighScore.setWifeVersion(nullint(currentValue.toString()));
				break;
			}
			case WIFEPERCENT: {
				currentHighScore.setWifePercent(nulldouble(currentValue.toString()));
				break;
			}
			case WIFEPOINTS: {
				currentHighScore.setWifePoints(nulldouble(currentValue.toString()));
				break;
			}
			case JUDGMENTS: {
				inJudgments = false;
				break;
			}
			case MARV: {
				if (!inJudgments) return;
				currentHighScore.setMarvCount(nullint(currentValue.toString()));
				break;
			}
			case PERF: {
				if (!inJudgments) return;
				currentHighScore.setPerfCount(nullint(currentValue.toString()));
				break;
			}
			case GREAT: {
				if (!inJudgments) return;
				currentHighScore.setGreatCount(nullint(currentValue.toString()));
				break;
			}
			case GOOD: {
				if (!inJudgments) return;
				currentHighScore.setGoodCount(nullint(currentValue.toString()));
				break;
			}
			case BAD: {
				if (!inJudgments) return;
				currentHighScore.setBadCount(nullint(currentValue.toString()));
				break;
			}
			case MISS: {
				if (!inJudgments) return;
				currentHighScore.setMissCount(nullint(currentValue.toString()));
				break;
			}
			case HITMINE: {
				if (!inJudgments) return;
				currentHighScore.setHitMineCount(nullint(currentValue.toString()));
				break;
			}
			case HOLDS: {
				inHolds = false;
				break;
			}
			case HELD: {
				if (!inHolds) return;
				currentHighScore.setHeldCount(nullint(currentValue.toString()));
				break;
			}
			case HOLDMISSED: {
				if (!inHolds) return;
				ngcount = nullint(currentValue.toString());
				break;
			}
			case LETGO: {
				if (!inHolds) return;
				lgcount = nullint(currentValue.toString());
				break;
			}
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) {
		currentValue.append(ch, start, length);
	}

	private Integer nullint(String txt) {
		if (txt == null || txt.isEmpty())
			return null;
		return Integer.parseInt(txt);
	}
	
	private Double nulldouble(String txt) {
		if (txt == null || txt.isEmpty())
			return null;
		return Double.parseDouble(txt);
	}
}
