package com.etterna.services.repo.projection;

import java.sql.Timestamp;

public interface ScoreReplayProjection {
	
	String getReplay();
	String getScorekey();
	Timestamp getDatetime();

}
