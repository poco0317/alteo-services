package com.etterna.services.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.etterna.services.data.Pack;
import com.etterna.services.repo.projection.ChartProjection;
import com.etterna.services.repo.projection.PackListProjection;

public interface PackRepository extends JpaRepository<Pack, String> {

	
	@Query(value="select packs_packname from packs_has_songs where song_songkey = :songkey", nativeQuery = true)
	public List<String> getPacksContainingSong(String songkey);
	
	public List<Pack> findByPackname(String packname);
	
	@Query(value = "select "
			+ "packs_packname as packname, "
			+ "count(*) as chartcount, "
			+ "(select count(*) from scores2 s "
			+ " where s.songkey in ("
			+ "  select song_songkey from packs_has_songs pp where pp.packs_packname = p.packs_packname)"
			+ ") as scorecount "
			+ "from packs_has_songs p group by packs_packname order by packs_packname asc",
			nativeQuery = true)
	public List<PackListProjection> getPackListing();
	
	@Query(value = "select "
			+ "c.song_songkey, c.chartkey, c.difficulty, c.msd, s.songname, s.subtitle, s.author, s.artist, "
			+ "(select count(*) "
			+ "  from scores2 sc where sc.chartkey = c.chartkey"
			+ ") as scorecount "
			+ "from charts c, song s "
			+ "where c.song_songkey = s.songkey and c.song_songkey in ("
			+ "  select song_songkey from packs_has_songs where packs_packname = :packname"
			+ ") order by s.songname asc",
			nativeQuery = true)
	public List<ChartProjection> getPackChartlist(String packname);

}
