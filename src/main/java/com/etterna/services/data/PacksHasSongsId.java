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
public class PacksHasSongsId implements Serializable {
	private static final long serialVersionUID = -2141644444664906895L;

	@Column(name = "packs_packname")
	private String packs_packname;
	
	@Column(name = "song_songkey")
	private String song_songkey;
	

}
