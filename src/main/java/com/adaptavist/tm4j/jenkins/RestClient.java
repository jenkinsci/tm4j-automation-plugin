package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.text.MessageFormat;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RestClient {

	private static final String CUCUMBER_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/cucumber/{1}";
	private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/{1}";
	private static final String TM4J_HEALTH_CHECK = "{0}/rest/atm/1.0/healthcheck/";

	public void sendCucumberFiles(String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases) throws Exception {
		sendZip(CUCUMBER_ENDPOINT, serverAddress, projectKey, username, password, zip, autoCreateTestCases);
	}

	public void sendCustomFormatFiles(String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases) throws Exception {
		 sendZip(CUSTOM_FORMAT_ENDPOINT, serverAddress, projectKey, username, password, zip, autoCreateTestCases);
	}

	public void sendZip(String endpoint, String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases) throws Exception  {
		try {
			String url = MessageFormat.format(endpoint, serverAddress, projectKey);
			HttpResponse<String> jsonResponse = Unirest.post(url)
					  .basicAuth(username, password)
                      .queryString("autoCreateTestCases", autoCreateTestCases)
					  .field("parameter", "value")
					  .field("file", zip)
					  .asString();
			jsonResponse.getStatus();
		} catch (UnirestException e) {
			throw new Exception(e.getMessage());
		}
	}

	public boolean isValidCredentials(String serverAddress, String username, String password) {
		try {
			String url = MessageFormat.format(TM4J_HEALTH_CHECK, serverAddress);
			HttpResponse<String> response = Unirest.get(url)
					  .basicAuth(username, password)
					  .asString();
			return response.getStatus() == 200;
		} catch (UnirestException e) {
			e.printStackTrace();
		}
		return false;
	}
}
