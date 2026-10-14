package com.etterna.services.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "charts")
@Getter @Setter
public class Chart {
	
	@Id
	@Column(name = "chart_id")
	private Long chartId;
	
	private String chartkey;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "song.songkey")
	private Song song;
	
	private String difficulty;
	private Double msd;
}
