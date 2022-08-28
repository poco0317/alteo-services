package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.User;

@Repository
public interface HighScoreRepository extends JpaRepository<HighScore, String> {

	List<HighScore> findByCalcVersionLessThan(Integer calcVersion);
	List<HighScore> findByChartChartKey(String chartkey);
	List<HighScore> findByUser(User user);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv WHERE ssv.calcVersion = :calcVersion AND ssv.id.score = hs AND hs.user = :user and ssv.id.skillset = :ss")
	List<Object[]> findScoreWithSkillsetValue(User user, Integer calcVersion, Skillset ss);
	
	@Query("SELECT hs, ssv FROM HighScore hs, ScoreSpecificValue ssv WHERE ssv.calcVersion = :calcVersion AND ssv.id.score = hs AND hs.user = :user")
	List<Object[]> findScoreWithAllSkillsets(User user, Integer calcVersion);
}
