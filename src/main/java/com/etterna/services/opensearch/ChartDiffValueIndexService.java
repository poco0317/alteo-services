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
import com.etterna.services.model.ChartDiffValue;

@Service
public class ChartDiffValueIndexService extends BaseIndexService<ChartDiffValue> {
	
	@Autowired
	private CalcManager calc;

	@Override
	public String INDEX_NAME() {
		return "chart-diff-value";
	}

	@Override
	public Class<ChartDiffValue> getClazz() {
		return ChartDiffValue.class;
	}

	/**
	 * Get the diff values for the current calc version for a chart
	 */
	public Set<ChartDiffValue> getDiffValues(Chart c) {
		if (c == null) {
			return new HashSet<>();
		}
		
		SearchRequest.Builder req = new SearchRequest.Builder()
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
					);
		
		return searchDocuments(req, null).stream().collect(Collectors.toSet());
	}
	
	/**
	 * Get the diff values for the current calc version for many charts
	 */
	public Set<ChartDiffValue> getDiffValues(Collection<Chart> charts) {
		if (charts == null || charts.isEmpty()) {
			return new HashSet<>();
		}
		
		List<FieldValue> fvs = charts.stream().map(c -> new FieldValue.Builder().stringValue(c.getChartKey()).build()).collect(Collectors.toList());
		SearchRequest.Builder req = new SearchRequest.Builder()
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
					);
		return searchDocuments(req).stream().collect(Collectors.toSet());
	}

}
