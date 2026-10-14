package com.etterna.services.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.data.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

}
