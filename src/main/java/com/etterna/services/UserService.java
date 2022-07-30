package com.etterna.services;

import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.controller.legacy.dto.GetUserInfoResponse;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO.UserSkillsetDTO;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;
import com.etterna.services.datamodel.UserSkillsetValue;

@Service
public class UserService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao users;

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

	public void getTop25() {
		// TODO Auto-generated method stub
		
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
