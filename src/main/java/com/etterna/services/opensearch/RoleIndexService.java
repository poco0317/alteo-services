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
		SearchRequest.Builder req = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("username").query(fv -> fv.stringValue(u.getUsername()))));
		
		List<RoleUser> connections = roleUserIndex.searchDocuments(req, null);
		String names = String.join(" ", connections.stream().map(ru -> ru.getRole()).collect(Collectors.toList()));
		SearchRequest.Builder getroles = new SearchRequest.Builder().query(q -> q.match(mq -> mq.field("name").query(fv -> fv.stringValue(names))));
		Set<Role> result = searchDocuments(getroles, null).stream().collect(Collectors.toSet());
		return result;
	}

}
