package com.etterna.services.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "song")
@Getter @Setter
public class Song {
	
	@Id
	@Column(name = "songkey", nullable = false)
	private String songkey;
	
	private Long id;
	
	private String subtitle;
	private String author;
	private String artist;
	
	@OneToMany(mappedBy = "song")
	private List<Chart> charts = new ArrayList<>();
	
	@OneToMany(mappedBy = "song")
	private Set<PacksHasSongs> packs;

}
