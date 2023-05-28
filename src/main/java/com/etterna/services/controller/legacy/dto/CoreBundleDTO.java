package com.etterna.services.controller.legacy.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CoreBundleDTO {
	
	private String id;
	private BundleData attributes;

	@Getter @Setter
	public class BundleData {
		private List<PackData> packs;

		@Getter @Setter
		public class PackData {
			private String packname;
		}
	}
}
