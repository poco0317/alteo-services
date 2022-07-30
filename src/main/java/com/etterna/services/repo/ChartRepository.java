package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.etterna.services.datamodel.Chart;
import com.etterna.services.datamodel.RankedChartkey;

@Repository
public interface ChartRepository extends JpaRepository<Chart, String> {

	List<Chart> findByCalcVersionLessThan(Integer calcVersion);
	List<Chart> findByPackName(String packName);
	List<RankedChartkey> findChartKeyByChartKeyNotNull();
}
