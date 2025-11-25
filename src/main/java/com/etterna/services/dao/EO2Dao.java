package com.etterna.services.dao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EO2Dao {
	
	private static class Pack {
		private String id;
		private String name;
		private List<Song> songs = new ArrayList<>();
	}
	
	private static class Song {
		private String id;
		private String name;
		private String name_translit;
	}
	
	public void refreshdata() {

		/*
		Map<String, Object> rmap = getreq("https://api.etternaonline.com/api/packs?page=1&limit=99999&sort=name");
		
		List<Object> l = (List<Object>)rmap.get("data");
		List<Pack> packs = new ArrayList<>();
		for (Object o : l) {
			Pack p = new Pack();
			Map<String, Object> mm = (Map<String, Object>)o;
			m_logger.info("id : {} ... name {}", mm.get("id"), mm.get("name"));
			
			p.id = Integer.toString((Integer)mm.get("id"));
			p.name = (String)mm.get("name");
			packs.add(p);
		}
		
		for (Pack p : packs) {
			m_logger.info("Updating songs for pack {} {}", p.id, p.name);
			
			Map<String, Object> pmap = getreq("https://api.etternaonline.com/api/packs/"+p.id+"/songs?page=1&limit=99999&sort=name");
			List<Object> sl = (List<Object>)pmap.get("data");
			for (Object o : sl) {
				Song s = new Song();
				Map<String, Object> mm = (Map<String, Object>)o;
				m_logger.info(" song : id {} ... name {}", mm.get("id"), mm.get("name"));
				s.id = Integer.toString((Integer)mm.get("id"));
				s.name = (String)mm.get("name");
				s.name_translit = (String)mm.get("name_translit");
				p.songs.add(s);
			}
			break;
		}
		m_logger.info("packs {}", packs.size());
		m_logger.info("songs {}", packs.get(0).songs.size());
		*/
	}
	
	private Map<String, Object> getreq(String url) {
		HttpClient c = HttpClient.newHttpClient();
		
		HttpRequest req = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.GET()
			.build();
		
		String respdata = null;
		
		try {
			HttpResponse<String> resp = c.send(req, HttpResponse.BodyHandlers.ofString());
			respdata = resp.body();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		
		if (respdata == null) {
			m_logger.info("Got no response from server for EO2 API {}", url);
			return null;
		}
		
		try {
			return new ObjectMapper().readValue(respdata, HashMap.class);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return null;
		}
	}

}
