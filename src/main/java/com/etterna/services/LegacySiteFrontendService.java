package com.etterna.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.data.Pack;
import com.etterna.services.dto.ChartInfo;
import com.etterna.services.dto.PackListDto;
import com.etterna.services.dto.PackPageDto;
import com.etterna.services.dto.UserInfoDto;
import com.etterna.services.repo.ChartRepository;
import com.etterna.services.repo.PackRepository;
import com.etterna.services.repo.ScoreRepository;
import com.etterna.services.repo.UserSkillsetsRepository;
import com.etterna.services.repo.projection.ChartProjection;
import com.etterna.services.repo.projection.PackListProjection;
import com.etterna.services.repo.projection.ScoreProjection;
import com.etterna.services.repo.projection.UserSkillsetProjection;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LegacySiteFrontendService {
	
	@Autowired
	private UserSkillsetsRepository userSkillsetRepo;
	
	@Autowired
	private ScoreRepository scoreRepo;
	
	@Autowired
	private ChartRepository chartRepo;
	
	@Autowired
	private PackRepository packRepo;
	
	private static int scorecount = 0;
	private static List<PackListProjection> packcache = new ArrayList<>();
	
	@PostConstruct
	public void fart() {
		scorecount = (int) scoreRepo.count();
		packcache = packRepo.getPackListing();
	}
	
	@Transactional
	public List<UserSkillsetProjection> getLeaderboard(Optional<String> sort) {
		m_logger.info("Getting leaderboard with SORT {}", sort);
		
		if (sort.isPresent()) {
			switch (sort.get()) {
				case "STREAM":
					return userSkillsetRepo.getForStream();
				case "JUMPSTREAM":
					return userSkillsetRepo.getForJumpstream();
				case "HANDSTREAM":
					return userSkillsetRepo.getForHandstream();
				case "STAMINA":
					return userSkillsetRepo.getForStamina();
				case "JACKSPEED":
					return userSkillsetRepo.getForJackSpeed();
				case "CHORDJACK":
					return userSkillsetRepo.getForChordjack();
				case "TECHNICAL":
					return userSkillsetRepo.getForTechnical();
			}
		}
		
		return userSkillsetRepo.getForOverall();
	}

	public UserInfoDto getUser(String username, Optional<String> sort) {
		m_logger.info("Getting user scores for {} SORT {}", username, sort);
		
		UserSkillsetProjection userinfo = userSkillsetRepo.getUser(username);
		List<ScoreProjection> scores = scoreRepo.getScoresForUser2(username);
		
		UserInfoDto result = new UserInfoDto();
		result.setUserAndSkillsets(userinfo);
		result.setScores(scores);
		
		return result;
	}
	
	public UserInfoDto getScoreInfo(String username, String scorekey) {
		m_logger.info("Getting score info for {} key {}", username, scorekey);
		
		UserSkillsetProjection userinfo = userSkillsetRepo.getUser(username);
		ScoreProjection score = scoreRepo.getScore(username, scorekey);
		
		UserInfoDto result = new UserInfoDto();
		result.setUserAndSkillsets(userinfo);
		result.setScore(score);
		
		return result;
	}

	public UserInfoDto getAllScores(int pagenum, String cursort) {
		m_logger.info("Getting allscores page {}, sort {}", pagenum, cursort);
		
		int perpage = 200;
		int maxpage = (int)Math.floor(scorecount / perpage) + 1;
		List<ScoreProjection> scores = scoreRepo.getAllScoresPagination((pagenum-1) * perpage, perpage);
		
		UserInfoDto r = new UserInfoDto();
		r.setScores(scores);
		r.setMaxPage(maxpage);
		
		return r;
	}

	public ChartInfo getChartInfo(String chartkey) {
		m_logger.info("Getting chart info for {}", chartkey);
		
		ChartInfo c = new ChartInfo();
		
		List<ChartProjection> charts = chartRepo.getChartsByChartkey(chartkey);
		if (charts == null || charts.isEmpty()) {
			return null;
		}
		
		Set<String> songkeys = charts.stream().map(chard -> chard.getSongkey()).collect(Collectors.toSet()); 
		
		ChartProjection choice = charts.get(0);
		
		Set<String> packnames = new HashSet<>();
		for (String song : songkeys) {
			packnames.addAll(packRepo.getPacksContainingSong(song));
		}
		
		List<Pack> outputpacks = new ArrayList<>();
		for (String name : packnames) {
			outputpacks.addAll(packRepo.findByPackname(name));
		}
		
		c.setPacks(outputpacks);
		c.setChart(choice);
		c.setScores(scoreRepo.getScoresForChart(chartkey));
		
		return c;
	}

	public List<PackListDto> getPackList() {
		
		List<PackListDto> o = new ArrayList<>();
		
		if (packcache.isEmpty()) {
			packcache = packRepo.getPackListing();
		}
		
		for (PackListProjection p : packcache) {
			o.add(new PackListDto(p));
		}
		
		
		return o;
	}

	public PackPageDto getPackPage(String packname) {
		PackPageDto o = new PackPageDto();
		
		o.setPackname(packname);
		o.setCharts(packRepo.getPackChartlist(packname));
		
		return o;
	}
	
	
	

}
