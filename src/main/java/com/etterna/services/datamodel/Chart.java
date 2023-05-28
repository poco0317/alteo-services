package com.etterna.services.datamodel;

import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "charts")
@Getter @Setter
public class Chart {

	@Id
	@Column(name = "chart_key", nullable = false)
	private String chartKey;

	@Column(name = "difficulty", nullable = false)
	private String difficulty;
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion = 0;
	
	@Lob
	@Column(name = "title", nullable = false)
	private String title;
	
	@Lob
	@Column(name = "translit_title")
	private String translitTitle;
	
	@Lob
	@Column(name = "subtitle")
	private String subtitle;
	
	@Lob
	@Column(name = "translit_subtitle")
	private String translitSubtitle;
	
	@Lob
	@Column(name = "artist")
	private String artist;
	
	@Lob
	@Column(name = "translit_artist")
	private String translitArtist;
	
	@Lob
	@Column(name = "credit")
	private String credit;
	
	@Column(name = "steps_type", nullable = false)
	private String stepsType;

	@OneToMany(mappedBy = "id.chart")
	private Set<ChartDiffValue> diffValues;

	@OneToMany(mappedBy = "chart")
	private Set<HighScore> scores;
	
	@ManyToMany(mappedBy = "charts", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	private Set<Pack> packs;

	@Override
	public int hashCode() {
		return Objects.hash(chartKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chart other = (Chart) obj;
		return Objects.equals(chartKey, other.chartKey);
	}
	
	

}
