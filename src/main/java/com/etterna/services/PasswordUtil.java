package com.etterna.services;

import java.security.MessageDigest;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PasswordUtil {
	
	private static final Logger m_logger = LoggerFactory.getLogger(PasswordUtil.class);
	
	/**
	 * Gives a secure random salt
	 */
	public final static String getSalt() {
		try {
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
			byte[] salt = new byte[16];
			sr.nextBytes(salt);
			return salt.toString();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return "";
		}
	}
	
	/**
	 * Hashes a password with the given salt and returns the result
	 */
	public final static String hashPassword(final String pass, final String salt) {
		if (salt == null || salt.isEmpty()) {
			throw new NullPointerException("Salt was not provided to enhance password security");
		}
		
		String result = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt.getBytes());
			byte[] resultbytes = md.digest(pass.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : resultbytes) {
				sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
			}
			result = sb.toString();
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		
		return result;
	}

}
