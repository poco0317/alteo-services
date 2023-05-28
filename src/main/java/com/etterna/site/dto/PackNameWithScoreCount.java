package com.etterna.site.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PackNameWithScoreCount {
	
	private String pack;
	private Integer count = 0;
	
	public PackNameWithScoreCount(String pack, Long count) {
		this.pack = pack;
		this.count = count.intValue();
	}

}
