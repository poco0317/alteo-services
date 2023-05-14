package com.etterna.services.datamodel;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "packs")
public class Pack {
	
	@Id
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "display_name", nullable = false)
	private String displayName;
	
	@ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	@JoinTable(
			name = "pack_charts",
			joinColumns = @JoinColumn(name = "id"),
			inverseJoinColumns = @JoinColumn(name = "chart_key"))
	private Set<Chart> charts;
	
	@Column(name = "ranked")
	private Date ranked;
	
	@Column(name = "released")
	private Date released;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Chart> getCharts() {
		return charts;
	}
	public void setCharts(Set<Chart> charts) {
		this.charts = charts;
	}
	public Date getRanked() {
		return ranked;
	}
	public void setRanked(Date ranked) {
		this.ranked = ranked;
	}
	public Date getReleased() {
		return released;
	}
	public void setReleased(Date released) {
		this.released = released;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pack other = (Pack) obj;
		return Objects.equals(name, other.name);
	}

}
