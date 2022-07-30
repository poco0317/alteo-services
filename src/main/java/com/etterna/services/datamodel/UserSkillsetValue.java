package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.pk.UserSkillsetValuePk;

@Entity
@Table(name = "user_skillset_values")
public class UserSkillsetValue {
	
	@EmbeddedId
	private UserSkillsetValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;
	
	public UserSkillsetValue() {}
	public UserSkillsetValue(User u, Skillset ss, Double val) {
		this.id = new UserSkillsetValuePk(u, ss);
		this.value = val;
	}

	public UserSkillsetValuePk getId() {
		return id;
	}

	public void setId(UserSkillsetValuePk id) {
		this.id = id;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}