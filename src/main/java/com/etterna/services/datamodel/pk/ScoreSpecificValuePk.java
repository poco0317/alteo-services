package com.etterna.services.datamodel.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.HighScore;

@Embeddable
public class ScoreSpecificValuePk implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "score_key")
	private HighScore score;
	
	@Column(name = "skillset", nullable = false)
	private Skillset skillset;
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion;
	
	public ScoreSpecificValuePk() {}
	
	public ScoreSpecificValuePk(HighScore chart, Skillset skillset, Integer calcVersion) {
		this.score = chart;
		this.skillset = skillset;
		this.calcVersion = calcVersion;
	}
	
	public HighScore getScore() {
		return score;
	}

	public void setScore(HighScore score) {
		this.score = score;
	}

	public Skillset getSkillset() {
		return skillset;
	}

	public void setSkillset(Skillset skillset) {
		this.skillset = skillset;
	}

	public Integer getCalcVersion() {
		return calcVersion;
	}

	public void setCalcVersion(Integer calcVersion) {
		this.calcVersion = calcVersion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(calcVersion, score, skillset);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScoreSpecificValuePk other = (ScoreSpecificValuePk) obj;
		return Objects.equals(calcVersion, other.calcVersion) && Objects.equals(score, other.score)
				&& skillset == other.skillset;
	}

}