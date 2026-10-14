package com.etterna.services.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.data.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
