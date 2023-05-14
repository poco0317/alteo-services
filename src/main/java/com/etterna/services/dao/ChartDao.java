package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.datamodel.RankedChartkey;
import com.etterna.services.repo.ChartRepository;
import com.etterna.site.dto.ChartWithCount;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.ChartsInPackPagination;
import com.etterna.site.dto.PackContentSort;
import com.etterna.site.dto.PackNameWithChartCount;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;

@Service
public class ChartDao {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ChartDao.class);
	
	@Autowired
	private ChartRepository charts;
	
	@Autowired
	private PackDao packs;

	@Transactional
	public Chart get(String chartkey) {
		return get(chartkey, false);
	}
	
	@Transactional
	public Chart get(String chartkey, boolean initPacks) {
		Chart c = null;
		if (charts.existsById(chartkey)) {
			c = charts.getById(chartkey);
		} else {
			c = charts.findById(chartkey).orElse(null);
		}
		if (initPacks && c != null) {
			Hibernate.initialize(c.getPacks());
		}
		return c;
	}
	
	@Transactional
	public void save(Chart c) {
		charts.save(c);
	}
	
	@Transactional
	public long count() {
		return charts.count();
	}
	
	@Transactional
	public List<Chart> findByCalcVersionLessThan(int version) {
		return charts.findByCalcVersionLessThan(version);
	}
	
	@Transactional
	public List<RankedChartkey> findChartKeyByChartKeyNotNull() {
		return charts.findChartKeyByChartKeyNotNull();
	}
	
	@Transactional
	public PackNameWithChartCountPagination getPacksAndChartCounts(PacksSort ps, int page, int itemsPerPage) {
		List<PackNameWithChartCount> pncc = charts.getPackNamesWithChartCounts();
		
		int sliceStart = Math.min(itemsPerPage * (page-1), pncc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, pncc.size());
		m_logger.debug("pncc {} {} {}", sliceStart, sliceEnd, pncc.size());
		
		if (pncc.size() == 0) {
			return new PackNameWithChartCountPagination(pncc, 1, 1);
		}
		
		Map<String, Integer> packScoreCounts = charts.getPackNamesWithScoreCountsMap();
		pncc.forEach(c -> {
			c.setScoreCount(packScoreCounts.getOrDefault(c.getPack(), 0));
		});
		
		Collections.sort(pncc, new Comparator<PackNameWithChartCount>() {
			@Override
			public int compare(PackNameWithChartCount a, PackNameWithChartCount b) {
				switch (ps) {
					case COUNT:
					case AVG:
					case SCORES:
					{
						int o = 0;
						switch (ps) {
							default:
							case COUNT:
								o = b.getCount().compareTo(a.getCount());
								break;
							case SCORES:
								o = b.getScoreCount().compareTo(a.getScoreCount());
								break;
							case AVG:
								o = b.getAverageScores().compareTo(a.getAverageScores());
								break;
						}
						if (o != 0) {
							return o;
						}
						// fallthrough to compare by name if equal counts
					}
					case NAME:
					default:
						return a.getPack().compareToIgnoreCase(b.getPack());
				}
			}
		});
		return new PackNameWithChartCountPagination(pncc.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(pncc.size() / (float)itemsPerPage)));
	}
	
	@Transactional
	public List<Chart> getChartsInPack(String packName) {
		Pack pack = packs.get(packName);
		if (pack == null) {
			return new ArrayList<>();
		}
		
		List<Chart> chartList = packs.orderedChartList(pack);
		
		return chartList;
	}
	
	@Transactional
	public ChartsInPackPagination getChartsInPackPagination(String pack, PackContentSort ps, int page, int itemsPerPage) {
		Set<String> chartkeys = charts.getChartKeysInPack(packs.get(pack));
		List<ChartWithCount> cwc = charts.getChartsAndScoreCounts(chartkeys);
		cwc.addAll(charts.getChartsWithNoScores(chartkeys));
		
		int sliceStart = Math.min(itemsPerPage * (page-1), cwc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, cwc.size());
		m_logger.debug("cwc {} {} {}", sliceStart, sliceEnd, cwc.size());
		
		if (cwc.size() == 0) {
			return new ChartsInPackPagination(new ArrayList<>(), 1, 1);
		}
		
		List<ChartWithSkillsets> cip = cwc.stream().map(c -> new ChartWithSkillsets(c.getChart(), c.getCount().intValue())).collect(Collectors.toList());
		
		Collections.sort(cip, new Comparator<ChartWithSkillsets>() {
			@Override
			public int compare(ChartWithSkillsets a, ChartWithSkillsets b) {
				int o = 0;
				switch (ps) {
					case SCORES:
						o = b.getScoreCount().compareTo(a.getScoreCount());
						break;
					case OVERALL:
						o = b.getOverall().compareTo(a.getOverall());
						break;
					case STREAM:
						o = b.getStream().compareTo(a.getStream());
						break;
					case JUMPSTREAM:
						o = b.getJumpstream().compareTo(a.getJumpstream());
						break;
					case HANDSTREAM:
						o = b.getHandstream().compareTo(a.getHandstream());
						break;
					case STAMINA:
						o = b.getStamina().compareTo(a.getStamina());
						break;
					case JACKSPEED:
						o = b.getJackspeed().compareTo(a.getJackspeed());
						break;
					case CHORDJACK:
						o = b.getChordjack().compareTo(a.getChordjack());
						break;
					case TECHNICAL:
						o = b.getTechnical().compareTo(a.getTechnical());
						break;
					case NAME:
					default: {
						int oo = a.getChart().getTitle().compareToIgnoreCase(b.getChart().getTitle());
						if (oo == 0) {
							return a.getChart().getDifficulty().compareToIgnoreCase(b.getChart().getDifficulty());
						} else {
							return oo;
						}
					}
				}
				if (o == 0) {
					int oo = a.getChart().getTitle().compareToIgnoreCase(b.getChart().getTitle());
					if (oo == 0) {
						return a.getChart().getDifficulty().compareToIgnoreCase(b.getChart().getDifficulty());
					} else {
						return oo;
					}
				} else {
					return o;
				}
			}
		});
		
		return new ChartsInPackPagination(cip.subList(sliceStart, sliceEnd), page, Math.max(1, (int)Math.ceil(cip.size() / (float)itemsPerPage)));
	}

}
