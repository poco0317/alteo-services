package com.etterna.services.controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.calc.CalcManager;
import com.etterna.calc.Skillset;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsets;
import com.etterna.services.controller.legacy.dto.HighScoreWithSkillsetsPagination;
import com.etterna.services.controller.legacy.dto.UserWithSkillsets;
import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.HighScoreDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.User;

/**
 * Abomination
 */
@RestController
@RequestMapping("/query")
public class PublicQueryController {

	private static final Logger m_logger = LoggerFactory.getLogger(PublicQueryController.class);
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private CalcManager calc;
	
	@Autowired
	private HighScoreDao scores;
	
	@Autowired
	private UserDao users;
	
	private static final String NAV = "<a href='/query'>All Packs</a><br><a href='/query/users'>User Leaderboard</a><br><br>";
	
	@GetMapping("")
	public String getRanked() {
		m_logger.info("QUERY API CALLED :: Base Url");
		
		List<String> packs = charts.getAllPacks();
		
		StringBuilder sb = new StringBuilder();
		packs.forEach(s -> {
			final String pn = URLEncoder.encode(s, StandardCharsets.UTF_8);
			sb.append("<a href='/query/pack/"+pn+"'>"+s+"</a><br>");
		});
		
		return NAV+"ALL RANKED PACKS<br><br>"+sb.toString();
	}
	
	@GetMapping("/")
	public String getRankedAlias() {
		return getRanked();
	}
	
	@GetMapping("/pack/{pack}")
	public String getCharts(@PathVariable String pack) {
		pack = URLDecoder.decode(pack, StandardCharsets.UTF_8);
		m_logger.info("QUERY API CALLED :: Get Pack {}", pack);
		
		List<Chart> results = charts.getChartsInPack(pack);
		StringBuilder sb = new StringBuilder();
		if (results.size() == 0) {
			return NAV+"there is nothing here.";
		}
		
		sb.append("<table>");
		sb.append("<tr><th>Song Name</th><th>Difficulty</th><th>Overall</th><th>Stream</th><th>Jumpstream</th><th>Handstream</th><th>Stamina</th><th>Jackspeed</th><th>Chordjack</th><th>Technical</th></tr>");
		
		results.forEach(c -> {
			// skip charts with no difficulty
			if (c.getDiffValues().iterator().next().getValue() > 0.01) {
				sb.append("<tr>");
				sb.append("<td><a href='/query/chart/"+c.getChartKey()+"'>"+c.getSongName()+"</a></td>");
				sb.append("<td>"+c.getDifficulty()+"</td>");
				sb.append(calc.diffsToString(c.getDiffValues(), true));
				sb.append("</tr>");
			}
		});
		sb.append("</table>");
		
		return NAV+"SONGS IN PACK "+pack+"<br><br>"+sb.toString();
	}
	
	@GetMapping("/chart/{chartkey}")
	public String getLeaderboard(@PathVariable String chartkey) {
		m_logger.info("QUERY API CALLED :: Get Chart Leaderboard {}", chartkey);
		
		StringBuilder sb = new StringBuilder();
		
		List<HighScore> leaderboard = scores.getLeaderboard(chartkey);
		if (leaderboard.size() == 0) {
			return NAV+"there is nothing here.";
		}
		
		sb.append("<table>");
		sb.append("<tr><th>Username</th><th>Rate</th><th>Percent</th><th>Date</th><th>Overall</th><th>Stream</th><th>Jumpstream</th><th>Handstream</th><th>Stamina</th><th>Jackspeed</th><th>Chordjack</th><th>Technical</th></tr>");
		
		leaderboard.forEach(hs -> {
			String ssrs = calc.ssrsToString(hs.getSsrs(), true);
			sb.append("<tr>");
			sb.append("<td><a href='/query/score/"+hs.getScoreKey()+"'>"+
					hs.getUser().getUsername()+"</a></td>"+
					"<td>"+String.format("%5.2f", hs.getMusicRate().doubleValue()/100)+"x</td>"+
					"<td>"+String.format("%5.4f", hs.getSsrNorm().doubleValue()/10000)+"%</td>"+
					"<td>"+hs.getDateStr()+"</td>"+
					ssrs);
			sb.append("</tr>");
		});
		
		sb.append("</table>");
		
		Chart chart = charts.get(chartkey);
		return NAV+"ALL RATES LEADERBOARD - "+chart.getSongName() + " - "+chart.getDifficulty()+ " - " + leaderboard.size() + " scores<br><br>"+sb.toString();
	}
	
