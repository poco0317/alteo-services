package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.pk.ScoreSpecificValuePk;

@Repository
public interface ScoreSpecificValueRepository extends JpaRepository<ScoreSpecificValue, ScoreSpecificValuePk> {
	
	public List<ScoreSpecificValue> findByIdSkillsetAndCalcVersionLessThan(Skillset skillset, Integer calcVersion);
	public List<ScoreSpecificValue> findByIdScore(HighScore score);
	
}
