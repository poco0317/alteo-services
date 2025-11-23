package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.UserWithSkillsets;
import com.etterna.services.controller.legacy.dto.UserWithSkillsetsPagination;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.User;
import com.etterna.services.model.UserSkillsetValuesHistory;
import com.etterna.services.opensearch.HighScoreIndexService;
import com.etterna.services.opensearch.UserIndexService;
import com.etterna.services.opensearch.UserSkillsetValueIndexService;
import com.etterna.site.dto.LeaderboardSort;

@Service
public class UserDao {

	private static final Logger m_logger = LoggerFactory.getLogger(UserDao.class);
	
	@Autowired
	private UserIndexService userIndex;
	
	@Autowired
	private UserSkillsetValueIndexService ussvIndex;
	
	@Autowired
	private HighScoreIndexService scoreIndex;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public void maintainUserSkillsetRatings() {
		List<User> users = userIndex.findByMustRecalcRatingTrue();
		if (!users.isEmpty()) {
			m_logger.info("Updating user skillset ratings for {} users", users.size());
			final int calcVer = calc.getCalcVersion();
			
			for (User user : users) {
				List<UserSkillsetValuesHistory> ssvals = ussvIndex.findByUserAndCalcVersion(user, calcVer);
				if (ssvals != null) {
					ussvIndex.deleteBulk(ssvals, Refresh.False);
				}
				
				HashMap<Skillset, List<Double>> skillsetSSRs = new HashMap<>();
				BiConsumer<Skillset, Double> addskillset = (ss, v) -> {
					if (!skillsetSSRs.containsKey(ss)) {
						skillsetSSRs.put(ss, new ArrayList<>());
					}
					skillsetSSRs.get(ss).add(v);
				};
				
				List<HighScore> userScores = scoreIndex.findByUser(user);
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
				user.setMustRecalcRating(false);
				ussvIndex.save(new UserSkillsetValuesHistory(user.getUsername(), calcVer, newssvals), Refresh.False);
				userIndex.save(user, Refresh.True);
				m_logger.info("Updated user {} SSRs", user.getUsername());
			}
			
			m_logger.info("Finished updating user skillset ratings");
		}
	}

	@Transactional
	public User get(String username) {
		List<User> users = userIndex.findByUsername(username);
		if (users == null || users.isEmpty()) {
			return null;
		}
		return users.get(0);
	}
	
	@Transactional
	public User getByUserId(String string) {
		return userIndex.findById(string);
	}
	
	@Transactional
	public boolean newUser(String username, String password) {
		if (get(username) != null) {
			return false;
		}
		m_logger.info("Created new user {}", username.toLowerCase());
		User user = new User();
		user.setUsername(username.toLowerCase());
		user.setDisplayName(username);
		user.setPassword(passwordEncoder.encode(password));
		userIndex.save(user, Refresh.True);
		return true;
	}
	
	@Transactional
	public boolean resetPassword(User u) {
		u.setPassword(passwordEncoder.encode("password"));
		userIndex.save(u, Refresh.True);
		m_logger.info("Reset user password - {}", u.getUsername());
		return true;
	}
	
	@Transactional
	public UserWithSkillsetsPagination getUserLeaderboard(LeaderboardSort ls, int page, int itemsPerPage) {
		List<User> usersAndSkillsets = userIndex.findAll();
		
		// users to structs
		HashMap<String, UserWithSkillsets> usvs = new HashMap<>();
		usersAndSkillsets.forEach(u -> {
			usvs.put(u.getUsername(), new UserWithSkillsets(u));
		});
		
		int sliceStart = Math.min(itemsPerPage * (page-1), usvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, usvs.size());
		m_logger.debug("{} {} {}", sliceStart, sliceEnd, usvs.size());
		
		if (usvs.size() == 0) {
			return new UserWithSkillsetsPagination(usvs.values().stream().collect(Collectors.toList()), 1, 1);
		}
		
		return new UserWithSkillsetsPagination(usvs.values().stream().sorted(new Comparator<UserWithSkillsets>() {
			@Override
			public int compare(UserWithSkillsets a, UserWithSkillsets b) {
				switch (ls) {
				case OVERALL:
				case STREAM:
				case JUMPSTREAM:
				case HANDSTREAM:
				case STAMINA:
				case JACKSPEED:
				case CHORDJACK:
				case TECHNICAL:
				{
					Double av = 0.0;
					Double bv = 0.0;
					switch (ls) {
						case OVERALL:
							av = a.getOverall();
							bv = b.getOverall();
							break;
						case STREAM:
							av = a.getStream();
							bv = b.getStream();
							break;
						case JUMPSTREAM:
							av = a.getJumpstream();
							bv = b.getJumpstream();
							break;
						case HANDSTREAM:
							av = a.getHandstream();
							bv = b.getHandstream();
							break;
						case STAMINA:
							av = a.getStamina();
							bv = b.getStamina();
							break;
						case JACKSPEED:
							av = a.getJackspeed();
							bv = b.getJackspeed();
							break;
						case CHORDJACK:
							av = a.getChordjack();
							bv = b.getChordjack();
							break;
						case TECHNICAL:
							av = a.getTechnical();
							bv = b.getTechnical();
							break;
						default:
							break;
					}
					if (av.equals(bv)) {
						// reverse sort when doing by name
						return a.getUser().getUsername().compareToIgnoreCase(b.getUser().getUsername());
					} else {
						return bv.compareTo(av);
					}
				}
				case NAME:
				default:
					{
						// reverse sort when doing by name
						int o = a.getUser().getUsername().compareToIgnoreCase(b.getUser().getUsername());
						if (o == 0) {
							return b.getOverall().compareTo(a.getOverall());
						} else {
							return o;
						}
					}
				}
			}
		}).collect(Collectors.toList()).subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(usvs.size() / (float)itemsPerPage)));
	}

}