	@GetMapping("/score/{scorekey}")
	public String getScoreInfo(@PathVariable String scorekey) {
		m_logger.info("QUERY API CALLED :: Get Score Page {}", scorekey);
		HighScore score = scores.get(scorekey);
		
		if (score == null) {
			return NAV+"there is nothing here.";
		}
		StringBuilder sb = new StringBuilder();
		String uname = score.getUser().getUsername();
		
		sb.append(score.getChart().getSongName() + " - " +score.getChart().getDifficulty() + " - "+String.format("%5.2f", score.getMusicRate().doubleValue()/100)+"x - "+score.getChart().getPackName()+"<br><br>");
		sb.append("<a href='/query/user/"+uname+"'>"+uname+"</a>'s score ("+scorekey+")<br><br>");
		sb.append(score.getDateStr() + "<br>");
		sb.append(String.format("%5.4f", score.getSsrNorm().doubleValue()/10000)+"%<br>");
		sb.append(score.getModString()+"<br>");
		
		sb.append(calc.ssrsToString(score.getSsrs(), false)+"<br><br>");
		sb.append(score.getMarvCount() + " marvs <br>");
		sb.append(score.getPerfCount() + " perfs <br>");
		sb.append(score.getGreatCount() + " greats <br>");
		sb.append(score.getGoodCount() + " goods <br>");
		sb.append(score.getBadCount() + " bads <br>");
		sb.append(score.getMissCount() + " misses <br>");
		sb.append(score.getHeldCount() + " holds held<br>");
		sb.append(score.getNgCount() + " holds NG'd<br>");
		sb.append(score.getHitMineCount() + " mines hit<br><br>");
		
		sb.append("debug info<br>");
		sb.append(score.getWifePercent() + " wifepercent<br>");
		sb.append(score.getJudgeScale() + " judge scale<br>");
		sb.append(score.getCalcVersion() + " calc version<br>");
		sb.append(score.getWifeVersion() + " wife version<br>");
		sb.append(score.getWifePoints() + " wife points<br>");
		sb.append(score.getNegBpm() + " negbpm flag<br>");
		sb.append(score.getManuallyInvalid() + " manual invalidation<br>");
		sb.append(score.getNerfMultiplier() + " manual nerf multiplier<br>");
		sb.append(score.getNoCC() + " nocc flag<br>");
		
		return NAV+sb.toString();
	}
	
	@GetMapping("/users")
	public String getUserLeaderboard() {
		return getUserLeaderboard("Overall");
	}
	
