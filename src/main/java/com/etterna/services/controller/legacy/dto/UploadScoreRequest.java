package com.etterna.services.controller.legacy.dto;

import com.etterna.services.data.HighScore;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UploadScoreRequest {

	private String scorekey;
	private String ssr_norm;
	private String max_combo;
	private String valid;
	private String mods;
	
	private String miss;
	private String bad;
	private String good;
	private String great;
	private String perfect;
	private String marv;
	
	private String datetime;
	private String hitmine;
	private String held;
	private String letgo;
	private String ng;
	private String chartkey;
	private String rate;
	private String negsolo;
	private String nocc;
	private String calc_version;
	private String wife_version;
	private String topscore;
	private String hash;
	private String wife;
	private String wifePoints;
	private String judgeScale;
	private String machineGuid;
	private String grade;
	private String wifeGrade;
	private String replay_data;
	
	public HighScore toHighScore(String username, int calcVersion) {
		HighScore hs = new HighScore();
		hs.setBadCount(Integer.parseInt(getBad()));
		hs.setBrittleKey(getHash());
		hs.setCalcVersion(calcVersion); // set 0 to cause a recalc later
		hs.setChartKey(getChartkey());
		hs.setDateStr(getDatetime());
		hs.setEtternaValid(Integer.parseInt(getValid()));
		hs.setGoodCount(Integer.parseInt(getGood()));
		hs.setGrade(getGrade());
		hs.setGreatCount(Integer.parseInt(getGreat()));
		hs.setGuid(getMachineGuid());
		hs.setHeldCount(Integer.parseInt(getHeld()));
		hs.setHitMineCount(Integer.parseInt(getHitmine()));
		hs.setJudgeScale(Double.parseDouble(getJudgeScale()));
		hs.setLetgoCount(Integer.parseInt(getLetgo()));
		hs.setMarvCount(Integer.parseInt(getMarv()));
		hs.setMaxCombo(Integer.parseInt(getMax_combo()));
		hs.setMissCount(Integer.parseInt(getMiss()));
		hs.setModString(getMods());
		hs.setMusicRate(Integer.parseInt(getRate()));
		hs.setNegBpm("1".equals(getNegsolo()));
		hs.setNgCount(Integer.parseInt(getNg()));
		hs.setNoCC("1".equals(getNocc()));
		hs.setPerfCount(Integer.parseInt(getPerfect()));
		hs.setScoreKey(getScorekey());
		hs.setSsrNorm(Integer.parseInt(getSsr_norm()));
		hs.setTopScore(Integer.parseInt(getTopscore()));
		hs.setUsername(username);
		hs.setWifeGrade(getWifeGrade());
		hs.setWifePercent(Double.parseDouble(getWife()));
		hs.setWifePoints(Double.parseDouble(getWifePoints()));
		hs.setWifeVersion(Integer.parseInt(getWife_version()));
		return hs;
	}
}
