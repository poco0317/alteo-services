package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	public List<User> findByUsername(String username);
	public List<User> findByMustRecalcRatingTrueOrMustRecalcRatingNull();

}
