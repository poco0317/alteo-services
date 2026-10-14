package com.etterna.services.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rate")
@Getter @Setter
public class Rate {
	
	@Id
	private Double rate;

}
