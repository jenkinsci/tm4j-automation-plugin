package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RestClient {

	private static final String REST_TROUBLESHOOTING_1_0_CHECK = "/rest/troubleshooting/1.0/check/";

	public int sendFiles(String url, String username, String password, List<File> files) {
		try {
			HttpResponse<String> jsonResponse = Unirest.post(url)
					  .header("accept", "application/json")
					  .basicAuth(username, password)
					  .field("parameter", "value")
					  .field("file", files.get(0))
					  .asString();
			return jsonResponse.getStatus();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean isValidCredentials(String serverAddress, String username, String password) {
		try {
			HttpResponse<String> response = Unirest.get(serverAddress + REST_TROUBLESHOOTING_1_0_CHECK)
					  .basicAuth(username, password)
					  .asString();
			return response.getStatus() == 200;
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return false;
	}
}
