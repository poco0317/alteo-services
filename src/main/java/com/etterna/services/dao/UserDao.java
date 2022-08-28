package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.PasswordUtil;
import com.etterna.services.controller.legacy.dto.UserWithSkillsets;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;
import com.etterna.services.datamodel.UserSkillsetValue;
import com.etterna.services.repo.HighScoreRepository;
import com.etterna.services.repo.ScoreSpecificValueRepository;
import com.etterna.services.repo.UserRepository;
import com.etterna.services.repo.UserSkillsetValueRepository;

@Service
public class UserDao {

	private static final Logger m_logger = LoggerFactory.getLogger(UserDao.class);
	
	@Autowired
	private UserRepository repo;
	
	@Autowired
	private UserSkillsetValueRepository ssRepo;
	
	@Autowired
	private ScoreSpecificValueRepository ssrRepo;
	
	@Autowired
	private HighScoreRepository scoreRepo;
	
	@Autowired
	private CalcManager calc;
	
	@Scheduled(fixedDelay = 1000L * 30L)
	void maintainUserSkillsetRatings() {
		List<User> users = repo.findByMustRecalcRatingTrueOrMustRecalcRatingNull();
		if (!users.isEmpty()) {
			m_logger.info("Updating user skillset ratings for {} users", users.size());
			
			for (User user : users) {
				List<UserSkillsetValue> ssvals = ssRepo.findByIdUser(user);
				List<HighScore> userScores = scoreRepo.findByUser(user);
				if (ssvals != null) {
					ssRepo.deleteAll(ssvals);
				}
				
				HashMap<Skillset, List<Double>> skillsetSSRs = new HashMap<>();
				m_logger.info("Updating user {} SSRs - {} total scores", user.getUsername(), userScores.size());
				for (HighScore hs : userScores) {
					if (hs.getCalcVersion() < calc.getCalcVersion()) {
						continue;
					}
					List<ScoreSpecificValue> ssrs = ssrRepo.findByIdScore(hs);
					for (ScoreSpecificValue ssr : ssrs) {
						Skillset ss = ssr.getId().getSkillset();
						switch (ss) {
							case OVERALL:
								break;
							case STREAM:
							case JUMPSTREAM:
							case HANDSTREAM:
							case STAMINA:
							case JACKSPEED:
							case CHORDJACK:
							case TECHNICAL:
							{
								if (!skillsetSSRs.containsKey(ss)) {
									skillsetSSRs.put(ss, new ArrayList<>());
								}
								skillsetSSRs.get(ss).add(ssr.getValue());
								
								break;
							}
							default:
								m_logger.error("Impossible skillset value {}", ssr.getId().getSkillset());
								break;
						}
					}
				}
				List<UserSkillsetValue> newssvals = new LinkedList<>();
				for (Skillset ss : Skillset.values()) {
					switch (ss) {
						case OVERALL:
						{
							newssvals.add(new UserSkillsetValue(user, ss, 0.0));
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
								newssvals.add(new UserSkillsetValue(user, ss, v));
							} else {
								newssvals.add(new UserSkillsetValue(user, ss, 0.0));
							}
							break;
						}
						default:
							break;
					}
				}
				// this should work out correctly
				// basically, we want a list of only Stream -> Tech not including the 0.0 Overall
				List<Double> tmpssvals = newssvals.stream().map(ssv -> ssv.getValue()).collect(Collectors.toList());
				tmpssvals.remove(0);
				
				newssvals.get(0).setValue(calc.aggregateSkill(tmpssvals, 0.1, 1.125, 0.0, 10.24));
				Set<UserSkillsetValue> ssvalSet = new HashSet<>(newssvals);
				user.setSkillsetValues(ssvalSet);
				user.setMustRecalcRating(false);
				ssRepo.saveAll(ssvalSet);
				repo.save(user);
				m_logger.info("Updated user {} SSRs", user.getUsername());
			}
			
			m_logger.info("Finished updating user skillset ratings");
		}
	}

	@Transactional
	public User get(String username) {
		List<User> users = repo.findByUsername(username);
		if (users == null || users.isEmpty()) {
			return null;
		}
		return users.get(0);
	}
	
	@Transactional
	public User getByUserId(Long userId) {
		return repo.findById(userId).orElse(null);
	}
	
	@Transactional
	public boolean newUser(String username, String password) {
		if (get(username) != null) {
			return false;
		}
		m_logger.info("Created new user {}", username);
		User user = new User();
		user.setUsername(username);
		String salt = PasswordUtil.getSalt();
		String pwsalt = PasswordUtil.hashPassword(password, salt);
		
		user.setSalt(salt);
		user.setPassword(pwsalt);
		repo.save(user);
		return true;
	}
	
	@Transactional
	public UserWithSkillsets getUserSkillsets(User u) {
		List<UserSkillsetValue> ssvs = ssRepo.findByIdUser(u);
		UserWithSkillsets o = new UserWithSkillsets();
		o.setUser(u);
		ssvs.forEach(ssv -> {
			final Double v = ssv.getValue();
			switch (ssv.getId().getSkillset()) {
				case OVERALL:
					o.setOverall(v);
					break;
				case STREAM:
					o.setStream(v);
					break;
				case JUMPSTREAM:
					o.setJumpstream(v);
					break;
				case HANDSTREAM:
					o.setHandstream(v);
					break;
				case STAMINA:
					o.setStamina(v);
					break;
				case JACKSPEED:
					o.setJackspeed(v);
					break;
				case CHORDJACK:
					o.setChordjack(v);
					break;
				case TECHNICAL:
					o.setTechnical(v);
					break;
				default:
					break;
			}
		});
		
		return o;
	}
	
	@Transactional
	public List<UserWithSkillsets> getUserLeaderboard(Skillset ss) {
		// a list of [User, UserSkillsetValue]
		// we need to compile the data structure
		List<Object[]> usersAndSkillsets = repo.findUsersWithSkillsets();
		
		// users to structs
		HashMap<String, UserWithSkillsets> usvs = new HashMap<>();
		usersAndSkillsets.forEach(usv -> {
			User u = (User)usv[0];
			UserSkillsetValue ssv = (UserSkillsetValue)usv[1];
			
			if (!usvs.containsKey(u.getUsername())) {
				usvs.put(u.getUsername(), new UserWithSkillsets());
				usvs.get(u.getUsername()).setUser(u);
			}
			final Skillset ssvss = ssv.getId().getSkillset();
			final Double v = ssv.getValue();
			switch (ssvss) {
				case OVERALL:
					usvs.get(u.getUsername()).setOverall(v);
				case STREAM:
					usvs.get(u.getUsername()).setStream(v);
				case JUMPSTREAM:
					usvs.get(u.getUsername()).setJumpstream(v);
				case HANDSTREAM:
					usvs.get(u.getUsername()).setHandstream(v);
				case STAMINA:
					usvs.get(u.getUsername()).setStamina(v);
				case JACKSPEED:
					usvs.get(u.getUsername()).setJackspeed(v);
				case CHORDJACK:
					usvs.get(u.getUsername()).setChordjack(v);
				case TECHNICAL:
					usvs.get(u.getUsername()).setTechnical(v);
				default:
					break;
			}
		});
		
		return usvs.values().stream().sorted(new Comparator<UserWithSkillsets>() {
			@Override
			public int compare(UserWithSkillsets a, UserWithSkillsets b) {
				switch (ss) {
					case OVERALL:
						return b.getOverall().compareTo(a.getOverall());
					case STREAM:
						return b.getStream().compareTo(a.getStream());
					case JUMPSTREAM:
						return b.getJumpstream().compareTo(a.getJumpstream());
					case HANDSTREAM:
						return b.getHandstream().compareTo(a.getHandstream());
					case STAMINA:
						return b.getStamina().compareTo(a.getStamina());
					case JACKSPEED:
						return b.getJackspeed().compareTo(a.getJackspeed());
					case CHORDJACK:
						return b.getChordjack().compareTo(a.getChordjack());
					case TECHNICAL:
						return b.getTechnical().compareTo(a.getTechnical());
					default:
						return b.getOverall().compareTo(a.getOverall());
				}
			}
		}).collect(Collectors.toList());
	}

}
