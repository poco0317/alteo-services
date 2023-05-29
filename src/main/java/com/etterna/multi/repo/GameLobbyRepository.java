package com.etterna.multi.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etterna.multi.data.GameLobby;

public interface GameLobbyRepository extends JpaRepository<GameLobby, Long> {

}
