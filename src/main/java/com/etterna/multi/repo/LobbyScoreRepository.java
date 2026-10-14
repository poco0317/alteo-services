package com.etterna.multi.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etterna.multi.data.LobbyScore;

public interface LobbyScoreRepository extends JpaRepository<LobbyScore, Long> {

	@Query("SELECT ls, ls.chartKey, c from LobbyScore ls, MultiChart c where c.chartKey = ls.chartKey and ls.lobby.id = :lobby")
	List<Object[]> findByLobby(Long lobby);
	
	@Query("SELECT ls, ls.chartKey from LobbyScore ls where ls.chartKey not in (select distinct c.chartKey from MultiChart c) and ls.lobby.id = :lobby")
	List<Object[]> findUnrankedFilesInLobby(Long lobby);
}
