package com.etterna.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;
import com.etterna.services.datamodel.UserSkillsetValue;

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
		
		List<Object[]> allScores = scores.getScoresWithSkillsetValue(user, actualSkillset);
		Collections.sort(allScores, new Comparator<Object[]>() {
			public int compare(Object[] a, Object[] b) {
				ScoreSpecificValue ssvA = (ScoreSpecificValue)a[1];
				ScoreSpecificValue ssvB = (ScoreSpecificValue)b[1];
				return ssvB.getValue().compareTo(ssvA.getValue());
			}
		});
		List<GetSkillsetTopXDTO> o = new ArrayList<>();
		for (Object[] oo : allScores) {
			HighScore hs = (HighScore)oo[0];
			ScoreSpecificValue ssv = (ScoreSpecificValue)oo[1];
			
			GetSkillsetTopXDTO dto = new GetSkillsetTopXDTO();
			ScoreDTO scoreDTO = new ScoreDTO();
			SkillsetDTO skillsetDTO = new SkillsetDTO(0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f, 0.f);
			switch (actualSkillset) {
				case OVERALL:
					skillsetDTO.setOverall(ssv.getValue().floatValue());
					break;
				case STREAM:
					skillsetDTO.setStream(ssv.getValue().floatValue());
					break;
				case JUMPSTREAM:
					skillsetDTO.setJumpstream(ssv.getValue().floatValue());
					break;
				case HANDSTREAM:
					skillsetDTO.setHandstream(ssv.getValue().floatValue());
					break;
				case STAMINA:
					skillsetDTO.setStamina(ssv.getValue().floatValue());
					break;
				case JACKSPEED:
					skillsetDTO.setJackSpeed(ssv.getValue().floatValue());
					break;
				case CHORDJACK:
					skillsetDTO.setChordjack(ssv.getValue().floatValue());
					break;
				case TECHNICAL:
					skillsetDTO.setTechnical(ssv.getValue().floatValue());
					break;
			}
			scoreDTO.setChartKey(hs.getChart().getChartKey());
			scoreDTO.setDifficulty(hs.getChart().getDifficulty());
			scoreDTO.setOverall(ssv.getValue().floatValue()); // THIS IS WRONG
			scoreDTO.setRate(hs.getMusicRate().floatValue() / 100.f);
			scoreDTO.setSongName(hs.getChart().getTitle());
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
		Set<UserSkillsetValue> ssrs = user.getSkillsetValues();
		if (ssrs != null) {
			ssrs.forEach(ssr -> {
				switch (ssr.getId().getSkillset()) {
					case OVERALL:
						skills.setOverall(ssr.getValue());
						dto.setPlayerRating(ssr.getValue());
						break;
					case STREAM:
						skills.setStream(ssr.getValue());
						break;
					case JUMPSTREAM:
						skills.setJumpstream(ssr.getValue());
						break;
					case HANDSTREAM:
						skills.setHandstream(ssr.getValue());
						break;
					case STAMINA:
						skills.setStamina(ssr.getValue());
						break;
					case JACKSPEED:
						skills.setJackSpeed(ssr.getValue());
						break;
					case CHORDJACK:
						skills.setChordjack(ssr.getValue());
						break;
					case TECHNICAL:
						skills.setTechnical(ssr.getValue());
						break;
					default: break;
				}
			});
		}
		dto.setSkillsets(skills);		
		r.setAttributes(dto);
		return r;
	}

}
