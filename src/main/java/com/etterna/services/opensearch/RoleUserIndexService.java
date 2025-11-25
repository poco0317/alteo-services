package com.etterna.services.opensearch;

import org.springframework.stereotype.Service;

import com.etterna.services.model.RoleUser;

@Service
public class RoleUserIndexService extends BaseIndexService<RoleUser> {

	@Override
	public String INDEX_NAME() {
		return "role-user";
	}

	@Override
	public Class<RoleUser> getClazz() {
		return RoleUser.class;
	}

}
