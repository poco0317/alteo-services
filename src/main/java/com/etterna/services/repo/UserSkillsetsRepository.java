package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.services.data.UserSkillsets;
import com.etterna.services.repo.projection.UserSkillsetProjection;

@Repository
public interface UserSkillsetsRepository extends JpaRepository<UserSkillsets, String> {
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by u.player_rating desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForOverall();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Stream desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForStream();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Jumpstream desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForJumpstream();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Handstream desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForHandstream();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Stamina desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForStamina();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.JackSpeed desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForJackSpeed();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Chordjack desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForChordjack();
	
	@Query(value = "select "
			+ "u.username, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid "
			+ "order by us.Technical desc limit 5000",
			nativeQuery = true)
	List<UserSkillsetProjection> getForTechnical();
	
	@Query(value = "select "
			+ "u.username, u.aboutme, u.player_rating as Overall, us.Stream, us.Jumpstream, us.Handstream, us.Stamina, us.JackSpeed, us.Chordjack, us.Technical "
			+ "from users u, user_skillsets us where u.userid = us.userid and u.username = :username "
			+ "limit 1",
			nativeQuery = true)
	UserSkillsetProjection getUser(String username);

}
