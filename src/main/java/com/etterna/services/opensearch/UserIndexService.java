package com.etterna.services.opensearch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.model.User;
import com.etterna.services.model.UserSkillsetValuesHistory;
import com.etterna.util.LogRuntime;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserIndexService extends BaseIndexService<User> {
	
	@Autowired
	private UserSkillsetValueIndexService skillsetIndex;

	@Override
	public String INDEX_NAME() {
		return "user";
	}

	@Override
	public Class<User> getClazz() {
		return User.class;
	}

	public List<User> findByMustRecalcRatingTrue() {
		SearchRequest req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("mustRecalcRating").query(fv -> fv.booleanValue(true)))).build();
		return searchDocuments(req);
	}

	public List<User> findByUsername(String username) {
		return searchDocuments(() -> new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("username.keyword").query(fv -> fv.stringValue(username.toLowerCase())))));
	}

	public List<Object[]> findUsersWithSkillsetHistory() {
		List<User> users = findAll();
		Map<String, User> usermap = users.stream().collect(Collectors.toMap(user -> user.getUsername(), user -> user));
		List<UserSkillsetValuesHistory> skillsetValues = skillsetIndex.findAll();
		return skillsetValues.stream().map(ssv -> new Object[] {usermap.get(ssv.getUsername()), ssv}).collect(Collectors.toList());
	}
	
	@LogRuntime
	public Map<String, User> findUsersByNameMap(Collection<String> usernames) {
		List<FieldValue> fvs = usernames.stream().map(ck -> new FieldValue.Builder().stringValue(ck).build()).collect(Collectors.toList());
		return searchDocuments(() -> new SearchRequest.Builder()
				.query(q -> q
						.terms(tq -> tq
								.field("username.keyword")
								.terms(tqf -> tqf
										.value(fvs)))))
				.stream().collect(Collectors.toMap(u -> u.getUsername(), u -> u));
	}
}
