package com.etterna.services.opensearch;

import org.springframework.stereotype.Service;

import com.etterna.services.model.LoginSession;

@Service
public class LoginSessionIndexService extends BaseIndexService<LoginSession> {

	@Override
	public String INDEX_NAME() {
		return "login-session";
	}

	@Override
	public Class<LoginSession> getClazz() {
		return LoginSession.class;
	}

}
