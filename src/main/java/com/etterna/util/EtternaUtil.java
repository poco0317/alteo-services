package com.etterna.util;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.etterna.services.repo.projection.ScoreProjection;
import com.etterna.services.repo.projection.ScoreReplayProjection;

public class EtternaUtil {
	
	public static String regenerateScorekey(ScoreProjection score) {
		return "S" + sha1str(scoredatestr(score.getDatetime()));
	}
	
	public static String regenerateScorekey(ScoreReplayProjection score) {
		return "S" + sha1str(scoredatestr(score.getDatetime()));
	}
	
	public static String scoredatestr(Timestamp ts) {
		SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dtf.format(ts);
	}
	
	public static String wifetograde(Float wife) {
		if (wife > 1F || wife < 0F) {
			return "Failed";
		}
		if (wife >= 0.999935) {
			return "Tier01";
		}
		if (wife >= 0.99955) {
			return "Tier04";
		}
		if (wife >= 0.997) {
			return "Tier07";
		}
		if (wife >= 0.93) {
			return "Tier10";
		}
		if (wife >= 0.8) {
			return "Tier13";
		}
		if (wife >= 0.7) {
			return "Tier14";
		}
		if (wife >= 0.6) {
			return "Tier15";
		}
		return "Tier16";
	}
	
	public static String sha1str(String s) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {return "0";}
		return byteArrayToHexString(md.digest(s.getBytes()));
	}
	
	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}

}
