package com.etterna.services.controller.legacy.dto;

import java.util.List;

public class CoreBundleDTO {
	
	private String id;
	private BundleData attributes;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BundleData getAttributes() {
		return attributes;
	}

	public void setAttributes(BundleData attributes) {
		this.attributes = attributes;
	}

	public class BundleData {
		private List<PackData> packs;
		
		public List<PackData> getPacks() {
			return packs;
		}

		public void setPacks(List<PackData> packs) {
			this.packs = packs;
		}

		public class PackData {
			private String packname;

			public String getPackname() {
				return packname;
			}

			public void setPackname(String packname) {
				this.packname = packname;
			}
		}
	}

}
