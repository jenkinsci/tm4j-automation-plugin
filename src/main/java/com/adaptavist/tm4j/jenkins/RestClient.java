package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.adaptavist.tm4j.jenkins.Tm4jReporter.ERROR;
import static com.adaptavist.tm4j.jenkins.Tm4jReporter.INFO;

public class RestClient {

	private static final String CUCUMBER_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/cucumber/{1}";
	private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/{1}";
	private static final String TM4J_HEALTH_CHECK = "{0}/rest/atm/1.0/healthcheck/";

    public int sendCucumberFiles(String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		return sendZip(CUCUMBER_ENDPOINT, serverAddress, projectKey, username, password, zip, autoCreateTestCases, logger);
	}

	public int sendCustomFormatFiles(String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		 return sendZip(CUSTOM_FORMAT_ENDPOINT, serverAddress, projectKey, username, password, zip, autoCreateTestCases, logger);
	}

	public int sendZip(String endpoint, String serverAddress, String projectKey, String username, String password, File zip, Boolean autoCreateTestCases, PrintStream logger) throws Exception  {
		try {
			String url = MessageFormat.format(endpoint, serverAddress, projectKey);
            HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                    .basicAuth(username, password)
                    .queryString("autoCreateTestCases", autoCreateTestCases)
                    .field("parameter", "value")
                    .field("file", zip)
                    .asJson();

            if(jsonResponse.getStatus() == 400) {
                JSONArray errorMessages = (JSONArray) jsonResponse.getBody().getObject().get("errorMessages");
                errorMessages.forEach(errorMessage -> logger.printf("%s %s %n", ERROR, errorMessage));
                logger.printf("%s Test Cycle was not created %n", ERROR);
            } else {
                JSONObject testRun = (JSONObject) jsonResponse.getBody().getObject().get("testRun");
                String testCycleKey = (String) testRun.get("key");
                String testCycleUrl = (String) testRun.get("url");
                logger.printf("%s Test Cycle created with the following KEY: %s. %s %n", INFO, testCycleKey, testCycleUrl);
                logger.printf("%s Test results published to Test Management for Jira successfully.%n", INFO);
            }
            return jsonResponse.getStatus();
		} catch (UnirestException e) {
			throw new Exception("Error trying to communicate with Jira", e.getCause());
		}
	}

	public boolean isValidCredentials(String serverAddress, String username, String password) {
		try {
			String url = MessageFormat.format(TM4J_HEALTH_CHECK, serverAddress);
			HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
			Unirest.setHttpClient(httpClient);
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
