package com.etterna.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO.ScoreDTO;
import com.etterna.services.controller.legacy.dto.GetSkillsetTopXDTO.ScoreDTO.SkillsetDTO;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO;
import com.etterna.services.controller.legacy.dto.GetUserInfoResponse.UserInfoDTO.UserSkillsetDTO;
import com.etterna.services.controller.legacy.dto.UserWithSkillsetsPagination;
import com.etterna.services.data.Chart;
import com.etterna.services.data.HighScore;
import com.etterna.services.data.User;
import com.etterna.services.data.UserSkillsetValuesHistory;
import com.etterna.services.jpa.dao.HighScoreDao;
import com.etterna.services.jpa.dao.UserDao;
import com.etterna.services.jpa.dao.UserSkillsetValuesHistoryDao;
import com.etterna.site.dto.LeaderboardSort;

@Service
public class UserService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private HighScoreDao scores;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private UserSkillsetValuesHistoryDao ussvHistory;
	
	public void maintainUserSkillsetRatings() {
		List<User> users = userDao.findByMustRecalcRatingTrue();
		if (!users.isEmpty()) {
			m_logger.info("Updating user skillset ratings for {} users", users.size());
			final int calcVer = calc.getCalcVersion();
			
			for (User user : users) {
				UserSkillsetValuesHistory ssvals = ussvHistory.findByUserAndCalcVersion(user, calcVer);
				if (ssvals != null) {
					ussvHistory.delete(ssvals);
				}
				
				HashMap<Skillset, List<Double>> skillsetSSRs = new HashMap<>();
				BiConsumer<Skillset, Double> addskillset = (ss, v) -> {
					if (!skillsetSSRs.containsKey(ss)) {
						skillsetSSRs.put(ss, new ArrayList<>());
					}
					skillsetSSRs.get(ss).add(v);
				};
				
				List<HighScore> userScores = scores.findByUser(user);
				m_logger.info("Updating user {} SSRs - {} total scores", user.getUsername(), userScores.size());
				for (HighScore hs : userScores) {
					if (hs.getCalcVersion() != calc.getCalcVersion()) {
						continue;
					}
					
					addskillset.accept(Skillset.STREAM, hs.getStream());
					addskillset.accept(Skillset.JUMPSTREAM, hs.getJumpstream());
					addskillset.accept(Skillset.HANDSTREAM, hs.getHandstream());
					addskillset.accept(Skillset.STAMINA, hs.getStamina());
					addskillset.accept(Skillset.JACKSPEED, hs.getJackspeed());
					addskillset.accept(Skillset.CHORDJACK, hs.getChordjack());
					addskillset.accept(Skillset.TECHNICAL, hs.getTechnical());
				}
				
				// this should work out correctly
				// basically, we want a list of only Stream -> Tech not including the 0.0 Overall
				List<Double> newssvals = new LinkedList<>();
				for (Skillset ss : Skillset.values()) {
					switch (ss) {
						case OVERALL:
						{
							break;
						}
						case STREAM:
						case JUMPSTREAM:
						case HANDSTREAM:
						case STAMINA:
						case JACKSPEED:
						case CHORDJACK:
						case TECHNICAL:
						{
							if (skillsetSSRs.containsKey(ss)) {
								Collections.sort(skillsetSSRs.get(ss), Collections.reverseOrder());
								Double v = calc.aggregateSkill(skillsetSSRs.get(ss), 0.1, 1.05, 0.0, 10.24);
								newssvals.add(v);
							} else {
								newssvals.add(0.0);
							}
							break;
						}
						default:
							break;
					}
				}
				
				newssvals.add(0, calc.aggregateSkill(newssvals, 0.1, 1.125, 0.0, 10.24));
				user.setSs1Value(newssvals.get(0));
				user.setSs2Value(newssvals.get(1));
				user.setSs3Value(newssvals.get(2));
				user.setSs4Value(newssvals.get(3));
				user.setSs5Value(newssvals.get(4));
				user.setSs6Value(newssvals.get(5));
				user.setSs7Value(newssvals.get(6));
				user.setSs8Value(newssvals.get(7));
				user.setMustRecalcRating(false);
				ussvHistory.save(new UserSkillsetValuesHistory(user.getUsername(), calcVer, newssvals));
				userDao.save(user);
				m_logger.info("Updated user {} SSRs", user.getUsername());
			}
			
			m_logger.info("Finished updating user skillset ratings");
		}
	}

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
		
		User user = userDao.get(username);
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
		User user = userDao.get(userName);
		if (user == null) {
			return null;
		}
		GetUserInfoResponse r = new GetUserInfoResponse();
		UserInfoDTO dto = new UserInfoDTO();
		UserSkillsetDTO skills = new UserSkillsetDTO();
		dto.setPlayerRating(user.getSs1Value());
		skills.setOverall(user.getSs1Value());
		skills.setStream(user.getSs2Value());
		skills.setJumpstream(user.getSs3Value());
		skills.setHandstream(user.getSs4Value());
		skills.setStamina(user.getSs5Value());
		skills.setJackSpeed(user.getSs6Value());
		skills.setChordjack(user.getSs7Value());
		skills.setTechnical(user.getSs8Value());
		dto.setSkillsets(skills);
		r.setAttributes(dto);
		return r;
	}
	
	public Map<String, User> getByUsernamesMap(Collection<String> usernames) {
		return userDao.getByUserNamesMap(usernames);
	}

	public void setMustRecalcRating(Collection<User> userList, boolean mustRecalcRating) {
		userList.forEach(u -> u.setMustRecalcRating(mustRecalcRating));
		userDao.syncUsers(userList);
	}

	public boolean newUser(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	public User get(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetPassword(User u) {
		// TODO Auto-generated method stub
		
	}

	public UserWithSkillsetsPagination getUserLeaderboard(LeaderboardSort ls, int currentPage, int itemsperpage) {
		// TODO Auto-generated method stub
		return null;
	}

}
