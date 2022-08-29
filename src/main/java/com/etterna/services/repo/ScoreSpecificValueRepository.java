package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.pk.ScoreSpecificValuePk;

@Repository
public interface ScoreSpecificValueRepository extends JpaRepository<ScoreSpecificValue, ScoreSpecificValuePk> {
	
	public Long deleteByIdScore(HighScore score);
	public Long deleteByIdCalcVersionLessThan(Integer calcVersion);
	
	public List<ScoreSpecificValue> findByIdScoreAndIdCalcVersion(HighScore score, Integer calcVersion);
}
