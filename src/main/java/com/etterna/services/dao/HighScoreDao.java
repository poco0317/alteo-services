package com.etterna.services.dao;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.controller.legacy.dto.UploadScoreRequest;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.ScoreSpecificValue;
import com.etterna.services.datamodel.User;
import com.etterna.services.datamodel.pk.ScoreSpecificValuePk;
import com.etterna.services.repo.HighScoreRepository;
import com.etterna.services.repo.ScoreSpecificValueRepository;
import com.etterna.services.repo.UserRepository;

@Service
public class HighScoreDao {

	private static final Logger m_logger = LoggerFactory.getLogger(HighScoreDao.class);

	@Autowired
	private HighScoreRepository hsRepo;

	@Autowired
	private ScoreSpecificValueRepository ssrRepo;
	
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ChartDao charts;
	
	@Autowired
	private CalcManager calc;

	private static final int SSR_LIST_LENGTH = 8;

	@Transactional
	public HighScore get(String scoreKey) {
		return hsRepo.findById(scoreKey).orElse(null);
	}
	
	@Transactional
	public List<HighScore> getLeaderboard(String chartkey) {
		List<HighScore> hses = hsRepo.findByChartChartKey(chartkey);
		
		hses.sort(new Comparator<HighScore>() {
			@Override
			public int compare(HighScore h1, HighScore h2) {
				Double s1 = 0.0;
				Double s2 = 0.0;
				
				for (ScoreSpecificValue ssv : h1.getSsrs()) {
					if (ssv.getId().getSkillset() == Skillset.OVERALL) {
						s1 = ssv.getValue();
						break;
					}
				}
				for (ScoreSpecificValue ssv : h2.getSsrs()) {
					if (ssv.getId().getSkillset() == Skillset.OVERALL) {
						s2 = ssv.getValue();
						break;
					}
				}
				if (s2.compareTo(s1) == 0) {
					return h2.getSsrNorm().compareTo(h1.getSsrNorm());
				}
				return s2.compareTo(s1);
			}
		});
		
		return hses;
	}
	
	@Transactional
	public List<HighScore> getRateLeaderboard(String chartkey, int rate) {
		List<HighScore> hses = hsRepo.findByChartChartKey(chartkey).stream().filter(hs -> hs.getMusicRate() == rate).collect(Collectors.toList());
		
		hses.sort(new Comparator<HighScore>() {
			@Override
			public int compare(HighScore h1, HighScore h2) {
				return h2.getSsrNorm().compareTo(h1.getSsrNorm());
			}
		});
		return hses;
	}
	
	@Transactional
	public List<HighScoreWithSkillsets> getUserScores(User u, Skillset ss) {
		List<Object[]> hses = hsRepo.findScoreWithAllSkillsets(u, calc.getCalcVersion());
		return sortBySkillsets(hses, ss);
	}
	
