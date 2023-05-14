package com.etterna.services.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.services.XmlProfileParsingService;
import com.etterna.services.dao.RankingDao;

@RestController
@RequestMapping("core")
public class GeneralServicesApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(GeneralServicesApiController.class);
	
	@Value("${etterna.note-info-folder-path}")
	private String rootNoteinfoPath;
	
	@Autowired
	private RankingDao chartRanking;
	
	@Autowired
	private XmlProfileParsingService xmls;
	
	private String auth(String bearer) {
		return bearer.replace("Bearer ","");
	}
	
	// handler for jwt authenticated xml upload
	@PostMapping("/xml/upload")
	public void uploadXml(@RequestHeader("Authorization") String authJwt, InputStream upload) {
		m_logger.info("API CALLED :: UploadXml");
		xmls.intakeProfile(upload, auth(authJwt));
	}
	
	@PostMapping("/rank")
	public void rank(InputStream upload) {
		m_logger.info("API CALLED :: Rank (rank pack)");
		
		ArrayList<String> songdatas = new ArrayList<>();
		final Pattern rootnamer = Pattern.compile("([^\\\\/]*)[\\\\/]");
		String packname = "No Pack Name";
		
		try (ZipInputStream zipin = new ZipInputStream(upload)) {
			
			ZipEntry entry = zipin.getNextEntry();
			while (entry != null) {
				
				String name = entry.getName();
				if (name.endsWith(".cache")) {
					String filename = new File(name).getName();
					Matcher rooter = rootnamer.matcher(name);
					if (rooter.find()) {
						packname = rooter.group(1);
					}
					
					m_logger.trace("File path {}", name);
					m_logger.info("Extracting filename {}", filename);
					
					if (name.contains("songdata")) {
						// should be song data
						String content = new String(zipin.readAllBytes(), StandardCharsets.UTF_8);
						songdatas.add(content);
					} else {
						// should be noteinfo
						OutputStream fileout = new FileOutputStream(rootNoteinfoPath + "/" + filename, false);
						fileout.write(zipin.readAllBytes());
						fileout.close();
					}
					
					
				} else {
					m_logger.info("Skipped filename {}", name);
				}
				
				
				zipin.closeEntry();
				entry = zipin.getNextEntry();
			}
			
			
		} catch (Exception e) {
			m_logger.warn("Attempted to parse upload and failed. {}", e);
		}
		
		chartRanking.queuePackForRanking(songdatas, packname);
	}
}
