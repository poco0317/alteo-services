package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.User;

@Repository
public interface HighScoreRepository extends JpaRepository<HighScore, String> {

	List<HighScore> findByCalcVersionLessThan(Integer calcVersion);
	List<HighScore> findByChartChartKey(String chartkey);
	List<HighScore> findByUser(User user);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv "
			+ "WHERE ssv.id.calcVersion = :calcVersion AND "
			+ "ssv.id.score = hs AND "
			+ "hs.chart = :chart")
	List<Object[]> findScoresByChartOnAllRates(Chart chart, Integer calcVersion);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv "
			+ "WHERE ssv.id.calcVersion = :calcVersion AND "
			+ "ssv.id.score = hs AND "
			+ "hs.chart = :chart AND "
			+ "hs.musicRate = :rate")
	List<Object[]> findScoresByChartOnRate(Chart chart, Integer rate, Integer calcVersion);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv "
			+ "WHERE ssv.id.calcVersion = :calcVersion AND "
			+ "ssv.id.score = hs")
	List<Object[]> findScoresOnAllChartsOnAllRates(Integer calcVersion);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv "
			+ "WHERE ssv.id.calcVersion = :calcVersion AND "
			+ "ssv.id.score = hs AND "
			+ "hs.musicRate = :rate")
	List<Object[]> findScoresOnAllChartsOnRate(Integer rate, Integer calcVersion);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv WHERE ssv.id.calcVersion = :calcVersion AND ssv.id.score = hs AND hs.user = :user and ssv.id.skillset = :ss")
	List<Object[]> findUserScoresWithSpecificSkillsetValue(User user, Integer calcVersion, Skillset ss);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv WHERE ssv.id.calcVersion = :calcVersion AND ssv.id.score = hs AND hs.user = :user")
	List<Object[]> findUserScoresWithSkillsets(User user, Integer calcVersion);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv WHERE ssv.id.calcVersion = :calcVersion AND ssv.id.score = hs AND hs.scoreKey = :scoreKey")
	List<Object[]> findScoreWithSkillsets(String scoreKey, Integer calcVersion);
	
	@Query("SELECT hs FROM HighScore hs WHERE hs.calcVersion <> :calcVersion and hs.manuallyInvalid = false and hs.noCC = true and hs.ssrNorm is not null and hs.musicRate is not null")
	List<HighScore> findRecalculableScores(Integer calcVersion);
	
	@Query("SELECT hs FROM HighScore hs WHERE hs.calcVersion <> :calcVersion and hs.manuallyInvalid = false and hs.noCC = true and hs.ssrNorm is not null and hs.musicRate is not null and hs.user = :user")
	List<HighScore> findUserRecalculableScores(User user, Integer calcVersion);
	
	@Query("SELECT hs FROM HighScore hs WHERE hs.calcVersion <> :calcVersion and (hs.manuallyInvalid = true or hs.noCC = false or hs.ssrNorm is null or hs.musicRate is null) and hs.user = :user")
	List<HighScore> findUserIncalculableScores(User user, Integer calcVersion);
	
	@Query("SELECT DISTINCT hs.musicRate FROM HighScore hs WHERE hs.chart = :chart")
	List<Integer> findRatesUsedOnChart(Chart chart);
	
	@Query("SELECT DISTINCT hs.musicRate FROM HighScore hs")
	List<Integer> findAllRates();
}
