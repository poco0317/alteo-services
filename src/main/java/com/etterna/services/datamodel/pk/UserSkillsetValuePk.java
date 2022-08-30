package com.etterna.services.datamodel.pk;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.etterna.calc.Skillset;
import com.etterna.services.datamodel.User;

@Embeddable
public class UserSkillsetValuePk implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "skillset", nullable = false)
	private Skillset skillset;
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion;
	
	public UserSkillsetValuePk() {}
	
	public UserSkillsetValuePk(User user, Skillset skillset, Integer calcVersion) {
		this.user = user;
		this.skillset = skillset;
		this.calcVersion = calcVersion;
	}

	public Skillset getSkillset() {
		return skillset;
	}

	public void setSkillset(Skillset skillset) {
		this.skillset = skillset;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getCalcVersion() {
		return calcVersion;
	}

	public void setCalcVersion(Integer calcVersion) {
		this.calcVersion = calcVersion;
	}

	@Override
	public int hashCode() {
		return Objects.hash(calcVersion, skillset, user);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSkillsetValuePk other = (UserSkillsetValuePk) obj;
		return Objects.equals(calcVersion, other.calcVersion) && skillset == other.skillset
				&& Objects.equals(user, other.user);
	}
	
}
