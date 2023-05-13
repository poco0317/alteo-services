package com.etterna.services.repo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.Pack;
import com.etterna.services.datamodel.RankedChartkey;
import com.etterna.site.dto.ChartWithCount;
import com.etterna.site.dto.PackNameWithChartCount;
import com.etterna.site.dto.PackNameWithScoreCount;

@Repository
public interface ChartRepository extends JpaRepository<Chart, String> {

	List<Chart> findByCalcVersionLessThan(Integer calcVersion);
	List<RankedChartkey> findChartKeyByChartKeyNotNull();
	
	@Query("select new com.etterna.site.dto.PackNameWithChartCount(p.displayName, count(*)) from Pack p left join p.charts group by p.displayName")
	List<PackNameWithChartCount> getPackNamesWithChartCounts();
	
	@Query("select new com.etterna.site.dto.PackNameWithScoreCount(p.displayName, count(*)) from Pack p left join p.charts c left join c.scores hs where hs is not null group by p.displayName")
	List<PackNameWithScoreCount> getPackNamesWithScoreCounts();
	
	default Map<String, Integer> getPackNamesWithScoreCountsMap() {
		return getPackNamesWithScoreCounts().stream().collect(Collectors.toMap(PackNameWithScoreCount::getPack, PackNameWithScoreCount::getCount));
	}
	
	@Query("select c.chartKey from Chart c where ?1 member of c.packs")
	Set<String> getChartKeysInPack(Pack pack);
	
	@Query("select new com.etterna.site.dto.ChartWithCount(c, count(*)) from Chart c, HighScore hs where hs.chart = c and c.chartKey in :chartkeys group by c.chartKey")
	List<ChartWithCount> getChartsAndScoreCounts(Set<String> chartkeys);
	
	@Query("select new com.etterna.site.dto.ChartWithCount(c, 0L) from Chart c where c.chartKey in :chartkeys and not exists (select s from HighScore s where s.chart = c)")
	List<ChartWithCount> getChartsWithNoScores(Set<String> chartkeys);
}
