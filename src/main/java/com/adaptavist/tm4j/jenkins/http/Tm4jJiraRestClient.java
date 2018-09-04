package com.adaptavist.tm4j.jenkins.http;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;

import com.adaptavist.tm4j.jenkins.exception.NoTestCasesFoundException;
import com.adaptavist.tm4j.jenkins.utils.Constants;
import com.adaptavist.tm4j.jenkins.extensions.JiraInstance;
import com.adaptavist.tm4j.jenkins.io.FileReader;
import com.adaptavist.tm4j.jenkins.io.FileWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.adaptavist.tm4j.jenkins.utils.Constants.ERROR;
import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class Tm4jJiraRestClient {

	private final JiraInstance jiraInstance;

	public Tm4jJiraRestClient(List<JiraInstance> jiraInstances, String serverAddress) throws Exception {
		jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
	}

	public void uploadCucumberFile(String workspace, String filePath, String projectKey, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		File file = new FileReader().getZip(workspace, filePath);
		HttpResponse<JsonNode> jsonResponse = jiraInstance.importCucumberBuildResult(projectKey, autoCreateTestCases, file);
		processImportingResultsResponse(jsonResponse, logger);
		file.delete();
	}

	public void uploadCustomFormatFile(String workspace, String filePath, String projectKey, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		File file = new FileReader().getZip(workspace, filePath);
		HttpResponse<JsonNode> jsonResponse = jiraInstance.importCustomFormatBuildResult(projectKey, autoCreateTestCases, file);
		processImportingResultsResponse(jsonResponse, logger);
		file.delete();
	}

	public void exportFeatureFiles(String featureFilesPath, String tql, PrintStream logger) throws Exception {
		try {
			HttpResponse<String> httpResponse = jiraInstance.exportFeatureFiles(tql);
			processExportingFeatureFilesResponse(featureFilesPath, logger, httpResponse);
		} catch (UnirestException e) {
			throw new Exception("Error trying to communicate with Jira", e.getCause());
		}
	}

	private void processExportingFeatureFilesResponse(String featureFilesPath, PrintStream logger, HttpResponse<String> httpResponse) throws IOException {
		if (isSuccessful(httpResponse)) {
			if (httpResponse.getStatus() == 204) {
				throw new NoTestCasesFoundException();
			}

			FileWriter fileWriter = new FileWriter(httpResponse.getRawBody());
			fileWriter.extractFeatureFilesFromZipAndSave(featureFilesPath);

			logger.printf("%s %s feature files downloaded to %s %n", INFO, fileWriter.getFileNames().size(), featureFilesPath);
		} else if (isClientError(httpResponse)) {
			if (httpResponse.getStatus() == 400) {
				processErrorMessages(httpResponse, logger);
			}

			throw new RuntimeException("There was an error while trying to request from Jira. Http Status Code: " + httpResponse.getStatus());
		} else if (isServerError(httpResponse)) {
			throw new RuntimeException("There was an error in Jira Server(" + jiraInstance.getServerAddress() + "). Http Status Code: " + httpResponse.getStatus());
		}
	}

	private void processImportingResultsResponse(HttpResponse<JsonNode> jsonResponse, PrintStream logger) {
		if (isSuccessful(jsonResponse)) {
			JSONObject testRun = (JSONObject) jsonResponse.getBody().getObject().get("testCycle");
			String testCycleKey = (String) testRun.get("key");
			String testCycleUrl = (String) testRun.get("url");
			logger.printf("%s Test Cycle created with the following KEY: %s. %s %n", INFO, testCycleKey, testCycleUrl);
			logger.printf("%s Test results published to Test Management for Jira successfully.%n", INFO);
		} else if (isClientError(jsonResponse)) {
			processErrorMessages(jsonResponse, logger);
			logger.printf("%s Test Cycle was not created %n", ERROR);
			throw new RuntimeException("There was an error while trying to import files to Jira. Http Status Code: " + jsonResponse.getStatus());
		} else if (isServerError(jsonResponse)) {
			throw new RuntimeException("There was an error in Jira Server(" + jiraInstance.getServerAddress() + "). Http Status Code: " + jsonResponse.getStatus());
		}
	}

	private void processErrorMessages(HttpResponse httpResponse, PrintStream logger) {
		JSONObject jsonObject = new JsonNode(httpResponse.getBody().toString()).getObject();
		JSONArray errorMessages = (JSONArray) jsonObject.get("errorMessages");
		for (Object errorMessage : errorMessages) {
			logger.printf("%s %s %n", ERROR, errorMessage);
		}
	}

	private boolean isSuccessful(HttpResponse httpResponse) {
		return httpResponse.getStatus() >= 200 && httpResponse.getStatus() < 300;
	}

	private boolean isClientError(HttpResponse httpResponse) {
		return httpResponse.getStatus() >= 400 && httpResponse.getStatus() < 500;
	}

	private boolean isServerError(HttpResponse httpResponse) {
		return httpResponse.getStatus() >= 500;
	}

	private JiraInstance getTm4jInstance(List<JiraInstance> jiraInstances, String serverAddress) throws Exception {
		if (jiraInstances == null)
			throw new IllegalStateException(Constants.THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED);
		for (JiraInstance jiraInstance : jiraInstances) {
			if (StringUtils.isNotBlank(jiraInstance.getServerAddress()) && jiraInstance.getServerAddress().trim().equals(serverAddress)) {
				return jiraInstance;
			}
		}
		throw new Exception(MessageFormat.format(Constants.JIRA_INSTANCE_NOT_FOUND, serverAddress));
	}
}
