package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.model.Chart;
import com.etterna.services.model.Pack;
import com.etterna.services.opensearch.ChartIndexService;
import com.etterna.services.opensearch.PackIndexService;

@Service
public class PackDao {

	@Autowired
	private PackIndexService packIndex;
	
	@Autowired
	private ChartIndexService chartIndex;

	@Transactional
	public Pack get(String name) {
		return packIndex.findById(name.toLowerCase());
	}
	
	@Transactional
	public boolean isRanked(String name) {
		return get(name) != null;
	}
	
	@Transactional
	public Pack getNewPackByName(String name) {
		Pack p = get(name);
		if (p == null) {
			p = new Pack();
			p.setDisplayName(name);
			p.setName(name.toLowerCase());
			p.setRanked(new Date());
			p.setChartKeys(null);
			packIndex.save(p, Refresh.True);
		}
		return p;
	}
	
	public void save(Pack pack) {
		packIndex.save(pack, Refresh.True);
	}
	
	@Transactional
	public List<String> getAllNames() {
		return packIndex.findAll().stream().map(p -> p.getName()).sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
	}

	/**
	 * Order charts for a pack by song name and then diff
	 * @param pack
	 * @return
	 */
	@Transactional
	public List<Chart> orderedChartList(Pack pack) {
		Set<Chart> charts = chartIndex.getChartsInPack(pack);
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

	@Transactional
	public List<Pack> findPacksByChart(String chartkey) {
		return packIndex.findByChartKey(chartkey);
	}


}
