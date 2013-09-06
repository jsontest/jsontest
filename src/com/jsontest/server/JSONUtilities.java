package com.jsontest.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Supplies miscellaneous methods needed by JSONTest.com services.
 * 
 * @author Vinny
 *
 */
public class JSONUtilities {
	
	/**
	 * Calculates a SHA1 hash from a provided String. If 
	 * to_be_sha1 is null, a RuntimeException will be thrown.
	 * 
	 * @param to_be_sha1 String to calculate a SHA1 hash from.
	 * @return A SHA1 hash from the provided String.
	 */
	public static String generateSHA1(String to_be_sha1) {
		String sha1_sum = "";
		
		//if the provided String is null, throw an Exception.
		if (to_be_sha1 == null) {
			throw new RuntimeException("There is no String to calculate a SHA1 hash from.");
		}
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
	        byte[] array = digest.digest(to_be_sha1.getBytes("UTF-8"));
	        StringBuffer collector = new StringBuffer();
	        for (int i = 0; i < array.length; i++) {
	            collector.append(Integer.toString((array[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        sha1_sum = collector.toString();
		}//end try
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not find a SHA1 instance: " + e.getMessage());
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not translate UTF-8: " + e.getMessage());
		}
		
		return sha1_sum;
	}//end generateSHA1
	
	/**
	 * Calculate a MD5 hash from the provided String. If the 
	 * provided String is null, this method will throw a 
	 * RuntimeException.
	 * 
	 * @param to_be_md5 A String to calculate a MD5 hash from.
	 * @return A MD5 hash calculated from the provided String.
	 * @throws RuntimeException If an error was encountered during calculating.
	 */
	public static String generateMD5(String to_be_md5) {
		String md5_sum = "";
		
		//If the provided String is null, then throw an Exception.
		if (to_be_md5 == null) {
			throw new RuntimeException("There is no string to calculate a MD5 hash from.");
		}
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(to_be_md5.getBytes("UTF-8"));
			StringBuffer collector = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				collector.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			md5_sum = collector.toString();
		}//end try
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not find a MD5 instance: " + e.getMessage());
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not translate UTF-8: " + e.getMessage());
		}
		
		return md5_sum;
	}//end generateMD5
}//end file
