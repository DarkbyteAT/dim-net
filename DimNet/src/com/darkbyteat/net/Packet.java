package com.darkbyteat.net;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Packet {
	
	//Takes in the ID and data for the packet
	public Packet(String id, String data) {
		this.id = id;
		this.data = data;
	}
	
	//Stores the ID and data of the packet
	public volatile String id, data;
	
	//Generates an ID using an IP address by hashing it
	//Can also be used to hash anything else with the SHA256 algorithm
	public static String hash(final String str) {
		//Stores the final string buffer for the hash
		StringBuffer buffer = new StringBuffer();
		
		//Hashes the IP and converts the hashed bytes into a string
		try {
			//Stores the bytes in the IP after being hashed
			byte[] bytes = MessageDigest.getInstance("SHA-256").digest(str.trim().getBytes());
			//Loops through each byte in the IP and appends the corresponding hex-string to the string buffer
			for(byte b : bytes)
				//Applies an XOR on the byte with FF (255 in binary) to conform the byte to ASCII encoding
				buffer.append(Integer.toHexString((int) (b & 0xff)));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		//Returns the contents of the buffer
		return buffer.toString();
	}
}