package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.services.datamodel.pk.ScoreSpecificValuePk;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "score_specific_values")
@Getter @Setter
public class ScoreSpecificValue {

	@EmbeddedId
	private ScoreSpecificValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;

}
