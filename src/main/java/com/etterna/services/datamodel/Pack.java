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

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "packs")
@Getter @Setter
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
