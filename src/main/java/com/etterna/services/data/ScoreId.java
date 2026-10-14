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
public class ScoreId implements Serializable {
	private static final long serialVersionUID = -6963724067233903267L;

	@Column(name = "user_chart_rate_user_chart_userid")
	private Long userChartRateUserChartUserid;
	
	@Column(name = "user_chart_rate_user_chart_chartkey")
	private String userChartRateUserChartChartkey;
	
	@Column(name = "user_chart_rate_rate")
	private Double userChartRateRate;
	
	@Column(name = "user_chart_rate_user_chart_songkey")
	private String userChartRateUserChartSongkey;

}
