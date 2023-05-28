package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.pk.ChartDiffValuePk;

import lombok.Getter;
import lombok.Setter;

/**
 * DiffValues represent the 93% SSR of a file, its base difficulty
 */
@Entity
@Table(name = "chart_diff_values")
@Getter @Setter
public class ChartDiffValue {
	
	@EmbeddedId
	private ChartDiffValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;
	
	public ChartDiffValue() {}
	public ChartDiffValue(Chart c, Double value, Skillset ss, Integer calcVersion) {
		this.id = new ChartDiffValuePk(c, ss, calcVersion);
		this.value = value;
	}

}
