package com.etterna.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.etterna.multi.web.dto.PlayerDTO;

@Service
public class MultiplayerRequestService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(MultiplayerRequestService.class);
	
	@Value("${etterna.multi-base-url}")
	private String multiplayerApiUrl;
	
	private static final String ONLINE_PLAYERS = "/online";
	
	private String api(String endpoint) {
		return multiplayerApiUrl + endpoint;
	}
	
	public List<PlayerDTO> getOnlinePlayers() {
		m_logger.info("Searching for online players");
		RestTemplate restCall = new RestTemplate();
		ResponseEntity<List<PlayerDTO>> response = restCall.exchange(api(ONLINE_PLAYERS), HttpMethod.GET, null, new ParameterizedTypeReference<List<PlayerDTO>>() {});
		
		List<PlayerDTO> o = response.getBody();
		m_logger.info("Got back {} players", o.size());
		return o;
	}

}
