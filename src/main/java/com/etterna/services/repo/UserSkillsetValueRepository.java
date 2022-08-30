package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.User;
import com.etterna.services.datamodel.UserSkillsetValue;
import com.etterna.services.datamodel.pk.UserSkillsetValuePk;

@Repository
public interface UserSkillsetValueRepository extends JpaRepository<UserSkillsetValue, UserSkillsetValuePk> {
	
	public List<UserSkillsetValue> findByIdUserAndIdCalcVersion(User user, Integer calcVersion);

}
