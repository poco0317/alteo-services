package com.etterna.services.opensearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.model.Chart;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.ScoreSpecificValue;
import com.etterna.services.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HighScoreIndexService extends BaseIndexService<HighScore> {

	@Autowired
	private ScoreSpecificValueIndexService ssrIndex;
	
	@Override
	public String INDEX_NAME() {
		return "highscore";
	}
	
	@Override
	public Class<HighScore> getClazz() {
		return HighScore.class;
	}

	public List<HighScore> findByUser(User user) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("username").query(fv -> fv.stringValue(user.getUsername()))));
		return searchDocuments(req);
	}

	public List<HighScore> findByChartKey(String chartkey) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(chartkey))));
		return searchDocuments(req);
	}

	public List<HighScore> findUserRecalculableScores(User u, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("manuallyInvalid").query(fv -> fv.booleanValue(false)))
						.build(),
					new Query.Builder().match(mq -> mq.field("noCC").query(fv -> fv.booleanValue(true)))
						.build(),
					new Query.Builder().exists(eq -> eq.field("musicRate"))
						.build(),
					new Query.Builder().exists(eq -> eq.field("ssrNorm"))
						.build(),
					new Query.Builder().match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername())))
						.build()
				).mustNot(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion)))
						.build())
				));
		return searchDocuments(req);
	}

	public List<HighScoreWithSkillsets> findUserScoresWithSkillsets(User u, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build(),
					new Query.Builder().match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername()))).build()
				)
				));
		return getSSRsForScores(searchDocuments(req), calcVersion);
	}

	public List<Integer> findAllRates() {
		SearchRequest.Builder req = new SearchRequest.Builder().aggregations("rates", agg -> agg.terms(ta -> ta.field("musicRate")));
		Map<String, Aggregate> aggs = searchInternal(req, null).aggregations();
		if (!aggs.get("rates").isLterms()) {
			return new ArrayList<>();
		}
		return aggs.get("rates").lterms().buckets().array().stream().map(b -> Integer.parseInt(b.key())).collect(Collectors.toList());
	}

	public List<HighScoreWithSkillsets> findScoresOnAllChartsOnRate(int rate, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("musicRate").query(fv -> fv.longValue(rate))).build(),
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
				)
				));
		
		return getSSRsForScores(searchDocuments(req), calcVersion);
	}

	public List<HighScoreWithSkillsets> findScoresOnAllChartsOnAllRates(int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
				)
				));
		return getSSRsForScores(searchDocuments(req), calcVersion);
	}

	public List<HighScore> findUserIncalculableScores(User u, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername()))))
				.mustNot(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
				.must(andmq -> andmq.bool(bqqq -> bqqq
					.should(qq -> qq.match(mq -> mq.field("manuallyInvalid").query(fv -> fv.booleanValue(true))))
					.should(qq -> qq.match(mq -> mq.field("noCC").query(fv -> fv.booleanValue(false))))
					.should(qq -> qq.bool(bq2 -> bq2.mustNot(qqq -> qqq.exists(eq -> eq.field("ssrNorm")))))
					.should(qq -> qq.bool(bq2 -> bq2.mustNot(qqq -> qqq.exists(eq -> eq.field("musicRate")))))
				))));
		return searchDocuments(req);
	}

	public List<Integer> findRatesUsedOnChart(Chart c) {
		SearchRequest.Builder req = new SearchRequest.Builder()
				.aggregations("rates", agg -> agg.terms(ta -> ta.field("musicRate")))
				.query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))));
		Map<String, Aggregate> aggs = searchInternal(req, null).aggregations();
		return aggs.get("rates").lterms().buckets().array().stream().map(b -> Integer.parseInt(b.key())).collect(Collectors.toList());
	}

	public List<HighScoreWithSkillsets> findScoresByChartOnAllRates(Chart c, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))))
				.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
				));
		return getSSRsForScores(searchDocuments(req), calcVersion);
	}

	public List<HighScoreWithSkillsets> findScoresByChartOnRate(Chart c, int rate, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))))
				.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
				.must(qq -> qq.match(mq -> mq.field("musicRate").query(fv -> fv.longValue(rate))))
				));
		return getSSRsForScores(searchDocuments(req), calcVersion);
	}

	public List<HighScoreWithSkillsets> findUserScoresWithSpecificSkillsetValue(User u, int calcVersion, Skillset ss) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
				)
				));
		return getSSRsForScores(searchDocuments(req), calcVersion, ss);
	}

	public HighScoreWithSkillsets findScoreWithSkillsets(String scorekey, int calcVersion) {
		HighScore hs = findById(scorekey);
		List<HighScore> hses = new ArrayList<>();
		if (hs != null) {
			hses.add(hs);
		}
		List<HighScoreWithSkillsets> o = getSSRsForScores(hses, calcVersion, null);
		if (o == null || o.isEmpty()) {
			return null;
		}
		return o.get(0);
	}

	public List<HighScore> findRecalculableScores(int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("manuallyInvalid").query(fv -> fv.booleanValue(false)))
						.build(),
					new Query.Builder().match(mq -> mq.field("noCC").query(fv -> fv.booleanValue(true)))
						.build(),
					new Query.Builder().exists(eq -> eq.field("musicRate"))
						.build(),
					new Query.Builder().exists(eq -> eq.field("ssrNorm"))
						.build()
				).mustNot(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion)))
						.build())
				));
		return searchDocuments(req);
	}
	
	private List<HighScoreWithSkillsets> getSSRsForScores(List<HighScore> hses, Integer calcVersion) {
		return getSSRsForScores(hses, calcVersion, null);
	}
	/**
	 * returns List<{HighScore, ScoreSpecificValue}>
	 */
	private List<HighScoreWithSkillsets> getSSRsForScores(List<HighScore> hses, Integer calcVersion, Skillset ss) {
		Map<String, HighScore> hsmap = hses.stream().collect(Collectors.toMap(hs -> hs.getScoreKey(), hs -> hs));
		SearchRequest.Builder ssrReq;
		if (calcVersion != null && ss == null) {
			List<FieldValue> fvs = hses.stream().map(hs -> new FieldValue.Builder().stringValue(hs.getScoreKey()).build()).collect(Collectors.toList());
			ssrReq = new SearchRequest.Builder()
					.query(q -> q.bool(bq -> bq
						.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
						.should(qq -> qq.terms(tq -> tq.field("scoreKey").terms(qf -> qf.value(fvs))))
					));
		} else if (calcVersion != null && ss != null) {
			String sks = String.join(" ", hsmap.keySet());
			ssrReq = new SearchRequest.Builder()
					.query(q -> q.bool(bq -> bq
						.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
						.must(qq -> qq.match(mq -> mq.field("scoreKey").query(fv -> fv.stringValue(sks))))
						.must(qq -> qq.match(mq -> mq.field("skillset").query(fv -> fv.longValue(ss.ordinal()))))
					));
		} else {
			List<FieldValue> fvs = hses.stream().map(hs -> new FieldValue.Builder().stringValue(hs.getScoreKey()).build()).collect(Collectors.toList());
			ssrReq = new SearchRequest.Builder()
					.query(q -> q
							.terms(tq -> tq.field("scoreKey").terms(qf -> qf.value(fvs))));	
		}
		
		List<ScoreSpecificValue> ssrs = ssrIndex.searchDocuments(ssrReq);
		Map<String, Set<ScoreSpecificValue>> ssrsCollected = new HashMap<>();
		for (ScoreSpecificValue ssr : ssrs) {
			if (!ssrsCollected.containsKey(ssr.getScoreKey())) {
				ssrsCollected.put(ssr.getScoreKey(), new HashSet<>());
			}
			ssrsCollected.get(ssr.getScoreKey()).add(ssr);
		}
		
		return hsmap.keySet().stream().map(sk -> new HighScoreWithSkillsets(hsmap.get(sk), ssrsCollected.get(sk))).collect(Collectors.toList());
	}

}
