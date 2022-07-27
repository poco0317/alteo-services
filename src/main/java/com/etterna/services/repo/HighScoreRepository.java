package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.HighScore;

@Repository
public interface HighScoreRepository extends JpaRepository<HighScore, String> {

	List<HighScore> findByCalcVersionLessThan(Integer calcVersion);
	List<HighScore> findByChartChartKey(String chartkey);
	
}
