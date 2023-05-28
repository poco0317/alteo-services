package com.etterna.services.controller.legacy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UploadScoreRequest {

	private String scorekey;
	private String ssr_norm;
	private String max_combo;
	private String valid;
	private String mods;
	
	private String miss;
	private String bad;
	private String good;
	private String great;
	private String perfect;
	private String marv;
	
	private String datetime;
	private String hitmine;
	private String held;
	private String letgo;
	private String ng;
	private String chartkey;
	private String rate;
	private String negsolo;
	private String nocc;
	private String calc_version;
	private String wife_version;
	private String topscore;
	private String hash;
	private String wife;
	private String wifePoints;
	private String judgeScale;
	private String machineGuid;
	private String grade;
	private String wifeGrade;
	private String replay_data;
}