	@GetMapping("/users/{skillset}")
	public String getUserLeaderboard(@PathVariable String skillset) {
		Skillset ss = Skillset.fromEttString(skillset);
		m_logger.info("QUERY API CALLED :: Get User Leaderboard {}", ss.name());
		List<UserWithSkillsets> result = users.getUserLeaderboard(ss);
		
		if (result.isEmpty()) {
			return NAV+"there is nothing here.";
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("LEADERBOARD BY "+ss.name()+"<br><br>");
		
		sb.append("<table>");
		sb.append("<tr><th>.</th><th>Username</th>");
		sb.append("<th><a href='/query/users/Overall'>Overall</a></th>"
				+ "<th><a href='/query/users/Stream'>Stream</a></th>"
				+ "<th><a href='/query/users/Jumpstream'>Jumpstream</a></th>"
				+ "<th><a href='/query/users/Handstream'>Handstream</a></th>"
				+ "<th><a href='/query/users/Stamina'>Stamina</a></th>"
				+ "<th><a href='/query/users/JackSpeed'>Jackspeed</a></th>"
				+ "<th><a href='/query/users/Chordjack'>Chordjack</a></th>"
				+ "<th><a href='/query/users/Technical'>Technical</a></th>"
				+ "</tr>");
		
		for (int i = 0; i < result.size(); i++) {
			UserWithSkillsets u = result.get(i);
			String uname = u.getUser().getUsername();
			sb.append("<tr>");
			sb.append("<td>"+(i+1)+"</td>");
			sb.append("<td><a href='/query/user/"+uname+"'>"+uname+"</a></td>");
			sb.append("<td>"+String.format("%5.2f", u.getOverall())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getStream())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getJumpstream())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getHandstream())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getStamina())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getJackspeed())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getChordjack())+"</td>");
			sb.append("<td>"+String.format("%5.2f", u.getTechnical())+"</td>");
			sb.append("</tr>");
		}
		
		sb.append("</table>");
		
		return NAV+sb.toString();
	}
	
	@GetMapping("/user/{username}")
	public String getUserPage(@PathVariable String username) {
		return getUserPage(username, "Overall");
	}
	
	@GetMapping("/user/{username}/{skillset}")
	public String getUserPage(@PathVariable String username, @PathVariable String skillset) {
		Skillset ss = Skillset.fromEttString(skillset);
		m_logger.info("QUERY API CALLED :: Get User Page {} - Skillset {}", username, ss.name());
		
		StringBuilder sb = new StringBuilder();
		
		User user = users.get(username);
		if (user == null) {
			return NAV+"there is nothing here.";
		}
		
		sb.append("USER PAGE - "+user.getUsername()+"<br><br>");
		
		UserWithSkillsets skillsets = users.getUserSkillsets(user);
		HighScoreWithSkillsetsPagination hspage = scores.getUserScores(user, ss, 1, 9999999);
		List<HighScoreWithSkillsets> hs = hspage.getHss();
		
		if (skillsets != null) {
			sb.append("Overall - "+String.format("%5.2f", skillsets.getOverall())+"<br>");
			sb.append("Stream - "+String.format("%5.2f", skillsets.getStream())+"<br>");
			sb.append("Jumpstream - "+String.format("%5.2f", skillsets.getJumpstream())+"<br>");
			sb.append("Handstream - "+String.format("%5.2f", skillsets.getHandstream())+"<br>");
			sb.append("Stamina - "+String.format("%5.2f", skillsets.getStamina())+"<br>");
			sb.append("Jackspeed - "+String.format("%5.2f", skillsets.getJackspeed())+"<br>");
			sb.append("Chordjack - "+String.format("%5.2f", skillsets.getChordjack())+"<br>");
			sb.append("Technical - "+String.format("%5.2f", skillsets.getTechnical())+"<br>");
			sb.append("<br>");
		}
		
		sb.append("Has "+hs.size()+" scores (only up to 200 will show here)<br><br>");
		
		if (hs.size() > 0) {
			sb.append("<table>");
			sb.append("<tr><th>Song Name</th><th>Difficulty</th><th>Rate</th><th>Percent</th><th>Date</th>");
			sb.append("<th><a href='/query/user/"+username+"/Overall'>Overall</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Stream'>Stream</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Jumpstream'>Jumpstream</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Handstream'>Handstream</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Stamina'>Stamina</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/JackSpeed'>Jackspeed</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Chordjack'>Chordjack</a></th>");
			sb.append("<th><a href='/query/user/"+username+"/Technical'>Technical</a></th>");
			sb.append("</tr>");
			
			for (int i = 0; i < hs.size() && i < 200; i++) {
				final HighScoreWithSkillsets h = hs.get(i);
				sb.append("<tr>");
				sb.append("<td><a href='/query/score/"+h.getScore().getScoreKey()+"'>"+h.getScore().getChart().getSongName()+"</a></td>");
				sb.append("<td>"+h.getScore().getChart().getDifficulty()+"</td>");
				sb.append("<td>"+String.format("%5.2fx", h.getScore().getMusicRate().doubleValue()/100)+"</td>");
				sb.append("<td>"+String.format("%5.4f%%", h.getScore().getSsrNorm().doubleValue()/10000)+"</td>");
				sb.append("<td>"+h.getScore().getDateStr()+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getOverall())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getStream())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getJumpstream())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getHandstream())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getStamina())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getJackspeed())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getChordjack())+"</td>");
				sb.append("<td>"+String.format("%5.2f", h.getTechnical())+"</td>");
				sb.append("</tr>");
			}
			
			sb.append("</table>");
		} else {
			sb.append("no scores");
		}
		
		
		return NAV+sb.toString();
	}
	
}
