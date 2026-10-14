package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etterna.services.data.Chart;
import com.etterna.services.repo.projection.ChartProjection;

public interface ChartRepository extends JpaRepository<Chart, Long> {
	
	
	@Query(value = 
			"select "
			+ "s.songkey, c.chartkey, s.songname, s.subtitle, s.author, s.artist, difficulty, msd "
			+ "from song s, charts c "
			+ "where s.songkey = c.song_songkey and c.chartkey = :chartkey",
			nativeQuery = true)
	public List<ChartProjection> getChartsByChartkey(String chartkey);
	
		
}
