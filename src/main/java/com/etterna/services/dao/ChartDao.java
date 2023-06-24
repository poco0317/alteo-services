package com.etterna.services.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.opensearch.client.opensearch._types.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.calc.CalcManager;
import com.etterna.services.model.Chart;
import com.etterna.services.model.Pack;
import com.etterna.services.opensearch.ChartDiffValueIndexService;
import com.etterna.services.opensearch.ChartIndexService;
import com.etterna.site.dto.ChartWithCount;
import com.etterna.site.dto.ChartWithSkillsets;
import com.etterna.site.dto.ChartsInPackPagination;
import com.etterna.site.dto.PackContentSort;
import com.etterna.site.dto.PackNameWithChartCount;
import com.etterna.site.dto.PackNameWithChartCountPagination;
import com.etterna.site.dto.PacksSort;
import com.etterna.util.LogRuntime;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChartDao {
		
	@Autowired
	private ChartIndexService chartsIndex;
	
	@Autowired
	private ChartDiffValueIndexService diffIndex;
	
	@Autowired
	private PackDao packs;
	
	@Autowired
	private CalcManager calc;
	
	@Transactional
	public Chart get(String chartkey) {
		return chartsIndex.findById(chartkey);
	}
	
	public Map<String, Chart> get(Collection<String> chartkeys) {
		return chartsIndex.findChartsByChartKeyMap(chartkeys);
	}
	
	@Transactional
	public void save(Chart c) {
		chartsIndex.save(c, Refresh.True);
	}
	
	@Transactional
	public boolean saveBulk(List<Chart> newCharts) {
		return chartsIndex.saveBulk(newCharts, Refresh.True);
	}
	
	@Transactional
	public long count() {
		return chartsIndex.count();
	}
	
	@Transactional
	public List<Chart> findByCalcVersionLessThan(int version) {
		return chartsIndex.findByCalcVersionLessThan(version);
	}
	
	@Transactional
	public List<Chart> findByCalcVersionNotEqual(int version) {
		return chartsIndex.findByCalcVersionNot(version);
	}
	
	@Transactional
	public Set<String> findChartKeyByChartKeyNotNull() {
		return chartsIndex.findChartKeyByChartKeyNotNull();
	}
	
	@Transactional
	public PackNameWithChartCountPagination getPacksAndChartCounts(PacksSort ps, int page, int itemsPerPage) {
		List<PackNameWithChartCount> pncc = chartsIndex.getPackNamesWithChartCounts();
		
		int sliceStart = Math.min(itemsPerPage * (page-1), pncc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, pncc.size());
		
		if (pncc.size() == 0) {
			return new PackNameWithChartCountPagination(pncc, 1, 1);
		}
		
		Map<String, Integer> packScoreCounts = chartsIndex.getPackNamesWithScoreCountsMap();
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
		Set<String> chartkeys = chartsIndex.getChartKeysInPack(packs.get(pack));
		List<ChartWithCount> cwc = chartsIndex.getChartsAndScoreCounts(chartkeys);
		//cwc.addAll(chartsIndex.getChartsWithNoScores(chartkeys));
		
		int sliceStart = Math.min(itemsPerPage * (page-1), cwc.size()-1);
		int sliceEnd = Math.min(itemsPerPage * page, cwc.size());
		
		if (cwc.size() == 0) {
			return new ChartsInPackPagination(new ArrayList<>(), 1, 1);
		}
		
		List<ChartWithSkillsets> cip = cwc
				.stream()
				.map(c -> new ChartWithSkillsets(c.getChart(), diffIndex.getDiffValues(c.getChart()), c.getCount().intValue()))
				.collect(Collectors.toList());
		
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
	
	@Transactional
	public ChartWithSkillsets getChartWithSkillsets(String chartKey) {
		return chartsIndex.findChartWithSkillsets(chartKey, calc.getCalcVersion());
	}
	
	@LogRuntime
	@Transactional
	public Map<String, ChartWithSkillsets> getChartsWithSkillsetsMap(Collection<String> chartKeys) {
		return chartsIndex.findChartsWithSkillsetsMap(chartKeys, calc.getCalcVersion());
	}

}
