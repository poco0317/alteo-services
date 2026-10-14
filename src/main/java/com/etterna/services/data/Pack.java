package com.etterna.services.data;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "packs")
@Getter @Setter
public class Pack {
	
	@Id
	@Column(name = "packname", nullable = false)
	private String packname;
	
	private Long packid;
	
	private Float average;
	private Timestamp date;
	private String download;
	private String mirror;
	private Long size;
	
	@OneToMany(mappedBy = "pack")
	Set<PacksHasSongs> songs;

}
