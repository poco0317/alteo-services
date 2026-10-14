package com.etterna.services.data;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scores")
@Getter @Setter
public class Score {
	
	@EmbeddedId
	private ScoreId scoreId;
	
	//private Long id;
	private String scorekey;
	
	@Column(name = "Overall")
	private Float overall;
	
	@Column(name = "Stream")
	private Float stream;
	
	@Column(name = "Jumpstream")
	private Float jumpstream;
	
	@Column(name = "Handstream")
	private Float handstream;
	
	@Column(name = "JackSpeed")
	private Float jackspeed;
	
	@Column(name = "Chordjack")
	private Float chordjack;
	
	@Column(name = "Technical")
	private Float technical;
	
	@Column(name = "Stamina")
	private Float stamina;
	
	private Float wifescore;
	private Long maxcombo;
	private Boolean valid;
	private String modifiers;
	private Long miss;
	private Long bad;
	private Long good;
	private Long great;
	private Long perfect;
	private Long marv;
	private Timestamp datetime;
	private Long hitmine;
	private Long held;
	private Long letgo;
	private Long missedhold;
	private Integer nocc;
	private Integer calcversion;
	private Integer topscore;
	//private String replay;
	
	@Column(name = "machineGuid")
	private String machineGuid;
	
	@Column(name = "judgeScale")
	private Float judgeScale;
	
	@Column(name = "wifeGrade")
	private String wifeGrade;
	
	@Column(name = "validationKey")
	private String validationKey;
	
	@Column(name = "scoreValue")
	private Float scoreValue;
	private Long wifeversion;
}
