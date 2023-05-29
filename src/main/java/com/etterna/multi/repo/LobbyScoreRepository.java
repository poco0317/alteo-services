package com.etterna.multi.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etterna.multi.data.LobbyScore;
import com.etterna.site.dto.LobbyScoreWithChart;

public interface LobbyScoreRepository extends JpaRepository<LobbyScore, Long> {

	@Query("SELECT new com.etterna.site.dto.LobbyScoreWithChart(ls, ls.chartKey, c) from LobbyScore ls, Chart c where c.chartKey = ls.chartKey and ls.lobby.id = :lobby")
	List<LobbyScoreWithChart> findByLobby(Long lobby);
	
	@Query("SELECT new com.etterna.site.dto.LobbyScoreWithChart(ls, ls.chartKey) from LobbyScore ls where ls.chartKey not in (select distinct c.chartKey from Chart c) and ls.lobby.id = :lobby")
	List<LobbyScoreWithChart> findUnrankedFilesInLobby(Long lobby);
}
