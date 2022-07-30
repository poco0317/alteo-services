package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
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
		return hsRepo.findByChartChartKey(chartkey);
	}

	/**
	 * Return all scores either missing SSRs or having SSRs which are on an old calc version
	 */
	@Transactional
	public List<HighScore> getScoresToCalculate() {
		Set<HighScore> hsUncalculated = new HashSet<>();
		hsUncalculated.addAll(hsRepo.findByCalcVersionLessThan(calc.getCalcVersion()));
		return new ArrayList<>(hsUncalculated);
	}

	@Transactional
	public void updateSsrs(HighScore hs, List<Float> ssrs) {
		if (ssrs == null || ssrs.size() != SSR_LIST_LENGTH) {
			m_logger.warn("Attempted to update HighScore {} with bad SSR list : {}", hs.getScoreKey(), ssrs);
			return;
		}
		
		m_logger.info("Updating SSRS : {} : {}", hs.getScoreKey(), ssrs);
		hs.setCalcVersion(calc.getCalcVersion());
		
		List<ScoreSpecificValue> ssrsUpdated = new LinkedList<>();
		for(Skillset ss : Skillset.values()) {
			ScoreSpecificValuePk id = new ScoreSpecificValuePk(hs, ss);
			ScoreSpecificValue ssr = ssrRepo.findById(id).orElse(null);
			if (ssr == null) {
				ssr = new ScoreSpecificValue();
				ssr.setId(id);
			}
			ssr.setCalcVersion(calc.getCalcVersion());
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
