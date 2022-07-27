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
	
	public UserSkillsetValuePk() {}
	
	public UserSkillsetValuePk(User user, Skillset skillset) {
		this.user = user;
		this.skillset = skillset;
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

	@Override
	public int hashCode() {
		return Objects.hash(user, skillset);
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
		return Objects.equals(user, other.user) && Objects.equals(skillset, other.skillset);
	}
}
