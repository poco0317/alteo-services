package com.etterna.services.opensearch;

import java.util.List;

import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.stereotype.Service;

import com.etterna.services.model.User;
import com.etterna.services.model.UserSkillsetValue;

@Service
public class UserSkillsetValueIndexService extends BaseIndexService<UserSkillsetValue> {

	@Override
	public String INDEX_NAME() {
		return "user-skillset-value";
	}

	@Override
	public Class<UserSkillsetValue> getClazz() {
		return UserSkillsetValue.class;
	}

	public List<UserSkillsetValue> findByUserAndCalcVersion(User user, int calcVersion) {
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q
				.bool(bq -> bq
						.must(qq -> qq.match(mq -> mq.field("username").query(fv -> fv.stringValue(user.getUsername()))))
						.must(qq -> qq.match(mq -> mq.field("calcVersion").query(fv -> fv.longValue(calcVersion))))
						));
		return searchDocuments(req, null);
	}

}
