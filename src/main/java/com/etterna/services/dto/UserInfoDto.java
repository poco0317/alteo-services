package com.etterna.services.dto;

import java.util.List;

import com.etterna.services.repo.projection.ScoreProjection;
import com.etterna.services.repo.projection.UserSkillsetProjection;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserInfoDto {
	
	private UserSkillsetProjection userAndSkillsets;
	private List<ScoreProjection> scores;
	private ScoreProjection score;
	
	private int maxPage;

}
