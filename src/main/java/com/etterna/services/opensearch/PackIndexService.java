package com.etterna.services.opensearch;

import java.util.List;

import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Service;

import com.etterna.services.model.Pack;

@Service
public class PackIndexService extends BaseIndexService<Pack> {
	
	@Override
	public String INDEX_NAME() {
		return "pack";
	}

	@Override
	public Class<Pack> getClazz() {
		return Pack.class;
	}

	/**
	 * Find a list of packs containing this chartkey
	 */
	public List<Pack> findByChartKey(String chartkey) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("chartKeys").query(fv -> fv.stringValue(chartkey))));
		return searchDocuments(req, null);
	}

}
