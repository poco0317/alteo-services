package com.etterna.services.datamodel.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.etterna.services.datamodel.Chart;

@Embeddable
public class ChartDiffValuePk implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chart_key")
	private Chart chart;
	
	@Column(name = "skillset", nullable = false)
	private String skillset;
	
	public ChartDiffValuePk() {}
	
	public ChartDiffValuePk(Chart chart, String skillset) {
		this.chart = chart;
		this.skillset = skillset;
	}

	public Chart getChart() {
		return chart;
	}

	public void setChart(Chart chart) {
		this.chart = chart;
	}

	public String getSkillset() {
		return skillset;
	}

	public void setSkillset(String skillset) {
		this.skillset = skillset;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chart, skillset);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChartDiffValuePk other = (ChartDiffValuePk) obj;
		return Objects.equals(chart, other.chart) && Objects.equals(skillset, other.skillset);
	}
}
