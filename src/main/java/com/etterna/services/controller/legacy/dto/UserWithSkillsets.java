package com.etterna.services.controller.legacy.dto;

import com.etterna.services.datamodel.User;

public class UserWithSkillsets {
	
	private User user;
	
	private Double overall;
	private Double stream;
	private Double jumpstream;
	private Double handstream;
	private Double stamina;
	private Double jackspeed;
	private Double chordjack;
	private Double technical;
	
	public Double getChordjack() {
		return chordjack;
	}
	public void setChordjack(Double chordjack) {
		this.chordjack = chordjack;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Double getOverall() {
		return overall;
	}
	public void setOverall(Double overall) {
		this.overall = overall;
	}
	public Double getStream() {
		return stream;
	}
	public void setStream(Double stream) {
		this.stream = stream;
	}
	public Double getJumpstream() {
		return jumpstream;
	}
	public void setJumpstream(Double jumpstream) {
		this.jumpstream = jumpstream;
	}
	public Double getHandstream() {
		return handstream;
	}
	public void setHandstream(Double handstream) {
		this.handstream = handstream;
	}
	public Double getStamina() {
		return stamina;
	}
	public void setStamina(Double stamina) {
		this.stamina = stamina;
	}
	public Double getJackspeed() {
		return jackspeed;
	}
	public void setJackspeed(Double jackspeed) {
		this.jackspeed = jackspeed;
	}
	public Double getTechnical() {
		return technical;
	}
	public void setTechnical(Double technical) {
		this.technical = technical;
	}

}
