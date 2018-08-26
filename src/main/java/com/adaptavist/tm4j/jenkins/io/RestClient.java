package com.adaptavist.tm4j.jenkins.io;

import static com.adaptavist.tm4j.jenkins.extensions.postbuildactions.Tm4jBuildResultReporter.ERROR;
import static com.adaptavist.tm4j.jenkins.extensions.postbuildactions.Tm4jBuildResultReporter.INFO;

import java.io.*;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RestClient {

	private static final String CUCUMBER_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/cucumber/{1}";
	private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/{1}";
	private static final String FEATURE_FILES_ENDPOINT = "{0}/rest/atm/1.0/automation/testcases";
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
			HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
			Unirest.setHttpClient(httpClient);
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
                JSONObject testRun = (JSONObject) jsonResponse.getBody().getObject().get("testCycle");
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

	public void downloadFeatureFiles(String serverAddress, String workspace, String username, String password, String tql) throws UnirestException, IOException {
		String url = MessageFormat.format(FEATURE_FILES_ENDPOINT, serverAddress);
		HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
		Unirest.setHttpClient(httpClient);

		HttpResponse<InputStream> featureFiles = Unirest.get(url)
				.basicAuth(username, password)
				.queryString("tql", tql)
				.asBinary();


		ZipInputStream zipInputStream = new ZipInputStream(featureFiles.getRawBody());

		ZipEntry entry = zipInputStream.getNextEntry();
		while (entry != null) {
			File featureFile = new File(workspace + entry.getName());
			FileWriter fileWriter = new FileWriter(featureFile);
			fileWriter.write("content");

			entry = zipInputStream.getNextEntry();
		}
	}
}
