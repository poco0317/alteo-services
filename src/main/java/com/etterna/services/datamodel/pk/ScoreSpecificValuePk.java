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
	
	public ScoreSpecificValuePk() {}
	
	public ScoreSpecificValuePk(HighScore chart, Skillset skillset) {
		this.score = chart;
		this.skillset = skillset;
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

	@Override
	public int hashCode() {
		return Objects.hash(score, skillset);
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
		return Objects.equals(score, other.score) && Objects.equals(skillset, other.skillset);
	}
}