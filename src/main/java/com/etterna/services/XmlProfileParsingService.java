package com.etterna.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.etterna.services.dao.ChartDao;
import com.etterna.services.dao.UserDao;
import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.HighScore;
import com.etterna.services.datamodel.User;

@Service
public class XmlProfileParsingService {

	private static final Logger m_logger = LoggerFactory.getLogger(XmlProfileParsingService.class);
	
	@Autowired
	private SessionService sessions;
	
	@Autowired
	private ChartDao charts;
	
	@Autowired
	private UserDao users;
	
	private static ConcurrentHashMap<Long, byte[]> queuedXmls = new ConcurrentHashMap<>();
	
	/**
	 * Returns null when successful
	 */
	@Transactional
	public String intakeProfile(InputStream in, String authToken) {
		User user = sessions.sessionToUser(authToken);
		if (user == null) {
			m_logger.warn("User attempted to upload xml without authorization - Token {}", authToken);
			return "Not a valid user";
		}
		
		return add(in, user.getUserId());
	}
	
	@Scheduled(fixedDelay = 1000L * 10L)
	void maintainXmlQueue() {
		Iterator<Entry<Long, byte[]>> it = queuedXmls.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, byte[]> entry = it.next();
			User user = users.getByUserId(entry.getKey());
			if (user != null) {
				parseProfile(entry.getValue(), user);
			} else {
				m_logger.warn("Failed to get profile via ID {}", entry.getKey());
			}
			it.remove();
		}
	}
	
	private String add(InputStream in, Long userId) {
		if (queuedXmls.containsKey(userId)) {
			m_logger.warn("Tried to add userId {} into XML Queue a second time... Skipped", userId);
			return "Cannot add the same user twice while processing XMLs";
		} else {
			try {
				queuedXmls.put(userId, in.readAllBytes());
				return null;
			} catch (IOException e) {
				m_logger.error(e.getMessage(), e);
				return "Error when storing XML data for queue : "+e.getMessage();
			}
		}
	}
	
	@Transactional
	private String parseProfile(byte[] bytes, User user) {
		m_logger.info("Parsing uploaded XML profile from user {}", user.getUsername());
		
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(bytes), "Windows-1252")));
			doc.normalizeDocument();
			
			/*
			 * The format for these files should be:
			 * <Stats>
			 * 	<GeneralData> </>
			 * 	<Favorites> </>
			 * 	<PermaMirror> </>
			 * 	<Playlists> </>
			 *  <ScoreGoals> </>
			 *  <PlayerScores>
			 *   <Chart>
			 *    <ScoresAt>
			 *     <Score>
			 *     ... </> </> </> </>
			 *  </>
			 */
			
			List<HighScore> highscores = new LinkedList<>();
			
			int total = doc.getElementsByTagName("Score").getLength();
			int cnt = 0;
			int percents = 0;
			
			NodeList chartNodes = doc.getElementsByTagName("Chart");
			for (int i = 0; i < chartNodes.getLength(); i++) {
				Node chartNode = chartNodes.item(i);
				Element chartElement = (Element)chartNode;
				
				String chartkey = chartNode.getAttributes().getNamedItem("Key").getNodeValue();
				if (!charts.ranked(chartkey)) {
					int skipped = 0;
					NodeList sa = chartElement.getElementsByTagName("ScoresAt");
					for (int l = 0; l < sa.getLength(); l++) {
						skipped += ((Element)sa.item(l)).getElementsByTagName("Score").getLength();
					}
					m_logger.info("Chartkey {} is not ranked - Skipped {} scores", chartkey, skipped);
					cnt += skipped;
					continue;
				}
				
				Chart chart = charts.get(chartkey);
				NodeList scoresAt = chartElement.getElementsByTagName("ScoresAt");
				for (int j = 0; j < scoresAt.getLength(); j++) {
					Node rateNode = scoresAt.item(j);
					Element rateElement = (Element)rateNode;
					
					Integer rate = (int)(100.f * Float.parseFloat(rateNode.getAttributes().getNamedItem("Rate").getNodeValue()));
					
					NodeList scores = rateElement.getElementsByTagName("Score");
					for (int k = 0; k < scores.getLength(); k++) {
						cnt++;
						if (cnt > total / 5) {
							int intervals = 1;
							while (cnt > total / 5) {
								cnt -= total / 5;
								intervals++;
							}
							percents += intervals;
							m_logger.info("User {} XML Progress {}%", user.getUsername(), percents * 5);
						}
						
						Node scoreNode = scores.item(k);
						Element scoreElement = (Element)scoreNode;
						
						String scorekey = scoreNode.getAttributes().getNamedItem("Key").getNodeValue();
						m_logger.debug("Chartkey {} Rate {} Scorekey {}", chartkey, rate, scorekey);
						
						HighScore hs = new HighScore();
						hs.setCalcVersion(0);
						hs.setChart(chart);
						hs.setDateStr(guaranteeGet(scoreElement, "DateTime"));
						hs.setEtternaValid(nullint(guaranteeGet(scoreElement, "EtternaValid")));
						hs.setGrade(guaranteeGet(scoreElement, "Grade"));
						hs.setGuid(guaranteeGet(scoreElement, "MachineGuid"));
						hs.setJudgeScale(nulldouble(guaranteeGet(scoreElement, "JudgeScale")));
						hs.setManuallyInvalid(false);
						hs.setMaxCombo(nullint(guaranteeGet(scoreElement, "MaxCombo")));
						hs.setModString(guaranteeGet(scoreElement, "Modifiers"));
						hs.setMusicRate(rate);
						hs.setNegBpm(null);
						hs.setNoCC("1".equals(guaranteeGet(scoreElement, "NoChordCohesion")));
						hs.setScoreKey(scorekey);
						Double ssrnorm = nulldouble(guaranteeGet(scoreElement, "SSRNormPercent"));
						if (ssrnorm != null) {
							hs.setSsrNorm((int)(100.0 * ssrnorm));
						} else {
							hs.setSsrNorm(null);
						}
						hs.setTopScore(nullint(guaranteeGet(scoreElement, "TopScore")));
						hs.setUser(user);
						hs.setWifeGrade(null);
						hs.setWifePercent(nulldouble(guaranteeGet(scoreElement, "WifeScore")));
						hs.setWifeVersion(nullint(guaranteeGet(scoreElement, "wv")));
						hs.setWifePoints(nulldouble(guaranteeGet(scoreElement, "WifePoints")));
						
						Element tns = (Element)scoreElement.getElementsByTagName("TapNoteScores").item(0);
						hs.setPerfCount(nullint(guaranteeGet(tns, "W2")));
						hs.setMarvCount(nullint(guaranteeGet(tns, "W1")));
						hs.setGreatCount(nullint(guaranteeGet(tns, "W3")));
						hs.setGoodCount(nullint(guaranteeGet(tns, "W4")));
						hs.setBadCount(nullint(guaranteeGet(scoreElement, "W5")));
						hs.setMissCount(nullint(guaranteeGet(tns, "Miss")));
						hs.setHitMineCount(nullint(guaranteeGet(tns, "HitMine")));
						
						Element hns = (Element)scoreElement.getElementsByTagName("HoldNoteScores").item(0);
						hs.setHeldCount(nullint(guaranteeGet(hns, "Held")));
						Integer ng = nullint(guaranteeGet(hns, "MissedHold"));
						Integer lg = nullint(guaranteeGet(hns, "LetGo"));
						int ngcount = 0;
						if (ng != null) {
							ngcount += ng;
						}
						if (lg != null) {
							ngcount += lg;
						}
						hs.setNgCount(ngcount);
						
						highscores.add(hs);
					}
				}
			}
			
			m_logger.info("Finished parsing XML for user {} - {} ranked scores", user.getUsername(), highscores.size());
			
			return null; // success
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return "Parsing error: "+e.getMessage();
		}
	}
	
	private String guaranteeGet(Element element, String name) {
		if (element != null) {
			NodeList l = element.getElementsByTagName(name);
			if (l != null) {
				Node i = l.item(0);
				if (i != null) {
					return i.getNodeValue();
				}
			}
		}
		return null;
	}
	
	private Integer nullint(String txt) {
		if (txt == null || txt.isEmpty())
			return null;
		return Integer.parseInt(txt);
	}
	
	private Double nulldouble(String txt) {
		if (txt == null || txt.isEmpty())
			return null;
		return Double.parseDouble(txt);
	}
	
}
