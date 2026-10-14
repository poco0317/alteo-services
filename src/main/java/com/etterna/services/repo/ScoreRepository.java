package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.services.data.Score;
import com.etterna.services.data.ScoreId;
import com.etterna.services.repo.projection.ScoreProjection;

@Repository
public interface ScoreRepository extends JpaRepository<Score, ScoreId> {
	
	public static final String SELECT_SCORES1 = "select "
			+ "s.scorekey, s.Overall, s.Stream, s.Jumpstream, s.Handstream, s.Stamina, s.JackSpeed, s.Chordjack, s.Technical, s.wifescore, s.maxcombo, s.valid, s.modifiers, s.miss, s.bad, s.good, s.great, s.perfect, s.marv, s.datetime, s.hitmine, s.held, s.letgo, s.missedhold, s.nocc, s.calcversion, s.machineGuid, s.wifeGrade, s.validationKey, s.scoreValue, s.wifeversion, c.difficulty, s.user_chart_rate_rate as rate, s.user_chart_rate_user_chart_chartkey as chartkey, so.id as songid, so.songname, u.username "
			+ "from scores s, users u, song so, charts c where s.user_chart_rate_user_chart_userid = u.userid and u.username = :username and so.songkey = s.user_chart_rate_user_chart_songkey and c.chartkey = s.user_chart_rate_user_chart_chartkey and c.song_songkey = so.songkey ";
	
	public static final String SELECT_SCORES2 = "select "
			+ "s.scorekey, s.overall, s.stream, s.jumpstream, s.handstream, s.stamina, s.jackspeed as jackSpeed, s.chordjack, s.technical, s.wifescore, json_extract(s.score_data, '$.maxcombo') as maxcombo, s.valid, json_extract(s.score_data, '$.modifiers') as modifiers, json_extract(score_data, '$.miss') as miss, json_extract(score_data, '$.bad') as bad, json_extract(score_data, '$.good') as good, json_extract(score_data, '$.great') as great, json_extract(score_data, '$.perfect') as perfect, json_extract(score_data, '$.marv') as marv, s.datetime, "
			+ "json_extract(s.score_data, '$.hitmine') as hitmine, json_extract(s.score_data, '$.held') as held, json_extract(s.score_data, '$.letgo') as letgo, json_extract(s.score_data, '$.missedhold') as missedhold, json_extract(s.score_data, '$.nocc') as nocc, json_extract(s.score_data, '$.calcversion') as calcversion, json_extract(s.score_data, '$.machineguid') as machineGuid, '' as wifeGrade, json_extract(s.score_data, '$.validationkey') as validationKey, '' as scoreValue, '' as wifeversion, s.difficulty, s.rate, s.chartkey, '' as songid, s.songname, u.username "
			+ "from scores2 s, users u where s.userid = u.userid ";  

	@Query(value = SELECT_SCORES1
			+ "order by s.datetime desc",
			nativeQuery = true)
	List<ScoreProjection> getScoresForUser(String username);
	
	@Query(value = "select "
			+ "s.scorekey, s.Overall, s.Stream, s.Jumpstream, s.Handstream, s.Stamina, s.JackSpeed, s.Chordjack, s.Technical, s.wifescore, s.maxcombo, s.valid, s.modifiers, s.miss, s.bad, s.good, s.great, s.perfect, s.marv, s.datetime, s.hitmine, s.held, s.letgo, s.missedhold, s.nocc, s.calcversion, s.machineGuid, s.wifeGrade, s.validationKey, s.scoreValue, s.wifeversion, c.difficulty, s.user_chart_rate_rate as rate, s.user_chart_rate_user_chart_chartkey as chartkey, so.id as songid, so.songname, u.username "
			+ "from scores s, users u, song so, charts c where s.user_chart_rate_user_chart_userid = u.userid and u.username = :username and s.scorekey = :scorekey and so.songkey = s.user_chart_rate_user_chart_songkey and c.chartkey = s.user_chart_rate_user_chart_chartkey and c.song_songkey = so.songkey "
			+ "order by s.datetime desc limit 1",
			nativeQuery = true)
	ScoreProjection getScore(String username, String scorekey);
	
	@Query(value = SELECT_SCORES2
			+ "and u.username = :username "
			+ "order by s.datetime desc",
			nativeQuery = true)
	List<ScoreProjection> getScoresForUser2(String username);
	
	
	@Query(value = SELECT_SCORES2
			+ "order by s.datetime desc limit :perpage offset :offset",
			nativeQuery = true)
	List<ScoreProjection> getAllScoresPagination(int offset, int perpage);
	
	@Query(value = SELECT_SCORES2
			+ "and s.chartkey = :chartkey "
			+ "order by s.Overall desc",
			nativeQuery = true)
	List<ScoreProjection> getScoresForChart(String chartkey);
	
}
