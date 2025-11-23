package com.etterna.services.opensearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

import com.etterna.calc.Skillset;
import com.etterna.services.model.Chart;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.User;
import com.etterna.services.opensearch.model.HighScoreCollection;
import com.etterna.site.dto.AllLeaderboardSort;
import com.etterna.site.dto.ProfileSort;
import com.etterna.util.LogRuntime;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HighScoreIndexService extends BaseIndexService<HighScore> {
	
	@Override
	public String INDEX_NAME() {
		return "highscore";
	}
	
	@Override
	public Class<HighScore> getClazz() {
		return HighScore.class;
	}

	@LogRuntime
	public List<HighScore> findByUser(User user) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("username").query(fv -> fv.stringValue(user.getUsername()))));
		return searchDocuments(req);
	}

	@LogRuntime
	public List<HighScore> findByChartKey(String chartkey) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(chartkey))));
		return searchDocuments(req);
	}
	
	@LogRuntime
	public List<HighScore> findByChartKeySortedBySkillset(String chartkey, Skillset skillset) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(chartkey))))))
				.sort(sopt -> {
					return sopt.field(fs -> fs.field(skillset.name().toLowerCase()));
				});
		return searchDocuments(req);
	}
	
	public List<HighScore> findScoresMissingChartMetadata() {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.mustNot(qq -> qq.exists(eq -> eq.field("songTitle")))
				.mustNot(qq -> qq.exists(eq -> eq.field("songArtist")))
				.mustNot(qq -> qq.exists(eq -> eq.field("songCredit")))));
		return searchDocuments(req);
	}

	@LogRuntime
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

	@LogRuntime
	public List<HighScore> findUserScoresSortedBySkillsets(User u, int calcVersion, ProfileSort ps) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build(),
					new Query.Builder().match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername()))).build()
				)
				));
		return searchDocuments(req);
	}

	@LogRuntime
	public List<Integer> findAllRates() {
		SearchRequest.Builder req = new SearchRequest.Builder().aggregations("rates", agg -> agg.terms(ta -> ta.field("musicRate")));
		Map<String, Aggregate> aggs = searchInternal(req, null).aggregations();
		if (!aggs.get("rates").isLterms()) {
			return new ArrayList<>();
		}
		return aggs.get("rates").lterms().buckets().array().stream().map(b -> Integer.parseInt(b.key())).collect(Collectors.toList());
	}

	@LogRuntime
	public HighScoreCollection findScoresOnAllChartsOnRate(int rate, int calcVersion, AllLeaderboardSort ls, int page, int itemsPerPage) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q
				.bool(bq -> bq
					.must(
						new Query.Builder().match(mq -> mq.field("musicRate").query(fv -> fv.longValue(rate))).build(),
						new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
					)
				))
				.aggregations("count", agg -> agg.valueCount(vc -> vc.field("scoreKey.keyword")))
				//.from((page-1)* itemsPerPage)
				//.size(itemsPerPage)
				
				// primary sort order
				.sort(so -> {
					FieldSort.Builder fsbuilder = null;
					switch (ls) {
						case PLAYER: {
							fsbuilder = new FieldSort.Builder().field("username.keyword");
							break;
						}
						case PERCENT: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
						case RATE: {
							fsbuilder = new FieldSort.Builder().field("musicRate");
							break;
						}
						case DATE: {
							fsbuilder = new FieldSort.Builder().field("dateStr.keyword");
							break;
						}
						case SONG: {
							fsbuilder = new FieldSort.Builder().field("songTitle.keyword");
							break;
						}
						case OVERALL:
						case STREAM:
						case JUMPSTREAM:
						case HANDSTREAM:
						case STAMINA:
						case JACKSPEED:
						case CHORDJACK:
						case TECHNICAL: {
							fsbuilder = new FieldSort.Builder().field(ls.name().toLowerCase());
							break;
						}
						default: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
					}
					
					if (fsbuilder != null) {
						if (ls != AllLeaderboardSort.SONG) {
							fsbuilder.order(SortOrder.Desc);
						} else {
							fsbuilder.order(SortOrder.Asc);
						}
						so.field(fsbuilder.build());
					}
					
					return so;
				})
				
				// fallback sort order
				.sort(so -> {
					FieldSort.Builder fsbuilder = null;
					switch (ls) {
						case PLAYER: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
						case PERCENT: {
							// scuffed fallback
							fsbuilder = new FieldSort.Builder().field("dateStr.keyword");
							break;
						}
						case RATE: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
						case DATE: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
						case OVERALL:
						case STREAM:
						case JUMPSTREAM:
						case HANDSTREAM:
						case STAMINA:
						case JACKSPEED:
						case CHORDJACK:
						case TECHNICAL: {
							fsbuilder = new FieldSort.Builder().field("musicRate");
							break;
						}
						case SONG:
						default: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
					}
					
					if (fsbuilder != null) {
						so.field(fsbuilder.order(SortOrder.Desc).build());
					}
					
					return so;
				});
		
		// TODO this fails after 10000 results
		//SearchResponse<HighScore> resp = searchInternal(req.index(INDEX_NAME()).build());
		//long count = (long)resp.aggregations().get("count").valueCount().value();
		//return new HighScoreCollection(hits(resp), count);
		return new HighScoreCollection(searchDocuments(req));
	}

	@LogRuntime
	public HighScoreCollection findScoresOnAllChartsOnAllRates(int calcVersion, AllLeaderboardSort ls, int page, int itemsPerPage) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q
				.bool(bq -> bq
					.must(
						new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
					)
				))
				.aggregations("count", agg -> agg.valueCount(vc -> vc.field("scoreKey.keyword")))
				//.from((page-1) * itemsPerPage)
				//.size(itemsPerPage)
				
				// primary sort order
				.sort(so -> {
					FieldSort.Builder fsbuilder = null;
					switch (ls) {
						case PLAYER: {
							fsbuilder = new FieldSort.Builder().field("username.keyword");
							break;
						}
						case PERCENT: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
						case RATE: {
							fsbuilder = new FieldSort.Builder().field("musicRate");
							break;
						}
						case DATE: {
							fsbuilder = new FieldSort.Builder().field("dateStr.keyword");
							break;
						}
						case SONG: {
							fsbuilder = new FieldSort.Builder().field("songTitle.keyword");
							break;
						}
						case OVERALL:
						case STREAM:
						case JUMPSTREAM:
						case HANDSTREAM:
						case STAMINA:
						case JACKSPEED:
						case CHORDJACK:
						case TECHNICAL: {
							fsbuilder = new FieldSort.Builder().field(ls.name().toLowerCase());
							break;
						}
						default: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
					}
					
					if (fsbuilder != null) {
						if (ls != AllLeaderboardSort.SONG) {
							fsbuilder.order(SortOrder.Desc);
						} else {
							fsbuilder.order(SortOrder.Asc);
						}
						so.field(fsbuilder.build());
					}
					
					return so;
				})
				
				// fallback sort order
				.sort(so -> {
					FieldSort.Builder fsbuilder = null;
					switch (ls) {
						case PLAYER: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
						case PERCENT: {
							// scuffed fallback
							fsbuilder = new FieldSort.Builder().field("dateStr.keyword");
							break;
						}
						case RATE: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
						case DATE: {
							fsbuilder = new FieldSort.Builder().field("overall");
							break;
						}
						case OVERALL:
						case STREAM:
						case JUMPSTREAM:
						case HANDSTREAM:
						case STAMINA:
						case JACKSPEED:
						case CHORDJACK:
						case TECHNICAL: {
							fsbuilder = new FieldSort.Builder().field("musicRate");
							break;
						}
						case SONG:
						default: {
							fsbuilder = new FieldSort.Builder().field("ssrNorm");
							break;
						}
					}
					
					if (fsbuilder != null) {
						so.field(fsbuilder.order(SortOrder.Desc).build());
					}
					
					return so;
				});
		
		// TODO this fails after 10000 results
		//SearchResponse<HighScore> resp = searchInternal(req.index(INDEX_NAME()).build());
		//long count = (long)resp.aggregations().get("count").valueCount().value();
		//return new HighScoreCollection(hits(resp), count);
		return new HighScoreCollection(searchDocuments(req));
	}

	@LogRuntime
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

	@LogRuntime
	public List<Integer> findRatesUsedOnChart(Chart c) {
		SearchRequest.Builder req = new SearchRequest.Builder()
				.aggregations("rates", agg -> agg.terms(ta -> ta.field("musicRate")))
				.query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))));
		Map<String, Aggregate> aggs = searchInternal(req, null).aggregations();
		if (!aggs.get("rates").isLterms()) {
			return new ArrayList<>();
		}
		return aggs.get("rates").lterms().buckets().array().stream().map(b -> Integer.parseInt(b.key())).collect(Collectors.toList());
	}

	@LogRuntime
	public List<HighScore> findScoresByChartOnAllRates(Chart c, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))))
				.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
				));
		return searchDocuments(req);
	}

	@LogRuntime
	public List<HighScore> findScoresByChartOnRate(Chart c, int rate, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(qq -> qq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(c.getChartKey()))))
				.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
				.must(qq -> qq.match(mq -> mq.field("musicRate").query(fv -> fv.longValue(rate))))
				));
		return searchDocuments(req);
	}

	@LogRuntime
	public List<HighScore> findUserScoresWithSpecificSkillsetValue(User u, int calcVersion, Skillset ss) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq
				.must(
					new Query.Builder().match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))).build()
				)
				));
		return searchDocuments(req);
	}

	@LogRuntime
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

	@LogRuntime
	public long deleteIfCalcVersionOlderThan(int calcVersion) {
		DeleteByQueryRequest req = new DeleteByQueryRequest.Builder().query(q -> q.range(rq -> rq.lt(JsonData.of(calcVersion)).field("calcVersion"))).build();
		return search.deleteByQuery(req);
	}

}
