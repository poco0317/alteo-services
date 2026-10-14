package com.etterna.services.data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "packs_has_songs")
@Getter @Setter
public class PacksHasSongs {
	
	@EmbeddedId
	private PacksHasSongsId id;
	
	@ManyToOne
	@MapsId("packs_packname")
	@JoinColumn(name = "packs_packname")
	private Pack pack;
	
	@ManyToOne
	@MapsId("song_songkey")
	@JoinColumn(name = "song_songkey")
	private Song song;

}
