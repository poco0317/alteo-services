package com.etterna.services.controller.legacy.dto;

public class ChartLeaderboardDTO {
	
	private LeaderboardScoreDTO attributes;
	
	public LeaderboardScoreDTO getAttributes() {
		return attributes;
	}

	public void setAttributes(LeaderboardScoreDTO attributes) {
		this.attributes = attributes;
	}

	public static class LeaderboardScoreDTO {
		private Boolean hasReplay;
		private LeaderboardUserDTO user;
		private LeaderboardJudgmentsDTO judgements;
		private LeaderboardSkillsetDTO skillsets;
		private String songId;
		private Float wife;
		private String modifiers;
		private Integer maxCombo;
		private String datetime;
		private String id;
		private Float rate;
		private Boolean noCC;
		private Boolean valid;
		private Integer wifeVersion;
		
		public Boolean getHasReplay() {
			return hasReplay;
		}
		public void setHasReplay(Boolean hasReplay) {
			this.hasReplay = hasReplay;
		}
		public LeaderboardUserDTO getUser() {
			return user;
		}
		public void setUser(LeaderboardUserDTO user) {
			this.user = user;
		}
		public LeaderboardJudgmentsDTO getJudgements() {
			return judgements;
		}
		public void setJudgements(LeaderboardJudgmentsDTO judgements) {
			this.judgements = judgements;
		}
		public LeaderboardSkillsetDTO getSkillsets() {
			return skillsets;
		}
		public void setSkillsets(LeaderboardSkillsetDTO skillsets) {
			this.skillsets = skillsets;
		}
		public String getSongId() {
			return songId;
		}
		public void setSongId(String songId) {
			this.songId = songId;
		}
		public Float getWife() {
			return wife;
		}
		public void setWife(Float wife) {
			this.wife = wife;
		}
		public String getModifiers() {
			return modifiers;
		}
		public void setModifiers(String modifiers) {
			this.modifiers = modifiers;
		}
		public Integer getMaxCombo() {
			return maxCombo;
		}
		public void setMaxCombo(Integer maxCombo) {
			this.maxCombo = maxCombo;
		}
		public String getDatetime() {
			return datetime;
		}
		public void setDatetime(String datetime) {
			this.datetime = datetime;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Float getRate() {
			return rate;
		}
		public void setRate(Float rate) {
			this.rate = rate;
		}
		public Boolean getNoCC() {
			return noCC;
		}
		public void setNoCC(Boolean noCC) {
			this.noCC = noCC;
		}
		public Boolean getValid() {
			return valid;
		}
		public void setValid(Boolean valid) {
			this.valid = valid;
		}
		public Integer getWifeVersion() {
			return wifeVersion;
		}
		public void setWifeVersion(Integer wifeVersion) {
			this.wifeVersion = wifeVersion;
		}
		public static class LeaderboardUserDTO {
			private String userName;
			private String avatar;
			private Integer userId;
			private String countryCode;
			private Float playerRating;
			public String getUserName() {
				return userName;
			}
			public void setUserName(String userName) {
				this.userName = userName;
			}
			public String getAvatar() {
				return avatar;
			}
			public void setAvatar(String avatar) {
				this.avatar = avatar;
			}
			public Integer getUserId() {
				return userId;
			}
			public void setUserId(Integer userId) {
				this.userId = userId;
			}
			public String getCountryCode() {
				return countryCode;
			}
			public void setCountryCode(String countryCode) {
				this.countryCode = countryCode;
			}
			public Float getPlayerRating() {
				return playerRating;
			}
			public void setPlayerRating(Float playerRating) {
				this.playerRating = playerRating;
			}
		}
		public static class LeaderboardJudgmentsDTO {
			private Integer marvelous;
			private Integer perfect;
			private Integer great;
			private Integer good;
			private Integer bad;
			private Integer miss;
			private Integer hitMines;
			private Integer heldHold;
			private Integer letGoHold;
			public Integer getMarvelous() {
				return marvelous;
			}
			public void setMarvelous(Integer marvelous) {
				this.marvelous = marvelous;
			}
			public Integer getPerfect() {
				return perfect;
			}
			public void setPerfect(Integer perfect) {
				this.perfect = perfect;
			}
			public Integer getGreat() {
				return great;
			}
			public void setGreat(Integer great) {
				this.great = great;
			}
			public Integer getGood() {
				return good;
			}
			public void setGood(Integer good) {
				this.good = good;
			}
			public Integer getBad() {
				return bad;
			}
			public void setBad(Integer bad) {
				this.bad = bad;
			}
			public Integer getMiss() {
				return miss;
			}
			public void setMiss(Integer miss) {
				this.miss = miss;
			}
			public Integer getHitMines() {
				return hitMines;
			}
			public void setHitMines(Integer hitMines) {
				this.hitMines = hitMines;
			}
			public Integer getHeldHold() {
				return heldHold;
			}
			public void setHeldHold(Integer heldHold) {
				this.heldHold = heldHold;
			}
			public Integer getLetGoHold() {
				return letGoHold;
			}
			public void setLetGoHold(Integer letGoHold) {
				this.letGoHold = letGoHold;
			}
		}
		public static class LeaderboardSkillsetDTO {
			private Float Overall;
			private Float Stream;
			private Float Jumpstream;
			private Float Handstream;
			private Float Stamina;
			private Float JackSpeed;
			private Float Chordjack;
			private Float Technical;
			public Float getOverall() {
				return Overall;
			}
			public void setOverall(Float overall) {
				Overall = overall;
			}
			public Float getStream() {
				return Stream;
			}
			public void setStream(Float stream) {
				Stream = stream;
			}
			public Float getJumpstream() {
				return Jumpstream;
			}
			public void setJumpstream(Float jumpstream) {
				Jumpstream = jumpstream;
			}
			public Float getHandstream() {
				return Handstream;
			}
			public void setHandstream(Float handstream) {
				Handstream = handstream;
			}
			public Float getStamina() {
				return Stamina;
			}
			public void setStamina(Float stamina) {
				Stamina = stamina;
			}
			public Float getJackSpeed() {
				return JackSpeed;
			}
			public void setJackSpeed(Float jackSpeed) {
				JackSpeed = jackSpeed;
			}
			public Float getChordjack() {
				return Chordjack;
			}
			public void setChordjack(Float chordjack) {
				Chordjack = chordjack;
			}
			public Float getTechnical() {
				return Technical;
			}
			public void setTechnical(Float technical) {
				Technical = technical;
			}
		}
	}

}
