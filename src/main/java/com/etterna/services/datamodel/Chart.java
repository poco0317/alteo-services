package com.etterna.services.datamodel;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion = 0;
	
	@Column(name = "title", nullable = false)
	private String title;
	
	@Column(name = "translit_title")
	private String translitTitle;
	
	@Column(name = "subtitle")
	private String subtitle;
	
	@Column(name = "translit_subtitle")
	private String translitSubtitle;
	
	@Column(name = "artist")
	private String artist;
	
	@Column(name = "translit_artist")
	private String translitArtist;
	
	@Column(name = "credit")
	private String credit;

	@OneToMany(mappedBy = "id.chart")
	private Set<ChartDiffValue> diffValues;

	@OneToMany(mappedBy = "chart")
	private Set<HighScore> scores;
	
	@ManyToMany(mappedBy = "charts", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	private Set<Pack> packs;
	
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

	public Set<Pack> getPacks() {
		return packs;
	}

	public void setPacks(Set<Pack> packs) {
		this.packs = packs;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTranslitTitle() {
		return translitTitle;
	}

	public void setTranslitTitle(String translitTitle) {
		this.translitTitle = translitTitle;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getTranslitSubtitle() {
		return translitSubtitle;
	}

	public void setTranslitSubtitle(String translitSubtitle) {
		this.translitSubtitle = translitSubtitle;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTranslitArtist() {
		return translitArtist;
	}

	public void setTranslitArtist(String translitArtist) {
		this.translitArtist = translitArtist;
	}

	public String getCredit() {
		return credit;
	}

	public void setCredit(String credit) {
		this.credit = credit;
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
