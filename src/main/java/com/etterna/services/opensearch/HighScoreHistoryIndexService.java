package com.etterna.services.opensearch;

import org.springframework.stereotype.Service;

import com.etterna.services.model.HighScore;

import lombok.extern.slf4j.Slf4j;

/**
 * There is a class made for this, HighScoreHistory, which is not used in the generic type.
 * This is because HighScore is sufficient and we dont want to override some logic...
 *
 */
@Service
@Slf4j
public class HighScoreHistoryIndexService extends BaseIndexService<HighScore> {
	
	@Override
	public String INDEX_NAME() {
		return "highscore-history";
	}

	@Override
	public Class<HighScore> getClazz() {
		return HighScore.class;
	}
	
}
