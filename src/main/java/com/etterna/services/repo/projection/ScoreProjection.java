package com.etterna.services.repo.projection;

import java.sql.Timestamp;

public interface ScoreProjection {
	
	String getScorekey();
	Float getOverall();
	Float getStream();
	Float getJumpstream();
	Float getHandstream();
	Float getStamina();
	Float getJackSpeed();
	Float getChordjack();
	Float getTechnical();
	Float getWifescore();
	Long getMaxcombo();
	Boolean getValid();
	String getModifiers();
	Long getMiss();
	Long getBad();
	Long getGood();
	Long getGreat();
	Long getPerfect();
	Long getMarv();
	Timestamp getDatetime();
	Long getHitmine();
	Long getHeld();
	Long getLetgo();
	Long getMissedhold();
	Integer getNocc();
	Integer getCalcversion();
	String getMachineGuid();
	String getWifeGrade();
	String getValidationKey();
	Float getScoreValue();
	Long getWifeversion();
	
	String getDifficulty();
	
	Float getRate();
	String getChartkey();
	Long getSongid();
	String getSongname();
	String getUsername();

}
