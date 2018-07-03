package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RestClient {

	private static final String TM4J_TESTRUNS = "{0}/rest/kanoahtests/1.0/ci/results/cucumber/{1}/testruns";
	private static final String TM4J_HEALTH_CHECK = "{0}/rest/kanoahtests/1.0/healthcheck/";

	public int sendFiles(String serverAddress, String projectKey, String username, String password, List<File> files) {
		try {
			String url = MessageFormat.format(TM4J_TESTRUNS, serverAddress, projectKey);
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
			String format = MessageFormat.format(TM4J_HEALTH_CHECK, serverAddress);
			HttpResponse<String> response = Unirest.get(format)
					  .basicAuth(username, password)
					  .asString();
			return response.getStatus() == 200;
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return false;
	}
}
