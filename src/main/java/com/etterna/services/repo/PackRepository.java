package com.etterna.services.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.Pack;

@Repository
public interface PackRepository extends JpaRepository<Pack, String> {
	
}
