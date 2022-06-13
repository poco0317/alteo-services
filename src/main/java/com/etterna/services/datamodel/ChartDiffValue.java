package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.services.datamodel.pk.ChartDiffValuePk;

/**
 * DiffValues represent the 93% SSR of a file, its base difficulty
 */
@Entity
@Table(name = "chart_diff_values")
public class ChartDiffValue {
	
	@EmbeddedId
	private ChartDiffValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;

	public ChartDiffValuePk getId() {
		return id;
	}

	public void setId(ChartDiffValuePk id) {
		this.id = id;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
