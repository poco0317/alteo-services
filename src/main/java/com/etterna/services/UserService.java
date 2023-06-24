package com.etterna.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO.ScoreDTO;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO.ScoreDTO.SkillsetDTO;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO.UserSkillsetDTO;
import com.etterna.services.controller.legacy.dto.UserWithSkillsets;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.model.Chart;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.User;

@Service
public class UserService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao users;
	
	@Autowired
	private HighScoreDao scores;

	public void addFavorite() {
		// TODO Auto-generated method stub
		
	}

	public void removeFavorite() {
		// TODO Auto-generated method stub
		
	}

	public void getFavorites() {
		// TODO Auto-generated method stub
		
	}

	public void removeGoal() {
		// TODO Auto-generated method stub
		
	}

	public void addGoal() {
		// TODO Auto-generated method stub
		
	}

	public void updateGoal() {
		// TODO Auto-generated method stub
		
	}

	public void getUserSkillsetRanks() {
		// TODO Auto-generated method stub
		
	}

	@Transactional
	public List<GetSkillsetTopXDTO> getTop25(String username, String skillset, int count) {
		m_logger.info("Getting Top {} for user {} - Skillset {}", count, username, skillset);
		Skillset actualSkillset = Skillset.fromEttString(skillset);
		
		User user = users.get(username);
		if (user == null) {
			return null;
		}
		
		List<HighScore> allScores = scores.getScoresWithSkillsetValue(user, actualSkillset);
		Collections.sort(allScores, new Comparator<HighScore>() {
			public int compare(HighScore a, HighScore b) {
				switch (actualSkillset) {
					case OVERALL:
					default:
						return a.getOverall().compareTo(b.getOverall());
					case STREAM:
						return a.getStream().compareTo(b.getStream());
					case JUMPSTREAM:
						return a.getJumpstream().compareTo(b.getJumpstream());
					case HANDSTREAM:
						return a.getHandstream().compareTo(b.getHandstream());
					case STAMINA:
						return a.getStamina().compareTo(b.getStamina());
					case JACKSPEED:
						return a.getJackspeed().compareTo(b.getJackspeed());
					case CHORDJACK:
						return a.getChordjack().compareTo(b.getChordjack());
					case TECHNICAL:
						return a.getTechnical().compareTo(b.getTechnical());
						
				}
			}
		});
		List<GetSkillsetTopXDTO> o = new ArrayList<>();
		for (HighScore hs : allScores) {
			Chart chart = scores.getChart(hs);
			
			GetSkillsetTopXDTO dto = new GetSkillsetTopXDTO();
			ScoreDTO scoreDTO = new ScoreDTO();
			SkillsetDTO skillsetDTO = new SkillsetDTO(0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f);
			skillsetDTO.setOverall(hs.getOverall().floatValue());
			skillsetDTO.setStream(hs.getStream().floatValue());
			skillsetDTO.setJumpstream(hs.getJumpstream().floatValue());
			skillsetDTO.setHandstream(hs.getHandstream().floatValue());
			skillsetDTO.setStamina(hs.getStamina().floatValue());
			skillsetDTO.setJackSpeed(hs.getJackspeed().floatValue());
			skillsetDTO.setChordjack(hs.getChordjack().floatValue());
			skillsetDTO.setTechnical(hs.getTechnical().floatValue());
			
			scoreDTO.setChartKey(hs.getChartKey());
			scoreDTO.setDifficulty(chart.getDifficulty());
			scoreDTO.setOverall(hs.getOverall().floatValue());
			scoreDTO.setRate(hs.getMusicRate().floatValue() / 100.f);
			scoreDTO.setSongName(chart.getTitle());
			scoreDTO.setWife(hs.getWifePercent().floatValue() * 100);
			scoreDTO.setSkillsets(skillsetDTO);
			dto.setAttributes(scoreDTO);
			dto.setId("1");
			o.add(dto);
			if (o.size() >= count)
				break;
		}
		return o;
	}

	@Transactional
	public GetUserInfoResponse getUserInfo(String userName) {
		User user = users.get(userName);
		if (user == null) {
			return null;
		}
		GetUserInfoResponse r = new GetUserInfoResponse();
		UserInfoDTO dto = new UserInfoDTO();
		UserSkillsetDTO skills = new UserSkillsetDTO();
		UserWithSkillsets uwss = users.getUserSkillsets(user);
		dto.setPlayerRating(uwss.getOverall());
		skills.setOverall(uwss.getOverall());
		skills.setStream(uwss.getStream());
		skills.setJumpstream(uwss.getJumpstream());
		skills.setHandstream(uwss.getHandstream());
		skills.setStamina(uwss.getStamina());
		skills.setJackSpeed(uwss.getJackspeed());
		skills.setChordjack(uwss.getChordjack());
		skills.setTechnical(uwss.getTechnical());
		dto.setSkillsets(skills);		
		r.setAttributes(dto);
		return r;
	}

}
