package com.etterna.services.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsetsPagination;
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
import com.etterna.site.dto.ChartLeaderboardPagination;
import com.etterna.site.dto.ChartLeaderboardSort;
import com.etterna.site.dto.ProfileSort;

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
	public HighScoreWithSkillsetsPagination getUserScores(User u, ProfileSort ps, int page, int perpage) {
		List<Object[]> hses = hsRepo.findScoreWithAllSkillsets(u, calc.getCalcVersion());
		return sortBySkillsets(hses, ps, page, perpage);
	}
	
	/**
	 * These scores are eligible to be recalculated and will be taken care of by the batch job
	 */
	@Transactional
	public int countUncalculatedScores(User u) {
		List<HighScore> hses = hsRepo.findUserRecalculableScores(u, calc.getCalcVersion());
		if (hses == null) {
			return 0;
		}
		return hses.size();
	}
	
	/**
	 * These scores may be eligible to be recalculated but are not worth it, due to cc on or invalidation
	 */
	@Transactional
	public int countIncalculableScores(User u) {
		List<HighScore> hses = hsRepo.findUserIncalculableScores(u, calc.getCalcVersion());
		if (hses == null) {
			return 0;
		}
		return hses.size();
	}
	
	@Transactional
	public ChartLeaderboardPagination getChartLeaderboardPagination(String chartkey, int rate, ChartLeaderboardSort ls, int page, int itemsPerPage) {
		Chart c = charts.get(chartkey);
		if (c == null) {
			return new ChartLeaderboardPagination(c, new ArrayList<>(), 1, 1, -1);
		}
		
		List<Integer> rates = hsRepo.findRatesUsedOnChart(c);
		rates.sort(Integer::compareTo);
		
		List<Object[]> obs;
		if (rate == -1) {
			obs = hsRepo.findScoresByChartOnAllRates(c, calc.getCalcVersion());
		} else {
			obs = hsRepo.findScoresByChartOnRate(c, rate, calc.getCalcVersion());
		}
		if (obs.isEmpty()) {
			return new ChartLeaderboardPagination(c, new ArrayList<>(), 1, 1, rate);
		}
		
		Map<String, HighScoreWithSkillsets> hsvs = mapHighScoreAndSSVObjects(obs);
		int sliceStart = Math.min(itemsPerPage * (page-1), hsvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, hsvs.size());
		m_logger.debug("{} {} {}", sliceStart, sliceEnd, hsvs.size());
		
		return new ChartLeaderboardPagination(c, hsvs.values().stream().sorted(new Comparator<HighScoreWithSkillsets>() {
			@Override
			public int compare(HighScoreWithSkillsets a, HighScoreWithSkillsets b) {
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
						switch(ls) {
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
						if (av == bv) {
							Integer ar = a.getScore().getMusicRate();
							Integer br = b.getScore().getMusicRate();
							if (ar == br || ar == null || br == null) {
								return b.getScore().getSsrNorm().compareTo(a.getScore().getSsrNorm());
							} else {
								return br.compareTo(ar);
							}
						} else {
							return bv.compareTo(av);
						}
					}
					case DATE:
					{
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String ads = a.getScore().getDateStr();
						String bds = b.getScore().getDateStr();
						try {
							Date ad = f.parse(ads);
							Date bd = f.parse(bds);
							return bd.compareTo(ad);
						} catch (ParseException e) {
							return bds.compareToIgnoreCase(ads);
						}
					}
					case PLAYER:
					{
						String an = a.getScore().getUser().getUsername();
						String bn = b.getScore().getUser().getUsername();
						// opposite direction sort vs the others
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
						// fall through
					}

					default:
					case PERCENT:
					{
						Integer as = a.getScore().getSsrNorm();
						Integer bs = b.getScore().getSsrNorm();
						int o = bs.compareTo(as);
						return o;
					}
					
				}
			}
		}).collect(Collectors.toList()).subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(hsvs.size() / (float)itemsPerPage)), rate);
	}
	
	private Map<String, HighScoreWithSkillsets> mapHighScoreAndSSVObjects(List<Object[]> obs) {
		Map<String, HighScoreWithSkillsets> hsvs = new HashMap<>();
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
		return hsvs;
	}
	
	/**
	 * Input is [HighScore, ScoreSpecificValue]
	 */
	private HighScoreWithSkillsetsPagination sortBySkillsets(List<Object[]> obs, ProfileSort ps, int page, int itemsPerPage) {
		Map<String, HighScoreWithSkillsets> hsvs = mapHighScoreAndSSVObjects(obs);
		
		int sliceStart = Math.min(itemsPerPage * (page-1), hsvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, hsvs.size());
		m_logger.debug("{} {} {}", sliceStart, sliceEnd, hsvs.size());
		
		if (hsvs.size() == 0) {
			return new HighScoreWithSkillsetsPagination(hsvs.values().stream().collect(Collectors.toList()), 1, 1);
		}
		
		return new HighScoreWithSkillsetsPagination(hsvs.values().stream().sorted(new Comparator<HighScoreWithSkillsets>() {
			@Override
			public int compare(HighScoreWithSkillsets a, HighScoreWithSkillsets b) {
				switch (ps) {
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
						switch(ps) {
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
						if (av == bv) {
							return b.getScore().getSsrNorm().compareTo(a.getScore().getSsrNorm());
						} else {
							return bv.compareTo(av);
						}
					}
					case SONG:
					{
						String an = a.getScore().getChart().getSongName();
						String bn = b.getScore().getChart().getSongName();
						// opposite direction sort vs the others
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					case PERCENT:
					{
						Integer as = a.getScore().getSsrNorm();
						Integer bs = b.getScore().getSsrNorm();
						int o = bs.compareTo(as);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					case DATE:
					default: {
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String ads = a.getScore().getDateStr();
						String bds = b.getScore().getDateStr();
						try {
							Date ad = f.parse(ads);
							Date bd = f.parse(bds);
							return bd.compareTo(ad);
						} catch (ParseException e) {
							return bds.compareToIgnoreCase(ads);
						}
					}
				}
			}
		}).collect(Collectors.toList()).subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(hsvs.size() / (float)itemsPerPage)));
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
