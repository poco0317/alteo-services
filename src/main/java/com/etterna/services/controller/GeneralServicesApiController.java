package com.etterna.services.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.services.XmlProfileParsingService;
import com.etterna.services.dao.RankingDao;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("core")
@Slf4j
public class GeneralServicesApiController {
		
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
		
		List<String> songdatas = new ArrayList<>();
		Map<String, byte[]> noteinfos = new HashMap<>();
		final Pattern rootnamer = Pattern.compile("([^\\\\/]*)[\\\\/]");
		String packname = "No Pack Name";
		
		try (ZipInputStream zipin = new ZipInputStream(upload)) {
			m_logger.info("Extracting pack for ranking");
			
			ZipEntry entry = zipin.getNextEntry();
			while (entry != null) {
				
				String name = entry.getName();
				if (name.endsWith(".cache") && !name.equalsIgnoreCase(".cache")) {
					String filename = new File(name).getName();
					Matcher rooter = rootnamer.matcher(name);
					if (rooter.find()) {
						packname = rooter.group(1);
					}
					
					m_logger.trace("File path {}", name);
					m_logger.trace("Extracting filename {}", filename);
					
					if (name.contains("songdata")) {
						// should be song data
						String content = new String(zipin.readAllBytes(), StandardCharsets.UTF_8);
						songdatas.add(content);
					} else {
						// should be noteinfo
						noteinfos.put(filename.replaceAll(".cache", ""), zipin.readAllBytes());
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
		
		chartRanking.queuePackForRanking(songdatas, noteinfos, packname);
	}
}
