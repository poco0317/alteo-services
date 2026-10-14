package com.etterna.multi.data;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.etterna.calc.Skillset;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "multi_charts")
@Getter @Setter
public class MultiChart {

	@Id
	@Column(name = "chart_key", nullable = false)
	private String chartKey;

	@Column(name = "difficulty", nullable = false)
	private String difficulty;
	
	@Column(name = "calc_version", nullable = false)
	private Integer calcVersion = 0;
	
	@Lob
	@Column(name = "title", nullable = false)
	private String title;
	
	@Lob
	@Column(name = "translit_title")
	private String translitTitle;
	
	@Lob
	@Column(name = "subtitle")
	private String subtitle;
	
	@Lob
	@Column(name = "translit_subtitle")
	private String translitSubtitle;
	
	@Lob
	@Column(name = "artist")
	private String artist;
	
	@Lob
	@Column(name = "translit_artist")
	private String translitArtist;
	
	@Lob
	@Column(name = "credit")
	private String credit;
	
	@Column(name = "steps_type", nullable = false)
	private String stepsType;
	
	@Column(name = "ssr_1", nullable = false)
	private Double ss1Value = 0.0; // overall
	
	@Column(name = "ssr_2", nullable = false)
	private Double ss2Value = 0.0; // stream
	
	@Column(name = "ssr_3", nullable = false)
	private Double ss3Value = 0.0; // jumpstream
	
	@Column(name = "ssr_4", nullable = false)
	private Double ss4Value = 0.0; // handstream
	
	@Column(name = "ssr_5", nullable = false)
	private Double ss5Value = 0.0; // stamina
	
	@Column(name = "ssr_6", nullable = false)
	private Double ss6Value = 0.0; // jacks
	
	@Column(name = "ssr_7", nullable = false)
	private Double ss7Value = 0.0; // cj
	
	@Column(name = "ssr_8", nullable = false)
	private Double ss8Value = 0.0; // tech
	
	public void intakeSsrs(List<Float> ssrs) {
		ss1Value = ssrs.get(Skillset.OVERALL.ordinal()).doubleValue();
		ss2Value = ssrs.get(Skillset.STREAM.ordinal()).doubleValue();
		ss3Value = ssrs.get(Skillset.JUMPSTREAM.ordinal()).doubleValue();
		ss4Value = ssrs.get(Skillset.HANDSTREAM.ordinal()).doubleValue();
		ss5Value = ssrs.get(Skillset.STAMINA.ordinal()).doubleValue();
		ss6Value = ssrs.get(Skillset.JACKSPEED.ordinal()).doubleValue();
		ss7Value = ssrs.get(Skillset.CHORDJACK.ordinal()).doubleValue();
		ss8Value = ssrs.get(Skillset.TECHNICAL.ordinal()).doubleValue();
	}

	@Override
	public int hashCode() {
		return Objects.hash(chartKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MultiChart other = (MultiChart) obj;
		return Objects.equals(chartKey, other.chartKey);
	}
	
	

}
