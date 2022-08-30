package com.etterna.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ScuffedPacklist {
	
	private static String json;
	
	static {
		 try {
			json = new String(ScuffedPacklist.class.getResourceAsStream("/packlist").readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			json = "";
			e.printStackTrace();
		}
	}

	public static String get() {
		return json;
	}
}
