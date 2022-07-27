package com.etterna.services.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.services.dao.ChartDao;

@RestController
@RequestMapping("core")
public class GeneralServicesApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(GeneralServicesApiController.class);
	
	@Value("${etterna.note-info-folder-path}")
	private String rootNoteinfoPath;
	
	@Autowired
	private ChartDao charts;
	
	@PostMapping("/rank")
	public void rank(InputStream upload) {
		
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
		
		parseSongDatas(songdatas, packname);
	}
	
	private void parseSongDatas(List<String> songdatas, String packname) {
		final Pattern titlepattern = Pattern.compile(";[\\s]*#TITLE:([^;]+);");
		final Pattern ckpattern = Pattern.compile(";[\\s]*#CHARTKEY:([^;]+);");
		final Pattern diffpattern = Pattern.compile(";[\\s]*#DIFFICULTY:([^;]+);");
		for (String contents : songdatas) {
			Matcher titlematch = titlepattern.matcher(contents);
			Matcher ckmatcher = ckpattern.matcher(contents);
			Matcher diffmatcher = diffpattern.matcher(contents);
			
			if (!titlematch.find()) {
				m_logger.warn("Skipped song due to missing title in {}", packname);
				m_logger.warn("{}", contents);
			} else {
				String songname = titlematch.group(1);
				List<String> cks = new LinkedList<>();
				while (ckmatcher.find()) {
					cks.add(ckmatcher.group(1));
				}
				List<String> diffs = new LinkedList<>();
				while (diffmatcher.find()) {
					diffs.add(diffmatcher.group(1));
				}
				if (diffs.size() != cks.size()) {
					m_logger.warn("Chartkey and Diff count is not the same!!! ck {} - diff {} - Skipped ranking {}", cks.size(), diffs.size(), songname);
				} else {
					m_logger.info("Found {} cks and {} diffs in song {} - pack {}", cks.size(), diffs.size(), songname, packname);
					for (int i = 0; i < cks.size(); i++) {
						charts.rankChart(cks.get(i), diffs.get(i), packname, songname);
					}
				}
			}
		}
	}
	
	

}
