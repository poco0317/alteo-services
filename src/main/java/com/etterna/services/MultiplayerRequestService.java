package com.etterna.services;

import java.util.Comparator;
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
	private static final String PLAYERS_IN_LOBBY = "/online/";
	
	private String api(String endpoint) {
		return multiplayerApiUrl + endpoint;
	}
	
	public List<PlayerDTO> getOnlinePlayers() {
		m_logger.info("Sending request to get all online players");
		RestTemplate restCall = new RestTemplate();
		ResponseEntity<List<PlayerDTO>> response = restCall.exchange(api(ONLINE_PLAYERS), HttpMethod.GET, null, new ParameterizedTypeReference<List<PlayerDTO>>() {});
		
		List<PlayerDTO> o = response.getBody();
		o.sort(new Comparator<PlayerDTO>() {
			@Override
			public int compare(PlayerDTO o1, PlayerDTO o2) {
				String l1 = o1.getLobby();
				String l2 = o2.getLobby();
				if (l1 == null) {
					return 1;
				}
				int o = l1.compareToIgnoreCase(l2);
				if (o == 0) {
					l1 = o1.getName();
					l2 = o2.getName();
					o = l1.compareToIgnoreCase(l2);
				}
				return o;
			}
		});
		m_logger.info("Got back {} players", o.size());
		return o;
	}
	
	public List<PlayerDTO> getPlayersInLobby(String lobbyName) {
		m_logger.info("Sending request to get players in lobby '{}'", lobbyName);
		RestTemplate restCall = new RestTemplate();
		ResponseEntity<List<PlayerDTO>> response = restCall.exchange(api(PLAYERS_IN_LOBBY) + lobbyName.toLowerCase(), HttpMethod.GET, null, new ParameterizedTypeReference<List<PlayerDTO>>() {});
		
		List<PlayerDTO> o = response.getBody();
		o.sort(new Comparator<PlayerDTO>() {
			@Override
			public int compare(PlayerDTO o1, PlayerDTO o2) {
				String l1 = o1.getLobby();
				String l2 = o2.getLobby();
				if (l1 == null) {
					return 1;
				}
				int o = l1.compareToIgnoreCase(l2);
				if (o == 0) {
					l1 = o1.getName();
					l2 = o2.getName();
					o = l1.compareToIgnoreCase(l2);
				}
				return o;
			}
		});
		m_logger.info("Got back {} players", o.size());
		return o;		
	}

}
