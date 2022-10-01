package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.RankedChartkey;
import com.etterna.site.dto.PackNameWithChartCount;

@Repository
public interface ChartRepository extends JpaRepository<Chart, String> {

	List<Chart> findByCalcVersionLessThan(Integer calcVersion);
	List<Chart> findByPackName(String packName);
	List<RankedChartkey> findChartKeyByChartKeyNotNull();
	
	@Query("select distinct a.packName from Chart a")
	List<String> findDistinctPackName();
	
	@Query("select new com.etterna.site.dto.PackNameWithChartCount(a.packName, count(*)) from Chart a group by a.packName")
	List<PackNameWithChartCount> getPackNamesWithChartCounts();
}
