package com.etterna.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import com.etterna.services.dao.UserDao;
import com.etterna.services.model.HighScore;
import com.etterna.services.model.User;
import com.etterna.services.opensearch.HighScoreIndexService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XmlProfileParsingService {
	
	@Autowired
	private HighScoreIndexService highScoreIndex;
	
	@Autowired
	private SessionService sessions;
	
	@Autowired
	private UserDao users;
	
	@Autowired
	private ApplicationContext ctx;
	
	private static final long XML_INTAKE_TIMER = 1000L * 10L; // 10 secs
	
	private static ConcurrentHashMap<String, byte[]> queuedXmls = new ConcurrentHashMap<>();
	
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
		return intakeProfile(in, user);
	}
	
	@Transactional
	public String intakeProfile(InputStream in, User user) {
		if (user == null) {
			m_logger.warn("User attempted to upload xml without being a user?");
			return "Unknown failure";
		}
		return add(in, user.getUsername());
	}
	
	@Scheduled(fixedDelay = XML_INTAKE_TIMER)
	void maintainXmlQueue() {
		Iterator<Entry<String, byte[]>> it = queuedXmls.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, byte[]> entry = it.next();
			User user = users.getByUserId(entry.getKey());
			if (user != null) {
				parseProfile(entry.getValue(), user);
			} else {
				m_logger.warn("Failed to get profile via ID {}", entry.getKey());
			}
			it.remove();
		}
	}
	
	private String add(InputStream in, String username) {
		if (queuedXmls.containsKey(username)) {
			m_logger.warn("Tried to add userId {} into XML Queue a second time... Skipped", username);
			return "Cannot add the same user twice while processing XMLs";
		} else {
			try {
				queuedXmls.put(username, in.readAllBytes());
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
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			EtternaXmlHandler handler = ctx.getBean(EtternaXmlHandler.class);
			handler.setUser(user);
			parser.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(bytes), "Windows-1252")), handler);
			
			List<HighScore> highscores = handler.getHighscores();
			
			m_logger.info("Finished parsing XML for user {} - {} ranked scores - Saving to DB", user.getUsername(), highscores.size());
			if (highscores.size() > 0)
				highScoreIndex.saveBulk(highscores, Refresh.False);
			m_logger.info("Saved highscores for user {} to DB", user.getUsername());
			
			return null; // success
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return "Parsing error: "+e.getMessage();
		}
	}
	
}
