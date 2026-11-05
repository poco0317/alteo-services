package com.etterna.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.repo.ScoreRepository;
import com.etterna.services.repo.projection.ScoreProjection;
import com.etterna.util.EtternaUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XmlGeneratorService {
	
	@Autowired
	private ScoreRepository scoreRepo;
	
	public String getEtternaXml(String username) {
		m_logger.info("Generating XML for {}", username);
		StringBuilder sb = new StringBuilder();
		sb.append("<Stats>");
		
		sb.append(getGeneralData(username) + "\n");
		sb.append(getPlayerScores(username) + "\n");
		
		sb.append("</Stats>");
		m_logger.info("Generated XML for {}", username);
		return sb.toString();
	}
	
	private String getGeneralData(String username) {
		StringBuilder sb = new StringBuilder();
		sb.append("<GeneralData>");
		
		eznode(sb, "DisplayName", username);
		
		sb.append("</GeneralData>");
		return sb.toString();
	}
	
	private String getPlayerScores(String username) {
		StringBuilder sb = new StringBuilder();
		sb.append("<PlayerScores>");
		
		List<ScoreProjection> scores = scoreRepo.getScoresForUser2(username);
		scores.sort(new Comparator<ScoreProjection>() {
			@Override
			public int compare(ScoreProjection o1, ScoreProjection o2) {
				if (o1.getChartkey().compareTo(o2.getChartkey()) == 0) {
					if (o1.getRate().compareTo(o2.getRate()) == 0) {
						return o1.getScorekey().compareTo(o2.getScorekey());
					}
					return o1.getRate().compareTo(o2.getRate());
				}
				return o1.getChartkey().compareTo(o2.getChartkey());
			}
		});
		m_logger.info(" For XML, user {} has {} scores", username, scores.size());
		
		String lastck = "impossible";
		Float lastrate = -1F;
		for (ScoreProjection score : scores) {
			if (!score.getChartkey().equals(lastck)) {
				if (!lastck.equals("impossible")) {
					sb.append("</ScoresAt>");
					sb.append("</Chart>");
				}
				lastck = score.getChartkey();
				lastrate = -1F;
				sb.append("<Chart Key='"+lastck+"' Pack='' Song='' Steps=''>");
			}
			if (score.getRate().floatValue() != lastrate.floatValue()) {
				if (lastrate.floatValue() != -1F) {
					sb.append("</ScoresAt>");
				}
				lastrate = score.getRate();
				sb.append("<ScoresAt BestGrade='' PBKey='' Rate='"+String.format("%4.3f", lastrate)+"'>");
			}
			sb.append(scoreNode(score) + "\n");
		}
		
		if (!scores.isEmpty()) {
			sb.append("</ScoresAt>\n</Chart>");
		}
		
		sb.append("</PlayerScores>");
		return sb.toString();
	}
	
	private String scoreNode(ScoreProjection score) {
		StringBuilder sb = new StringBuilder();
		
		String datestr = EtternaUtil.scoredatestr(score.getDatetime());
		String realscorekey = EtternaUtil.regenerateScorekey(score);
		
		sb.append("<Score Key='"+realscorekey+"'>");
		
		
		eznode(sb, "SSRCalcVersion", score.getCalcversion().toString());
		eznode(sb, "Grade", score.getWifeGrade().equalsIgnoreCase("Failed") ? "Failed" : wifetograde(score.getWifescore()));
		eznode(sb, "WifeScore", score.getWifescore().toString());
		eznode(sb, "WifePoints", "0");
		eznode(sb, "SSRNormPercent", score.getWifescore().toString());
		eznode(sb, "JudgeScale", "1.0");
		eznode(sb, "NoChordCohesion", score.getNocc().toString());
		eznode(sb, "EtternaValid", score.getValid() ? "1" : "0");
		eznode(sb, "MaxCombo", score.getMaxcombo().toString());
		eznode(sb, "Modifiers", score.getModifiers().replaceAll("[\"]", ""));
		eznode(sb, "MachineGuid", score.getMachineGuid().replaceAll("[\"]", ""));
		eznode(sb, "DateTime", datestr);
		
		sb.append("<TapNoteScores>");
		eznode(sb, "HitMine", score.getHitmine().toString());
		eznode(sb, "AvoidMine", "0");
		eznode(sb, "Miss", score.getMiss().toString());
		eznode(sb, "W5", score.getBad().toString());
		eznode(sb, "W4", score.getGood().toString());
		eznode(sb, "W3", score.getGreat().toString());
		eznode(sb, "W2", score.getPerfect().toString());
		eznode(sb, "W1", score.getMarv().toString());
		sb.append("</TapNoteScores>");
		
		sb.append("<HoldNoteScores>");
		eznode(sb, "LetGo", score.getLetgo().toString());
		eznode(sb, "Held", score.getHeld().toString());
		eznode(sb, "MissedHold", score.getMissedhold().toString());
		sb.append("</HoldNoteScores>");
		
		sb.append("<ValidationKeys>");
		eznode(sb, "Brittle", score.getValidationKey());
		eznode(sb, "Weak", "");
		sb.append("</ValidationKeys>");
		
		sb.append("</Score>");
		return sb.toString();
	}
	
	private void eznode(StringBuilder sb, String label, String value) {
		sb.append(String.format("<%s>%s</%s>\n", label, value, label));
	}
	
	private String wifetograde(Float wife) {
		return EtternaUtil.wifetograde(wife);
	}

}
