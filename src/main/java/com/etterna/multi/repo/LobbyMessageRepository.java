package com.etterna.multi.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etterna.multi.data.LobbyMessage;

public interface LobbyMessageRepository extends JpaRepository<LobbyMessage, Long> {

	@Query("select m from LobbyMessage m where m.lobby.id = :lobby")
	List<LobbyMessage> findByLobby(Long lobby);
	
}
