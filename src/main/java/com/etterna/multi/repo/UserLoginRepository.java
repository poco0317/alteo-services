package com.etterna.multi.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.multi.data.UserLogin;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, String> {

}
