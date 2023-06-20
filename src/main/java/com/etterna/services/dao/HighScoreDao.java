package com.etterna.services.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsetsPagination;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.controller.legacy.dto.UploadScoreRequest;
import com.etterna.services.model.Chart;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.ScoreSpecificValue;
import com.etterna.services.model.User;
import com.etterna.services.opensearch.HighScoreIndexService;
import com.etterna.services.opensearch.ScoreSpecificValueIndexService;
import com.etterna.services.opensearch.UserIndexService;
import com.etterna.services.opensearch.model.HighScoreFullUnion;
import com.etterna.site.dto.AllLeaderboardSort;
import com.etterna.site.dto.ChartLeaderboardPagination;
import com.etterna.site.dto.ChartLeaderboardSort;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.ProfileSort;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HighScoreDao {

	@Autowired
	private HighScoreIndexService hsIndex;

	@Autowired
	private ScoreSpecificValueIndexService ssrIndex;
	
	@Autowired
	private UserIndexService userIndex;

	@Autowired
	private ChartDao charts;
	
	@Autowired
	private CalcManager calc;

	private static final int SSR_LIST_LENGTH = 8;
	private static final int AUTO_COMMIT_CHUNK_SIZE = 100;
	
	private Queue<Object[]> stagedSsrUpdates = new ConcurrentLinkedQueue<>();

	@Transactional
	public HighScore get(String scoreKey) {
		return hsIndex.findById(scoreKey);
	}
	
	@Transactional
	public HighScoreFullUnion getFullUnion(String scoreKey) {
		HighScoreFullUnion o = new HighScoreFullUnion();
		
		HighScoreWithSkillsets hs = getScoreWithSkillsets(scoreKey);
		if (hs != null) {
			User user = userIndex.findById(hs.getScore().getUsername());
			ChartWithSkillsets chart = charts.getChartWithSkillsets(hs.getScore().getChartKey());
			
			o.setHsUnion(hs);
			o.setUser(user);
			o.setChartUnion(chart);
		}
		return o;
	}
	
	@Transactional
	public List<HighScore> getLeaderboard(String chartkey) {
		List<HighScore> hses = hsIndex.findByChartKey(chartkey);
		
		hses.sort(new Comparator<HighScore>() {
			@Override
			public int compare(HighScore h1, HighScore h2) {
				Double s1 = 0.0;
				Double s2 = 0.0;
				
				for (ScoreSpecificValue ssv : getSsrs(h1)) {
					if (ssv.getSkillset() == Skillset.OVERALL) {
						s1 = ssv.getValue();
						break;
					}
				}
				for (ScoreSpecificValue ssv : getSsrs(h2)) {
					if (ssv.getSkillset() == Skillset.OVERALL) {
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
		List<HighScore> hses = hsIndex.findByChartKey(chartkey).stream().filter(hs -> hs.getMusicRate() == rate).collect(Collectors.toList());
		
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
		List<HighScoreWithSkillsets> hses = hsIndex.findUserScoresWithSkillsets(u, calc.getCalcVersion());
		return sortBySkillsets(hses, ps, page, perpage);
	}
	
	/**
	 * These scores are eligible to be recalculated and will be taken care of by the batch job
	 */
	@Transactional
	public int countUncalculatedScores(User u) {
		List<HighScore> hses = hsIndex.findUserRecalculableScores(u, calc.getCalcVersion());
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
		List<HighScore> hses = hsIndex.findUserIncalculableScores(u, calc.getCalcVersion());
		if (hses == null) {
			return 0;
		}
		return hses.size();
	}
	
	@Transactional
	public ChartLeaderboardPagination getLeaderboardForAllChartsPagination(int rate, AllLeaderboardSort ls, int page, int itemsPerPage) {
		
		List<Integer> rates = hsIndex.findAllRates();
		rates.sort(Integer::compareTo);
		
		List<HighScoreWithSkillsets> obs;
		if (rate == -1) {
			obs = hsIndex.findScoresOnAllChartsOnAllRates(calc.getCalcVersion());
		} else {
			obs = hsIndex.findScoresOnAllChartsOnRate(rate, calc.getCalcVersion());
		}
		if (obs.isEmpty()) {
			return new ChartLeaderboardPagination(null, new ArrayList<>(), 1, 1, rate);
		}
		
		Map<String, HighScoreFullUnion> hsvs = completeUnion(obs);
		int sliceStart = Math.min(itemsPerPage * (page-1), hsvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, hsvs.size());
		
		return new ChartLeaderboardPagination(null, hsvs.values()
				.stream()
				.sorted(AllLeaderboardSort.HighScoreWithSkillsetsComparator(ls))
				.collect(Collectors.toList())
				.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(hsvs.size() / (float)itemsPerPage)), rate);
	}
	
	@Transactional
	public ChartLeaderboardPagination getChartLeaderboardPagination(String chartkey, int rate, ChartLeaderboardSort ls, int page, int itemsPerPage) {
		Chart c = charts.get(chartkey);
		if (c == null) {
			return new ChartLeaderboardPagination(c, new ArrayList<>(), 1, 1, -1);
		}
		
		List<Integer> rates = hsIndex.findRatesUsedOnChart(c);
		rates.sort(Integer::compareTo);
		
		List<HighScoreWithSkillsets> hsUnion;
		if (rate == -1) {
			hsUnion = hsIndex.findScoresByChartOnAllRates(c, calc.getCalcVersion());
		} else {
			hsUnion = hsIndex.findScoresByChartOnRate(c, rate, calc.getCalcVersion());
		}
		if (hsUnion.isEmpty()) {
			return new ChartLeaderboardPagination(c, new ArrayList<>(), 1, 1, rate);
		}
		
		Map<String, HighScoreFullUnion> hsvs = completeUnion(hsUnion);
		int sliceStart = Math.min(itemsPerPage * (page-1), hsvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, hsvs.size());
		
		return new ChartLeaderboardPagination(c, hsvs.values()
				.stream()
				.sorted(ChartLeaderboardSort.HighScoreWithSkillsetsComparator(ls))
				.collect(Collectors.toList())
				.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(hsvs.size() / (float)itemsPerPage)), rate);
	}
	
	private Map<String, HighScoreFullUnion> completeUnion(List<HighScoreWithSkillsets> hsUnions) {
		Set<String> chartkeys = hsUnions.stream().map(hsUnion -> hsUnion.getScore().getChartKey()).collect(Collectors.toSet());
		Map<String, ChartWithSkillsets> cwss = charts.getChartsWithSkillsetsMap(chartkeys);
		Set<String> usernames = hsUnions.stream().map(hsUnion -> hsUnion.getScore().getUsername()).collect(Collectors.toSet());
		Map<String, User> users = userIndex.findUsersByNameMap(usernames);
		return hsUnions.stream().collect(Collectors.toMap(hswss -> hswss.getScore().getScoreKey(), hswss -> {
			final String ck = hswss.getScore().getChartKey();
			final String user = hswss.getScore().getUsername();
			HighScoreFullUnion union = new HighScoreFullUnion();
			union.setChartUnion(cwss.get(ck));
			union.setHsUnion(hswss);
			union.setUser(users.get(user));
			return union;
		}));
	}
	
	private HighScoreWithSkillsetsPagination sortBySkillsets(List<HighScoreWithSkillsets> obs, ProfileSort ps, int page, int itemsPerPage) {
		Map<String, HighScoreFullUnion> hsvs = completeUnion(obs);
		
		int sliceStart = Math.min(itemsPerPage * (page-1), hsvs.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, hsvs.size());
		
		if (hsvs.size() == 0) {
			return new HighScoreWithSkillsetsPagination(hsvs.values().stream().collect(Collectors.toList()), 1, 1);
		}
		
		return new HighScoreWithSkillsetsPagination(hsvs.values().stream().sorted(new Comparator<HighScoreFullUnion>() {
			@Override
			public int compare(HighScoreFullUnion a, HighScoreFullUnion b) {
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
								av = a.getHsUnion().getOverall();
								bv = b.getHsUnion().getOverall();
								break;
							case STREAM:
								av = a.getHsUnion().getStream();
								bv = b.getHsUnion().getStream();
								break;
							case JUMPSTREAM:
								av = a.getHsUnion().getJumpstream();
								bv = b.getHsUnion().getJumpstream();
								break;
							case HANDSTREAM:
								av = a.getHsUnion().getHandstream();
								bv = b.getHsUnion().getHandstream();
								break;
							case STAMINA:
								av = a.getHsUnion().getStamina();
								bv = b.getHsUnion().getStamina();
								break;
							case JACKSPEED:
								av = a.getHsUnion().getJackspeed();
								bv = b.getHsUnion().getJackspeed();
								break;
							case CHORDJACK:
								av = a.getHsUnion().getChordjack();
								bv = b.getHsUnion().getChordjack();
								break;
							case TECHNICAL:
								av = a.getHsUnion().getTechnical();
								bv = b.getHsUnion().getTechnical();
								break;
							default:
								break;
						}
						if (av.equals(bv)) {
							return b.getHsUnion().getScore().getSsrNorm().compareTo(a.getHsUnion().getScore().getSsrNorm());
						} else {
							return bv.compareTo(av);
						}
					}
					case SONG:
					{
						String an = a.getChartUnion().getChart().getTitle();
						String bn = b.getChartUnion().getChart().getTitle();
						// opposite direction sort vs the others
						int o = an.compareToIgnoreCase(bn);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					case PERCENT:
					{
						Integer as = a.getHsUnion().getScore().getSsrNorm();
						Integer bs = b.getHsUnion().getScore().getSsrNorm();
						int o = bs.compareTo(as);
						if (o != 0) {
							return o;
						}
						// fall through
					}
					case DATE:
					default: {
						SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String ads = a.getHsUnion().getScore().getDateStr();
						String bds = b.getHsUnion().getScore().getDateStr();
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
	
	@Transactional
	public List<HighScoreWithSkillsets> getScoresWithSkillsetValue(User u, Skillset ss) {
		return hsIndex.findUserScoresWithSpecificSkillsetValue(u, calc.getCalcVersion(), ss);
	}
	
	@Transactional
	public HighScoreWithSkillsets getScoreWithSkillsets(String scorekey) {
		return hsIndex.findScoreWithSkillsets(scorekey, calc.getCalcVersion());
	}

	/**
	 * Return all scores either missing SSRs or having SSRs which are on an old calc version
	 * Will skip a score which: is invalid by site, is cc on, is missing ssr/rate
	 * Currently does not care if the xml/game invalidates the score
	 */
	@Transactional
	public List<HighScore> getScoresToCalculate() {
		return hsIndex.findRecalculableScores(calc.getCalcVersion());
	}
	
	@Transactional
	public Long deleteSsrsOlderThan(Integer calcVersion) {
		return ssrIndex.deleteByCalcVersionLessThan(calcVersion);
	}

	/**
	 * This prepares new ssr values to be committed but does not save anything.
	 * The work is deferred to chunks or a manual flush.
	 */
	public void stageUpdatedSsrs(HighScore hs, List<Float> ssrs, boolean instantCommit) {
		if (ssrs == null || ssrs.size() != SSR_LIST_LENGTH) {
			m_logger.warn("Attempted to update HighScore {} with bad SSR list : {}", hs.getScoreKey(), ssrs);
			return;
		}
		
		m_logger.debug("Updating SSRS : {} : {}", hs.getScoreKey(), ssrs);
		hs.setCalcVersion(calc.getCalcVersion());
		
		List<ScoreSpecificValue> ssrsUpdated = new LinkedList<>();
		for(Skillset ss : Skillset.values()) {
			ScoreSpecificValue ssr = new ScoreSpecificValue();
			ssr.setCalcVersion(calc.getCalcVersion());
			ssr.setScoreKey(hs.getScoreKey());
			ssr.setSkillset(ss);
			ssr.setValue(ssrs.get(ss.ordinal()).doubleValue());
			ssrsUpdated.add(ssr);
		}
		
		if (instantCommit) {
			ssrIndex.saveBulk(ssrsUpdated, Refresh.False);
			hsIndex.save(hs, Refresh.False);
		}
		
		User u = getUser(hs);
		if (u != null && (u.getMustRecalcRating() == null || !u.getMustRecalcRating())) {
			u.setMustRecalcRating(true);
			if (instantCommit) {
				userIndex.save(u, Refresh.True);
			}
		}
		
		if (!instantCommit) {
			stagedSsrUpdates.add(new Object[] {u, hs, ssrsUpdated});
			if (stagedSsrUpdates.size() >= AUTO_COMMIT_CHUNK_SIZE) {
				flushStagedSsrs();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void flushStagedSsrs() {
		if (stagedSsrUpdates.isEmpty()) return;
		
		m_logger.info("Flushing to commit {} entries from the staged ssr queue", stagedSsrUpdates.size());
		Set<User> users = new HashSet<>();
		List<ScoreSpecificValue> ssrs = new LinkedList<>();
		List<HighScore> hses = new LinkedList<>();
		while (!stagedSsrUpdates.isEmpty()) {
			Object[] entry = stagedSsrUpdates.poll();
			if (entry[0] != null) {
				// ...huh
				users.add((User)entry[0]);
			}
			hses.add((HighScore)entry[1]);
			ssrs.addAll((List<ScoreSpecificValue>)entry[2]);
		}
		userIndex.saveBulk(users, Refresh.True);
		ssrIndex.saveBulk(ssrs, Refresh.False);
		hsIndex.saveBulk(hses, Refresh.False);
		m_logger.info("Finished flushing staged ssr queue");
	}

	@Transactional
	public void add(UploadScoreRequest req, User user) {
		if (get(req.getScorekey()) != null) {
			return;
		}

		HighScore hs = new HighScore();
		hs.setBadCount(Integer.parseInt(req.getBad()));
		hs.setBrittleKey(req.getHash());
		hs.setCalcVersion(0); // set 0 to cause a recalc later
		hs.setChartKey(req.getChartkey());
		hs.setDateStr(req.getDatetime());
		hs.setEtternaValid(Integer.parseInt(req.getValid()));
		hs.setGoodCount(Integer.parseInt(req.getGood()));
		hs.setGrade(req.getGrade());
		hs.setGreatCount(Integer.parseInt(req.getGreat()));
		hs.setGuid(req.getMachineGuid());
		hs.setHeldCount(Integer.parseInt(req.getHeld()));
		hs.setHitMineCount(Integer.parseInt(req.getHitmine()));
		hs.setJudgeScale(Double.parseDouble(req.getJudgeScale()));
		hs.setLetgoCount(Integer.parseInt(req.getLetgo()));
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
		hs.setUsername(user.getUsername());
		hs.setWifeGrade(req.getWifeGrade());
		hs.setWifePercent(Double.parseDouble(req.getWife()));
		hs.setWifePoints(Double.parseDouble(req.getWifePoints()));
		hs.setWifeVersion(Integer.parseInt(req.getWife_version()));

		hsIndex.save(hs, Refresh.False);
	}

	public User getUser(HighScore hs) {
		return userIndex.findById(hs.getUsername());
	}

	public Set<ScoreSpecificValue> getSsrs(HighScore hs) {
		return ssrIndex.findByScoreAndCalcVersion(hs, calc.getCalcVersion()).stream().collect(Collectors.toSet());
	}

	public Chart getChart(HighScore hs) {
		return charts.get(hs.getChartKey());
	}

}
