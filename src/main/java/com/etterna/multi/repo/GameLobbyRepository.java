package com.etterna.multi.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.etterna.multi.data.GameLobby;

public interface GameLobbyRepository extends JpaRepository<GameLobby, Long> {

	@Modifying
	@Query("update GameLobby g set g.active = false")
	void closeAllLobbies();
	
}
