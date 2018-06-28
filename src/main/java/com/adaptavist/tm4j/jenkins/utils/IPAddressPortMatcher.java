package com.adaptavist.tm4j.jenkins.utils;

/**
 * @author mohan
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddressPortMatcher {

	private static String IP_ADDRESS_PORT = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?"; 

	/**
	 * 
	 * @param srcString
	 * @return
	 * 
	 * Returns the Ip Address and Port or null if no match is found (Ex. 127.0.0.1:80 or null).
	 */
	public static String getIpAddressPort(String srcString) {

		Pattern pattern = Pattern.compile(IP_ADDRESS_PORT);
		Matcher matcher = pattern.matcher(srcString);
		String matchedString = null;
		if (matcher.find()) {
			matchedString = matcher.group();
		}
		
		return matchedString;
	}
	
	
}
