package com.adaptavist.tm4j.jenkins.http;

import com.adaptavist.tm4j.jenkins.exception.NoTestCasesFoundException;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.io.FileReader;
import com.adaptavist.tm4j.jenkins.io.FileWriter;
import com.adaptavist.tm4j.jenkins.utils.Constants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;

import static com.adaptavist.tm4j.jenkins.utils.Constants.ERROR;
import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class Tm4jJiraRestClient {

    private final Instance jiraInstance;

    public Tm4jJiraRestClient(List<Instance> jiraInstances, String name) throws Exception {
        jiraInstance = getTm4jInstance(jiraInstances, name);
    }

    public void uploadCucumberFile(String directory, String filePath, String projectKey, Boolean autoCreateTestCases,
                                   final PrintStream logger) throws Exception {
        FileReader fileReader = new FileReader();
        File file;
        if(filePath.endsWith("json")){
            file = fileReader.getJsonCucumberZip(directory, filePath, logger);
        } else {
           file = fileReader.getZip(directory, filePath);
        }

        HttpResponse<JsonNode> jsonResponse = jiraInstance.publishCucumberFormatBuildResult(projectKey, autoCreateTestCases, file);
        processUploadingResultsResponse(jsonResponse, logger);
        if (!file.delete()) {
            logger.printf("%s The generated ZIP file couldn't be deleted. Please check folder permissions and delete the file manually: " + file.getAbsolutePath() + " %n", INFO);
        }
    }

    public void uploadCustomFormatFile(String directory, String projectKey, Boolean autoCreateTestCases,
                                       final PrintStream logger) throws Exception {
        File file = new FileReader().getZipForCustomFormat(directory);
        HttpResponse<JsonNode> jsonResponse = jiraInstance.publishCustomFormatBuildResult(projectKey, autoCreateTestCases, file);
        processUploadingResultsResponse(jsonResponse, logger);
        if (!file.delete()) {
            logger.printf("%s The generated ZIP file couldn't be deleted. Please check folder permissions and delete the file manually: " + file.getAbsolutePath() + " %n", INFO);
        }
    }

    public void uploadJUnitXmlResultFile(String directory, String filePath, String projectKey, Boolean autoCreateTestCases,
                                         PrintStream logger) throws Exception {
        File file = new FileReader().getZip(directory, filePath);
        HttpResponse<JsonNode> jsonResponse = jiraInstance.publishJUnitFormatBuildResult(projectKey, autoCreateTestCases, file);
        processUploadingResultsResponse(jsonResponse, logger);
        if (!file.delete()) {
            logger.printf("%s The generated ZIP file couldn't be deleted. Please check folder permissions and delete the file manually: " + file.getAbsolutePath() + " %n", INFO);
        }
    }

    public void importFeatureFiles(File rootDir, FilePath workspace, String targetPath, String projectKey, final PrintStream logger) throws Exception {
        try {
            HttpResponse<String> httpResponse = jiraInstance.downloadFeatureFile(projectKey);
            processDownloadingFeatureFilesResponse(rootDir, workspace, targetPath, logger, httpResponse);
        } catch (UnirestException e) {
            throw new Exception("Error trying to communicate with Jira", e.getCause());
        }
    }

    private void processDownloadingFeatureFilesResponse(File rootDir, FilePath workspace, String targetPath, final PrintStream logger, HttpResponse<String> httpResponse) throws IOException, InterruptedException {
        if (isSuccessful(httpResponse)) {
            if (httpResponse.getStatus() == 204) {
                throw new NoTestCasesFoundException();
            }

            FileWriter fileWriter = new FileWriter(httpResponse.getRawBody());
            fileWriter.extractFeatureFilesFromZipAndSave(rootDir, workspace, targetPath);

            logger.printf("%s %s feature files downloaded to %s %n", INFO, fileWriter.getFileNames().size(), workspace);
        } else if (isClientError(httpResponse)) {
            if (httpResponse.getStatus() == 400) {
                processErrorMessages(httpResponse, logger);
            }

            throw new RuntimeException("There was an error while trying to request from Jira. Http Status Code: " + httpResponse.getStatus());
        } else if (isServerError(httpResponse)) {
            throw new RuntimeException(MessageFormat.format("There was an error with the Jira Instance({0}). Http Status Code: {1}", jiraInstance.name(), httpResponse.getStatus()));
        }
    }

    private void processUploadingResultsResponse(HttpResponse<JsonNode> jsonResponse, final PrintStream logger) {
        if (isSuccessful(jsonResponse)) {
            JSONObject testRun = (JSONObject) jsonResponse.getBody().getObject().get("testCycle");
            String testCycleKey = (String) testRun.get("key");
            String testCycleUrl = (String) testRun.get("url");
            logger.printf("%s Test Cycle created with the following KEY: %s. %s %n", INFO, testCycleKey, testCycleUrl);
            logger.printf("%s Test results published to Zephyr Scale successfully.%n", INFO);
        } else if (isClientError(jsonResponse)) {
            processErrorMessages(jsonResponse, logger);
            logger.printf("%s Test Cycle was not created %n", ERROR);
            throw new RuntimeException("There was an error while trying to import files to Jira. Http Status Code: " + jsonResponse.getStatus());
        } else if (isServerError(jsonResponse)) {
            throw new RuntimeException(MessageFormat.format("There was an error with the Jira Instance({0}). Http Status Code: {1}", jiraInstance.name(), jsonResponse.getStatus()));
        }
    }

    private void processErrorMessages(HttpResponse<?> httpResponse, final PrintStream logger) {
        JSONObject jsonObject = new JsonNode(httpResponse.getBody().toString()).getObject();
        try {
            JSONArray errorMessages = (JSONArray) jsonObject.get("errorMessages");
            for (Object errorMessage : errorMessages) {
                logger.printf("%s %s %n", ERROR, errorMessage);
            }
        } catch (Exception e) {
            try {
                String errorMessage = (String) jsonObject.get("message");
                logger.printf("%s %s %n", ERROR, errorMessage);
            } catch (Exception e1) {
                logger.printf("%s Could not parse error message %n", ERROR);
            }
        }
    }

    private boolean isSuccessful(HttpResponse<?> httpResponse) {
        return httpResponse.getStatus() >= 200 && httpResponse.getStatus() < 300;
    }

    private boolean isClientError(HttpResponse<?> httpResponse) {
        return httpResponse.getStatus() >= 400 && httpResponse.getStatus() < 500;
    }

    private boolean isServerError(HttpResponse<?> httpResponse) {
        return httpResponse.getStatus() >= 500;
    }

    private Instance getTm4jInstance(List<Instance> jiraInstances, String name) throws Exception {
        if (jiraInstances == null) {
            throw new IllegalStateException(Constants.THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED);
        }
        for (Instance jiraInstance : jiraInstances) {
            if (StringUtils.isNotBlank(jiraInstance.name()) && jiraInstance.name().trim().equals(name)) {
                return jiraInstance;
            }
        }
        throw new Exception(MessageFormat.format(Constants.JIRA_INSTANCE_NOT_FOUND, name));
    }
}
