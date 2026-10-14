package com.etterna.services.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter
@EqualsAndHashCode
public class UserChartRateId implements Serializable {
	private static final long serialVersionUID = -8344945197426584680L;

	@Column(name = "user_chart_userid")
	private Long userChartUserid;
	
	@Column(name = "user_chart_chartkey")
	private String userChartChartkey;
	
	@Column(name = "user_chart_songkey")
	private String userChartSongkey;
	
	@Column(name = "rate")
	private Double rate;

}
