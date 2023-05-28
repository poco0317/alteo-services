package com.etterna.services.datamodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.pk.UserSkillsetValuePk;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_skillset_values")
@Getter @Setter
public class UserSkillsetValue {
	
	@EmbeddedId
	private UserSkillsetValuePk id;
	
	@Column(name = "value", nullable = false)
	private Double value;
	
	public UserSkillsetValue() {}
	public UserSkillsetValue(User u, Skillset ss, Double val, Integer calcVersion) {
		this.id = new UserSkillsetValuePk(u, ss, calcVersion);
		this.value = val;
	}

}