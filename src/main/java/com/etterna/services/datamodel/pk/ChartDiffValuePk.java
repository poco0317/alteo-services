package com.etterna.services.datamodel.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.Chart;

@Embeddable
public class ChartDiffValuePk implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chart_key")
	private Chart chart;
	
	@Column(name = "skillset", nullable = false)
	private Skillset skillset;

	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion;
	
	public ChartDiffValuePk() {}
	
	public ChartDiffValuePk(Chart chart, Skillset skillset, Integer calcVersion) {
		this.chart = chart;
		this.skillset = skillset;
		this.calcVersion = calcVersion;
	}

	public Chart getChart() {
		return chart;
	}

	public void setChart(Chart chart) {
		this.chart = chart;
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
		return Objects.hash(calcVersion, chart, skillset);
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
		return Objects.equals(calcVersion, other.calcVersion) && Objects.equals(chart, other.chart)
				&& skillset == other.skillset;
	}

}
