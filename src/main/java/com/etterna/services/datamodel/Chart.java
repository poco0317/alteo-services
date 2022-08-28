package com.etterna.services.datamodel;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "charts")
public class Chart {

	@Id
	@Column(name = "chart_key", nullable = false)
	private String chartKey;

	@Column(name = "difficulty", nullable = false)
	private String difficulty;

	@Column(name = "song_name", nullable = true)
	private String songName;

	@Column(name = "pack_name", nullable = true)
	private String packName;
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion;

	@OneToMany(mappedBy = "id.chart")
	private Set<ChartDiffValue> diffValues;

	@OneToMany(mappedBy = "chart")
	private Set<HighScore> scores;
	
	public String getChartKey() {
		return chartKey;
	}

	public void setChartKey(String chartKey) {
		this.chartKey = chartKey;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getPackName() {
		return packName;
	}

	public void setPackName(String packName) {
		this.packName = packName;
	}

	public Set<ChartDiffValue> getDiffValues() {
		return diffValues;
	}

	public void setDiffValues(Set<ChartDiffValue> diffValues) {
		this.diffValues = diffValues;
	}

	public Set<HighScore> getScores() {
		return scores;
	}

	public void setScores(Set<HighScore> scores) {
		this.scores = scores;
	}

	public Integer getCalcVersion() {
		return calcVersion;
	}

	public void setCalcVersion(Integer calcVersion) {
		this.calcVersion = calcVersion;
	}

}
