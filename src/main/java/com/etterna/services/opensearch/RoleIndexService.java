package com.etterna.services.opensearch;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.model.Role;
import com.etterna.services.model.RoleUser;
import com.etterna.services.model.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleIndexService extends BaseIndexService<Role> {
	
	@Autowired
	private RoleUserIndexService roleUserIndex;

	@Override
	public String INDEX_NAME() {
		return "role";
	}

	@Override
	public Class<Role> getClazz() {
		return Role.class;
	}

	public Set<Role> findByUser(User u) {
		
		List<RoleUser> connections = roleUserIndex.searchDocuments(() -> new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername())))));
		String names = String.join(" ", connections.stream().map(ru -> ru.getRole()).collect(Collectors.toList()));
		Set<Role> result = searchDocuments(() -> new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("name").query(fv -> fv.stringValue(names)))))
				.stream().collect(Collectors.toSet());
		return result;
	}

}
