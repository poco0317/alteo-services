package com.etterna.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.multi.data.GameLobby;
import com.etterna.multi.data.LobbyMessage;
import com.etterna.multi.data.LobbyScore;
import com.etterna.multi.data.UserLogin;
import com.etterna.multi.repo.GameLobbyRepository;
import com.etterna.multi.repo.LobbyMessageRepository;
import com.etterna.multi.repo.LobbyScoreRepository;
import com.etterna.multi.repo.UserLoginRepository;
import com.etterna.services.dao.UserDao;
import com.etterna.site.dto.LobbyScoreWithChart;
import com.etterna.site.dto.MultiUserWithStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MultiplayerDataService {
	
	@Autowired
	private GameLobbyRepository lobbies;
	
	@Autowired
	private LobbyMessageRepository messages;
	
	@Autowired
	private LobbyScoreRepository scores;
	
	@Autowired
	private UserLoginRepository multiLogins;
	
	@Autowired
	private UserDao siteUsers;
	
	@Autowired
	private MultiplayerRequestService multiplayerApi;
	
	@Transactional
	public GameLobby getSession(Long sessionId) {
		return lobbies.findById(sessionId).orElse(null);
	}
	
	@Transactional
	public List<GameLobby> getMultiplayerSessions() {
		List<GameLobby> sessions = lobbies.findAll();
		
		sessions.sort(new Comparator<GameLobby>() {
			@Override
			public int compare(GameLobby o1, GameLobby o2) {
				return o1.getId().compareTo(o2.getId());
			}});
		
		return sessions;
	}
	
	@Transactional
	public List<LobbyMessage> getMessagesInSession(Long sessionId) {
		List<LobbyMessage> o = messages.findByLobby(sessionId);
		o.sort(new Comparator<LobbyMessage>() {
			@Override
			public int compare(LobbyMessage l1, LobbyMessage l2) {
				return l1.getSent().compareTo(l2.getSent());
			}
		});
		
		return o;
	}
	
	@Transactional
	public List<LobbyScoreWithChart> getScoresInSession(Long sessionId) {
		List<LobbyScoreWithChart> o = scores.findByLobby(sessionId);
		o.addAll(scores.findUnrankedFilesInLobby(sessionId));
		
		o.sort(new Comparator<LobbyScoreWithChart>() {
			@Override
			public int compare(LobbyScoreWithChart l1, LobbyScoreWithChart l2) {
				return l1.getScore().getDateStr().compareTo(l2.getScore().getDateStr());
			}
		});
		
		return o;
	}
	
	@Transactional
	public List<MultiUserWithStatus> getPlayersInSession(Long sessionId) {
		GameLobby session = lobbies.findById(sessionId).orElse(null);
		if (session == null) {
			return new ArrayList<>();
		}
		
		Set<String> activity = multiplayerApi.getPlayersInLobby(session.getName()).stream().map(player -> player.getName().toLowerCase()).collect(Collectors.toSet());
		
		List<MultiUserWithStatus> o = session.getUsers().stream().sorted(new Comparator<UserLogin>() {
			@Override
			public int compare(UserLogin o1, UserLogin o2) {
				return o1.getUsername().compareToIgnoreCase(o2.getUsername());
			}
		}).map(user -> {
			MultiUserWithStatus mu = new MultiUserWithStatus();
			mu.setUser(user);
			mu.setActive(activity.contains(user.getUsername().toLowerCase()));
			return mu;
		}).collect(Collectors.toList());
		
		return o;
	}

}