	/**
	 * Input is [HighScore, ScoreSpecificValue]
	 */
	private List<HighScoreWithSkillsets> sortBySkillsets(List<Object[]> obs, Skillset ss) {
		HashMap<String, HighScoreWithSkillsets> hsvs = new HashMap<>();
		obs.forEach(o -> {
			HighScore hs = (HighScore)o[0];
			ScoreSpecificValue ssv = (ScoreSpecificValue)o[1];
			if (!hsvs.containsKey(hs.getScoreKey())) {
				hsvs.put(hs.getScoreKey(), new HighScoreWithSkillsets());
				hsvs.get(hs.getScoreKey()).setScore(hs);
			}
			final Skillset ssvss = ssv.getId().getSkillset();
			final Double v = ssv.getValue();
			switch (ssvss) {
				case OVERALL:
					hsvs.get(hs.getScoreKey()).setOverall(v);
				case STREAM:
					hsvs.get(hs.getScoreKey()).setStream(v);
				case JUMPSTREAM:
					hsvs.get(hs.getScoreKey()).setJumpstream(v);
				case HANDSTREAM:
					hsvs.get(hs.getScoreKey()).setHandstream(v);
				case STAMINA:
					hsvs.get(hs.getScoreKey()).setStamina(v);
				case JACKSPEED:
					hsvs.get(hs.getScoreKey()).setJackspeed(v);
				case CHORDJACK:
					hsvs.get(hs.getScoreKey()).setChordjack(v);
				case TECHNICAL:
					hsvs.get(hs.getScoreKey()).setTechnical(v);
				default:
					break;
			}
		});
		
		return hsvs.values().stream().sorted(new Comparator<HighScoreWithSkillsets>() {
			@Override
			public int compare(HighScoreWithSkillsets a, HighScoreWithSkillsets b) {
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
	
	/**
	 * Provides a list of Object[]
	 * Each Object[] is length 2
	 * index 0 is a HighScore
	 * index 1 is a ScoreSpecificValue
	 */
	@Transactional
	public List<Object[]> getScoresWithSkillsetValue(User u, Skillset ss) {
		return hsRepo.findScoreWithSkillsetValue(u, calc.getCalcVersion(), ss);
	}

	/**
	 * Return all scores either missing SSRs or having SSRs which are on an old calc version
	 * Will skip a score which: is invalid by site, is cc on, is missing ssr/rate
	 * Currently does not care if the xml/game invalidates the score
	 */
	@Transactional
	public List<HighScore> getScoresToCalculate() {
		return hsRepo.findRecalculableScores(calc.getCalcVersion());
	}
	
	@Transactional
	public Long deleteSsrsOlderThan(Integer calcVersion) {
		return ssrRepo.deleteByIdCalcVersionLessThan(calcVersion);
	}

	@Transactional
	public void updateSsrs(HighScore hs, List<Float> ssrs) {
		if (ssrs == null || ssrs.size() != SSR_LIST_LENGTH) {
			m_logger.warn("Attempted to update HighScore {} with bad SSR list : {}", hs.getScoreKey(), ssrs);
			return;
		}
		
		m_logger.debug("Updating SSRS : {} : {}", hs.getScoreKey(), ssrs);
		hs.setCalcVersion(calc.getCalcVersion());
		
		List<ScoreSpecificValue> ssrsUpdated = new LinkedList<>();
		for(Skillset ss : Skillset.values()) {
			ScoreSpecificValuePk id = new ScoreSpecificValuePk(hs, ss, calc.getCalcVersion());
			ScoreSpecificValue ssr = ssrRepo.findById(id).orElse(null);
			if (ssr == null) {
				ssr = new ScoreSpecificValue();
				ssr.setId(id);
			}
			ssr.setValue(ssrs.get(ss.ordinal()).doubleValue());
			ssrsUpdated.add(ssr);
		}
		ssrRepo.saveAll(ssrsUpdated);
		hs.setSsrs(new HashSet<>(ssrsUpdated));
		hsRepo.save(hs);
		
		User u = hs.getUser();
		if (u != null && u.getMustRecalcRating() == null || !u.getMustRecalcRating()) {
			u.setMustRecalcRating(true);
			userRepo.save(u);
		}
		
	}

	@Transactional
	public void add(UploadScoreRequest req, User user) {
		if (get(req.getScorekey()) != null) {
			return;
		}

		Chart chart = charts.get(req.getChartkey());

		HighScore hs = new HighScore();
		hs.setBadCount(Integer.parseInt(req.getBad()));
		hs.setBrittleKey(req.getHash());
		hs.setCalcVersion(0); // set 0 to cause a recalc later
		hs.setChart(chart);
		hs.setDateStr(req.getDatetime());
		hs.setEtternaValid(Integer.parseInt(req.getValid()));
		hs.setGoodCount(Integer.parseInt(req.getGood()));
		hs.setGrade(req.getGrade());
		hs.setGreatCount(Integer.parseInt(req.getGreat()));
		hs.setGuid(req.getMachineGuid());
		hs.setHeldCount(Integer.parseInt(req.getHeld()));
		hs.setHitMineCount(Integer.parseInt(req.getHitmine()));
		hs.setJudgeScale(Double.parseDouble(req.getJudgeScale()));
		hs.setMarvCount(Integer.parseInt(req.getMarv()));
		hs.setMaxCombo(Integer.parseInt(req.getMax_combo()));
		hs.setMissCount(Integer.parseInt(req.getMiss()));
		hs.setModString(req.getMods());
		hs.setMusicRate(Integer.parseInt(req.getRate()));
		hs.setNegBpm("1".equals(req.getNegsolo()));
		hs.setNgCount(Integer.parseInt(req.getNg()));
		hs.setNoCC("1".equals(req.getNocc()));
		hs.setPerfCount(Integer.parseInt(req.getPerfect()));
		hs.setScoreKey(req.getScorekey());
		hs.setSsrNorm(Integer.parseInt(req.getSsr_norm()));
		hs.setTopScore(Integer.parseInt(req.getTopscore()));
		hs.setUser(user);
		hs.setWifeGrade(req.getWifeGrade());
		hs.setWifePercent(Double.parseDouble(req.getWife()));
		hs.setWifePoints(Double.parseDouble(req.getWifePoints()));
		hs.setWifeVersion(Integer.parseInt(req.getWife_version()));

		hsRepo.save(hs);
	}

}
