package com.etterna.services.datamodel;

import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "scores")
@Getter @Setter
public class HighScore {
	
	@Id
	@Column(name = "score_key", nullable = false)
	private String scoreKey;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chart_key", nullable = false)
	private Chart chart;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(name = "manual_invalid")
	private Boolean manuallyInvalid = false;
	
	@Column(name = "nerf_multiplier")
	private Double nerfMultiplier = 1.0;
	
	@OneToMany(mappedBy = "id.score")
	private Set<ScoreSpecificValue> ssrs;
	
	// score attribs here

	@Column(name = "ssr_norm")
	private Integer ssrNorm;
	@Column(name = "music_rate")
	private Integer musicRate;
	@Column(name = "max_combo")
	private Integer maxCombo;
	@Column(name = "valid")
	private Integer etternaValid;
	@Column(name = "mods")
	private String modString;
	
	@Column(name = "miss_cnt")
	private Integer missCount;
	@Column(name = "bad_cnt")
	private Integer badCount;
	@Column(name = "good_cnt")
	private Integer goodCount;
	@Column(name = "great_cnt")
	private Integer greatCount;
	@Column(name = "perf_cnt")
	private Integer perfCount;
	@Column(name = "marv_cnt")
	private Integer marvCount;
	@Column(name = "hitmine_cnt")
	private Integer hitMineCount;
	@Column(name = "held_cnt")
	private Integer heldCount;
	@Column(name = "ng_cnt")
	private Integer ngCount;
	@Column(name = "letgo_cnt")
	private Integer letgoCount;
	@Column(name = "date_str")
	private String dateStr;
	
	@Column(name = "negbpm")
	private Boolean negBpm;
	@Column(name = "nocc")
	private Boolean noCC;
	
	@Column(name = "calc_vers")
	private Integer calcVersion = 0;
	@Column(name = "wife_vers")
	private Integer wifeVersion;
	@Column(name = "top_score")
	private Integer topScore;
	@Column(name = "brittle_key")
	private String brittleKey;
	@Column(name = "machine_guid")
	private String guid;
	
	@Column(name = "wife_perc")
	private Double wifePercent;
	@Column(name = "wife_pts")
	private Double wifePoints;
	@Column(name = "judge")
	private Double judgeScale;
	@Column(name = "grade")
	private String grade;
	@Column(name = "wife_grade")
	private String wifeGrade;
	
	@Override
	public int hashCode() {
		return Objects.hash(scoreKey);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HighScore other = (HighScore) obj;
		return Objects.equals(scoreKey, other.scoreKey);
	}
	
}
