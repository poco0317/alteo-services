package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.services.datamodel.pk.ScoreSpecificValuePk;

@Entity
@Table(name = "score_specific_values")
public class ScoreSpecificValue {

	@EmbeddedId
	private ScoreSpecificValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;

	public ScoreSpecificValuePk getId() {
		return id;
	}

	public void setId(ScoreSpecificValuePk id) {
		this.id = id;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
