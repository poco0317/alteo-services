package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.repo.PackRepository;


@Service
public class PackDao {

	@Autowired
	private PackRepository repo;

	@Transactional
	public Pack get(String name) {
		if (repo.existsById(name.toLowerCase())) {
			return repo.getById(name);
		} else {
			return repo.findById(name.toLowerCase()).orElse(null);
		}
	}
	
	@Transactional
	public boolean isRanked(String name) {
		return get(name) != null;
	}
	
	@Transactional
	public Pack getNewPackByName(String name) {
		return getNewPackByName(name, false);
	}
	
	@Transactional
	public Pack getNewPackByName(String name, boolean initCharts) {
		Pack p = get(name);
		if (p == null) {
			p = new Pack();
			p.setDisplayName(name);
			p.setName(name.toLowerCase());
			p.setRanked(new Date());
			p.setCharts(new HashSet<>());
			repo.save(p);
		}
		if (initCharts) {
			Hibernate.initialize(p.getCharts());
		}
		return p;
	}
	
	@Transactional
	public List<String> getAllNames() {
		return repo.findAll().stream().map(p -> p.getName()).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
	}

	/**
	 * Order charts for a pack by song name and then diff
	 * @param pack
	 * @return
	 */
	@Transactional
	public List<Chart> orderedChartList(Pack pack) {
		Set<Chart> charts = pack.getCharts();
		List<Chart> o = new ArrayList<>();
		if (charts == null || charts.isEmpty()) {
			return o;
		}
		
		Collections.sort(o, new Comparator<Chart>() {
			@Override
			public int compare(Chart c1, Chart c2) {
				int diff = c1.getTitle().compareToIgnoreCase(c2.getTitle());
				if (diff == 0) {
					diff = c1.getDifficulty().compareToIgnoreCase(c2.getDifficulty());
				}
				return diff;
			}
		});
		return o;
	}


}
