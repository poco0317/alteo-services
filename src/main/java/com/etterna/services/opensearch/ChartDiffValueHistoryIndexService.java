package com.etterna.services.opensearch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.model.Chart;
import com.etterna.services.model.ChartSkillsetValuesHistory;
import com.etterna.util.LogRuntime;

@Service
public class ChartDiffValueHistoryIndexService extends BaseIndexService<ChartSkillsetValuesHistory> {
	
	@Autowired
	private CalcManager calc;

	@Override
	public String INDEX_NAME() {
		return "chart-diff-value-history";
	}

	@Override
	public Class<ChartSkillsetValuesHistory> getClazz() {
		return ChartSkillsetValuesHistory.class;
	}

	/**
	 * Get the diff values for the current calc version for a chart
	 */
	@LogRuntime
	public ChartSkillsetValuesHistory getDiffValues(Chart c) {
		if (c == null) {
			return null;
		}
		
		Set<ChartSkillsetValuesHistory> result = searchDocuments(() -> new SearchRequest.Builder()
				.query(new Query.Builder()
						.bool(new BoolQuery.Builder()
							.must(
								new Query.Builder()
									.match(m -> m.field("chartKey").query(fv -> fv.stringValue(c.getChartKey())))
									.build(),
								new Query.Builder()
									.match(m -> m.field("calcVersion").query(fv -> fv.longValue(c.getCalcVersion())))
									.build()
								).build()
							).build()
						))
				.stream().collect(Collectors.toSet());
		if (result == null || result.isEmpty()) {
			return null;
		}
		return result.iterator().next();
	}
	
	/**
	 * Get the diff values for the current calc version for many charts
	 */
	@LogRuntime
	public Set<ChartSkillsetValuesHistory> getDiffValues(Collection<Chart> charts) {
		if (charts == null || charts.isEmpty()) {
			return new HashSet<>();
		}
		
		List<FieldValue> fvs = charts.stream().map(c -> new FieldValue.Builder().stringValue(c.getChartKey()).build()).collect(Collectors.toList());
		return searchDocuments(() -> new SearchRequest.Builder()
				.query(new Query.Builder()
						.bool(new BoolQuery.Builder()
							.must(
								new Query.Builder()
									.terms(tq -> tq.field("chartKey.keyword").terms(tqf -> tqf.value(fvs)))
									.build(),
								new Query.Builder()
									.match(m -> m.field("calcVersion").query(fv -> fv.longValue(calc.getCalcVersion())))
									.build()
								).build()
							).build()
						))
				.stream().collect(Collectors.toSet());
	}

}
