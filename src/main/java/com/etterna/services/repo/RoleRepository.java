package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	List<Role> findByName(String name);

}
