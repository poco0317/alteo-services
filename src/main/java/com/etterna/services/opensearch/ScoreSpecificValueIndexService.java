package com.etterna.services.opensearch;

import java.util.List;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Service;

import com.etterna.services.model.HighScore;
import com.etterna.services.model.ScoreSpecificValue;

@Service
public class ScoreSpecificValueIndexService extends BaseIndexService<ScoreSpecificValue> {

	@Override
	public String INDEX_NAME() {
		return "score-specific-value";
	}

	@Override
	public Class<ScoreSpecificValue> getClazz() {
		return ScoreSpecificValue.class;
	}

	public List<ScoreSpecificValue> findByScoreAndCalcVersion(HighScore hs, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q
				.bool(bq -> bq
						.must(qq -> qq
								.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
						.must(qq -> qq
								.match(mq -> mq.field("scoreKey").query(fv -> fv.stringValue(hs.getScoreKey()))))
				));
		return searchDocuments(req, null);
	}

	public Long deleteByCalcVersionLessThan(Integer calcVersion) {
		DeleteByQueryRequest req = new DeleteByQueryRequest.Builder().query(q -> q.range(rq -> rq.field("calcVersion").lt(JsonData.of(calcVersion)))).index(INDEX_NAME()).refresh(true).build();
		return search.deleteByQuery(req);
	}

}
