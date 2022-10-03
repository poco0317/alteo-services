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

@Entity
@Table(name = "scores")
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
	
	public String getScoreKey() {
		return scoreKey;
	}
	public void setScoreKey(String scoreKey) {
		this.scoreKey = scoreKey;
	}
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Integer getSsrNorm() {
		return ssrNorm;
	}
	public void setSsrNorm(Integer ssrNorm) {
		this.ssrNorm = ssrNorm;
	}
	public Integer getMusicRate() {
		return musicRate;
	}
	public void setMusicRate(Integer musicRate) {
		this.musicRate = musicRate;
	}
	public Integer getMaxCombo() {
		return maxCombo;
	}
	public void setMaxCombo(Integer maxCombo) {
		this.maxCombo = maxCombo;
	}
	public Integer getEtternaValid() {
		return etternaValid;
	}
	public void setEtternaValid(Integer etternaValid) {
		this.etternaValid = etternaValid;
	}
	public String getModString() {
		return modString;
	}
	public void setModString(String modString) {
		this.modString = modString;
	}
	public Integer getMissCount() {
		return missCount;
	}
	public void setMissCount(Integer missCount) {
		this.missCount = missCount;
	}
	public Integer getBadCount() {
		return badCount;
	}
	public void setBadCount(Integer badCount) {
		this.badCount = badCount;
	}
	public Integer getGoodCount() {
		return goodCount;
	}
	public void setGoodCount(Integer goodCount) {
		this.goodCount = goodCount;
	}
	public Integer getGreatCount() {
		return greatCount;
	}
	public void setGreatCount(Integer greatCount) {
		this.greatCount = greatCount;
	}
	public Integer getPerfCount() {
		return perfCount;
	}
	public void setPerfCount(Integer perfCount) {
		this.perfCount = perfCount;
	}
	public Integer getMarvCount() {
		return marvCount;
	}
	public void setMarvCount(Integer marvCount) {
		this.marvCount = marvCount;
	}
	public Integer getHitMineCount() {
		return hitMineCount;
	}
	public void setHitMineCount(Integer hitMineCount) {
		this.hitMineCount = hitMineCount;
	}
	public Integer getHeldCount() {
		return heldCount;
	}
	public void setHeldCount(Integer heldCount) {
		this.heldCount = heldCount;
	}
	public Integer getNgCount() {
		return ngCount;
	}
	public void setNgCount(Integer ngCount) {
		this.ngCount = ngCount;
	}
	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	public Boolean getNegBpm() {
		return negBpm;
	}
	public void setNegBpm(Boolean negBpm) {
		this.negBpm = negBpm;
	}
	public Boolean getNoCC() {
		return noCC;
	}
	public void setNoCC(Boolean noCC) {
		this.noCC = noCC;
	}
	public Integer getCalcVersion() {
		return calcVersion;
	}
	public void setCalcVersion(Integer calcVersion) {
		this.calcVersion = calcVersion;
	}
	public Integer getWifeVersion() {
		return wifeVersion;
	}
	public void setWifeVersion(Integer wifeVersion) {
		this.wifeVersion = wifeVersion;
	}
	public Integer getTopScore() {
		return topScore;
	}
	public void setTopScore(Integer topScore) {
		this.topScore = topScore;
	}
	public String getBrittleKey() {
		return brittleKey;
	}
	public void setBrittleKey(String brittleKey) {
		this.brittleKey = brittleKey;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public Double getWifePercent() {
		return wifePercent;
	}
	public void setWifePercent(Double wifePercent) {
		this.wifePercent = wifePercent;
	}
	public Double getWifePoints() {
		return wifePoints;
	}
	public void setWifePoints(Double wifePoints) {
		this.wifePoints = wifePoints;
	}
	public Double getJudgeScale() {
		return judgeScale;
	}
	public void setJudgeScale(Double judgeScale) {
		this.judgeScale = judgeScale;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getWifeGrade() {
		return wifeGrade;
	}
	public void setWifeGrade(String wifeGrade) {
		this.wifeGrade = wifeGrade;
	}
	
	public Set<ScoreSpecificValue> getSsrs() {
		return ssrs;
	}
	public void setSsrs(Set<ScoreSpecificValue> ssrs) {
		this.ssrs = ssrs;
	}
	public Boolean getManuallyInvalid() {
		return manuallyInvalid;
	}
	public void setManuallyInvalid(Boolean manuallyInvalid) {
		this.manuallyInvalid = manuallyInvalid;
	}
	public Double getNerfMultiplier() {
		return nerfMultiplier;
	}
	public void setNerfMultiplier(Double nerfMultiplier) {
		this.nerfMultiplier = nerfMultiplier;
	}
	public Integer getLetgoCount() {
		return letgoCount;
	}
	public void setLetgoCount(Integer letgoCount) {
		this.letgoCount = letgoCount;
	}
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
