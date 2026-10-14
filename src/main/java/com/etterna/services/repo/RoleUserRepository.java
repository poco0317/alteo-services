package com.etterna.services.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.data.RoleUser;
import com.etterna.services.data.RoleUserId;

@Repository
public interface RoleUserRepository extends JpaRepository<RoleUser, RoleUserId> {

}
