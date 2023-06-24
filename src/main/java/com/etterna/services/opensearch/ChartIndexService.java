package com.etterna.services.opensearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.FiltersBucket;
import org.opensearch.client.opensearch._types.aggregations.MultiBucketBase;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.SearchRequest.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartDiffValue;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.Pack;
import com.etterna.site.dto.ChartWithCount;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.PackNameWithChartCount;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChartIndexService extends BaseIndexService<Chart> {
	
	@Autowired
	private PackIndexService packIndex;
	
	@Autowired
	private HighScoreIndexService scoreIndex;
	
	@Autowired
	private ChartDiffValueIndexService diffValueIndex;

	@Override
	public String INDEX_NAME() {
		return "chart";
	}

	@Override
	public Class<Chart> getClazz() {
		return Chart.class;
	}

	public List<Chart> findByCalcVersionLessThan(int version) {
		SearchRequest.Builder req = new SearchRequest.Builder()
				.query(
					new Query.Builder()
						.range(rq -> rq.field("calcVersion").lt(JsonData.of(version)))
						.build()
					);
		return searchDocuments(req);
	}

	public List<Chart> findByCalcVersionNot(int version) {
		SearchRequest.Builder req = new SearchRequest.Builder()
				.query(
					new Query.Builder()
						.bool(bq -> bq.mustNot(mnq -> mnq.match(m -> m.field("calcVersion").query(fv -> fv.longValue(version)))))
						.build()
					);
		return searchDocuments(req);
	}
	
	public Chart findByChartkey(String chartkey) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(chartkey))));
		List<Chart> o = searchDocuments(req, null);
		if (o == null || o.size() == 0) {
			return null;
		}
		return o.get(0);
	}
	
	public Map<String, Chart> findChartsByChartKeyMap(Collection<String> chartkeys) {
		List<FieldValue> fvs = chartkeys.stream().map(ck -> new FieldValue.Builder().stringValue(ck).build()).collect(Collectors.toList());
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.terms(tq -> tq.field("chartKey.keyword").terms(tqf -> tqf.value(fvs))));
		return searchDocuments(req).stream().collect(Collectors.toMap(c -> c.getChartKey(), c -> c));
	}

	/**
	 * This just gets the whole list of chartkeys from the db...
	 */
	public Set<String> findChartKeyByChartKeyNotNull() {
		SearchRequest.Builder req = new SearchRequest.Builder()
				.query(new Query.Builder()
						.bool(bq -> bq.must(mq -> mq.exists(eq -> eq.field("chartKey"))))
						.build());
		return searchDocuments(req).stream().map(c -> c.getChartKey()).collect(Collectors.toSet());
	}

	public Map<String, Integer> getPackNamesWithScoreCountsMap() {
		List<Pack> packs = packIndex.findAll();
		Map<String, Query> ql = new HashMap<>();
		packs.forEach(p -> {
			String cks = String.join(" ", p.getChartKeys());
			ql.put(p.getName(), new Query.Builder().match(new MatchQuery.Builder().field("chartKey").query(fv -> fv.stringValue(cks)).build()).build());
		});
		SearchRequest.Builder req = new SearchRequest.Builder().query(qb -> qb.matchAll(maq -> maq)).aggregations("count", agg -> agg.filters(v -> v.filters(fq -> fq.keyed(ql))));
		SearchResponse<HighScore> resp = scoreIndex.searchInternal(req, null);
		Map<String, Aggregate> aggs = resp.aggregations();
		return aggs.get("count").filters().buckets().keyed().entrySet().stream().collect(Collectors.toMap(k -> k.getKey(), v -> (int)v.getValue().docCount()));
	}

	public List<PackNameWithChartCount> getPackNamesWithChartCounts() {
		List<Pack> packs = packIndex.findAll();
		return packs.stream().map(p -> new PackNameWithChartCount(p.getName(), p.getDisplayName(), p.getChartKeys().size())).collect(Collectors.toList());
	}

	public Set<String> getChartKeysInPack(Pack pack) {
		return pack.getChartKeys().stream().collect(Collectors.toSet());
	}

	public List<ChartWithCount> getChartsAndScoreCounts(Set<String> chartkeys) {
		if (chartkeys.isEmpty()) {
			return new ArrayList<>();
		}
		
		Map<String, Query> ql = new HashMap<>();
		chartkeys.forEach(ck -> {
			ql.put(ck, new Query.Builder().match(new MatchQuery.Builder().field("chartKey").query(fv -> fv.stringValue(ck)).build()).build());
		});
		SearchRequest.Builder req = new SearchRequest.Builder().query(qb -> qb.matchAll(maq -> maq)).aggregations("count", agg -> agg.filters(v -> v.filters(fq -> fq.keyed(ql))));
		SearchResponse<HighScore> resp = scoreIndex.searchInternal(req, null);
		Map<String, FiltersBucket> aggs = resp.aggregations().get("count").filters().buckets().keyed();
		
		SearchRequest.Builder getCharts = new SearchRequest.Builder().query(qb -> qb.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(String.join(" ", chartkeys)))));
		return searchDocuments(getCharts).stream().map(c -> new ChartWithCount(c, (long)aggs.get(c.getChartKey()).docCount())).collect(Collectors.toList());
	}

	// TODO ???
	public List<ChartWithCount> getChartsWithNoScores(Set<String> chartkeys) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.bool(bq -> bq.mustNot(mnq -> mnq.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(String.join(" ", chartkeys)))))));
		scoreIndex.searchDocuments(req);
		return null;
	}
	
	public ChartWithSkillsets findChartWithSkillsets(String chartkey, int calcVersion) {
		Chart c = findByChartkey(chartkey);
		Set<ChartDiffValue> diffs = diffValueIndex.getDiffValues(c);
		return new ChartWithSkillsets(c, diffs, 0);
	}
	
	public List<ChartWithSkillsets> findChartsWithSkillsets(Collection<String> chartkeys, int calcVersion) {
		List<FieldValue> fvs = chartkeys.stream().map(ck -> new FieldValue.Builder().stringValue(ck).build()).collect(Collectors.toList());
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.terms(tq -> tq.field("chartKey.keyword").terms(tqf -> tqf.value(fvs))));
		Map<String, Chart> chartmap = searchDocuments(req).stream().collect(Collectors.toMap(c -> c.getChartKey(), c -> c));
		Set<ChartDiffValue> diffs = diffValueIndex.getDiffValues(chartmap.values());
		Map<String, Set<ChartDiffValue>> diffCollected = new HashMap<>();
		for (ChartDiffValue diff : diffs) {
			if (!diffCollected.containsKey(diff.getChartKey())) {
				diffCollected.put(diff.getChartKey(), new HashSet<>());
			}
			diffCollected.get(diff.getChartKey()).add(diff);
		}
		return diffCollected.keySet().stream().map(ck -> new ChartWithSkillsets(chartmap.get(ck), diffCollected.get(ck), 0)).collect(Collectors.toList());
	}
	
	public Map<String, ChartWithSkillsets> findChartsWithSkillsetsMap(Collection<String> chartkeys, int calcVersion) {
		List<FieldValue> fvs = chartkeys.stream().map(ck -> new FieldValue.Builder().stringValue(ck).build()).collect(Collectors.toList());
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.terms(tq -> tq.field("chartKey.keyword").terms(tqf -> tqf.value(fvs))));
		
		List<Chart> huh = searchDocuments(req);
		Map<String, Chart> chartmap = huh.stream().collect(Collectors.toMap(c -> c.getChartKey(), c -> c));
		Set<ChartDiffValue> diffs = diffValueIndex.getDiffValues(chartmap.values());
		
		Map<String, Set<ChartDiffValue>> diffCollected = new HashMap<>();
		for (ChartDiffValue diff : diffs) {
			if (!diffCollected.containsKey(diff.getChartKey())) {
				diffCollected.put(diff.getChartKey(), new HashSet<>());
			}
			diffCollected.get(diff.getChartKey()).add(diff);
		}
		return diffCollected.keySet().stream().collect(Collectors.toMap(ck -> ck, ck -> new ChartWithSkillsets(chartmap.get(ck), diffCollected.get(ck), 0)));
	}
	
	public Set<Chart> getChartsInPack(Pack pack) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("chartKey").query(fv -> fv.stringValue(String.join(" ", pack.getChartKeys())))));
		return searchDocuments(req, null).stream().collect(Collectors.toSet());
	}

}
