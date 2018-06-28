package com.adaptavist.tm4j.jenkins.utils;

/**
 * @author mohan
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLException;

public class URLValidator {

	public static final String INVALID_URL = "This is not a valid URL";
	public static final String SSL_ERROR = "SSL Exception";
	public static final String CONNECTION_ERROR = "Could not establish the connection";

	public static String validateURL(String string) {

		String result = null;
		URL url = null;
		URLConnection conn = null;
		try {
			url = new URL(string);
			conn = url.openConnection();
			conn.connect();

			result = url.getProtocol();
			result += "://";
			result += url.getHost();
			
			int port = url.getPort();
			if (port > 0) {
				result += ":";
				result += port;
				
			}
		} catch (MalformedURLException e) {
			result = INVALID_URL;
		} catch (SSLException e) {
			result = SSL_ERROR;
		} catch (IOException e) {
			result = CONNECTION_ERROR;
		}
		return result;
	}

}
