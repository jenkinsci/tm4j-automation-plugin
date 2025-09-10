package com.adaptavist.tm4j.jenkins.http;

import com.adaptavist.tm4j.jenkins.exception.NoTestCasesFoundException;
import com.adaptavist.tm4j.jenkins.extensions.ExpandedCustomTestCycle;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.io.FileReader;
import com.adaptavist.tm4j.jenkins.io.FileWriter;
import com.adaptavist.tm4j.jenkins.utils.Constants;
import com.adaptavist.tm4j.jenkins.utils.ZipHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.FilePath;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.*;
import java.text.MessageFormat;
import java.util.List;

import static com.adaptavist.tm4j.jenkins.utils.Constants.ERROR;
import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class Tm4jJiraRestClient {

    private final Instance jiraInstance;
    private final PrintStream logger;

    public Tm4jJiraRestClient(PrintStream logger, List<Instance> jiraInstances, String name) throws Exception {
        this.logger = logger;
        jiraInstance = getTm4jInstance(jiraInstances, name);
    }

    public void uploadCucumberFile(final String directory, final String filePath, final String projectKey,
                                   final Boolean autoCreateTestCases, final ExpandedCustomTestCycle expandedCustomTestCycle
    ) throws Exception {

        final FileReader fileReader = new FileReader();

        final File file = filePath.endsWith("json")
                ? fileReader.getJsonCucumberZip(directory, filePath, this.logger)
                : fileReader.getZip(directory, filePath);

        HttpResponse<JsonNode> jsonResponse = jiraInstance.publishCucumberFormatBuildResult(
                projectKey,
                autoCreateTestCases,
                file,
                expandedCustomTestCycle
        );

        processUploadingResultsResponse(jsonResponse);

        deleteFile(file);
    }

    public void uploadCustomFormatFile(final String directory, final String projectKey, final Boolean autoCreateTestCases,
                                       final ExpandedCustomTestCycle expandedCustomTestCycle) throws Exception {

        File file = new FileReader().getZipForCustomFormat(directory);

        HttpResponse<JsonNode> jsonResponse = jiraInstance.publishCustomFormatBuildResult(
                projectKey,
                autoCreateTestCases,
                file,
                expandedCustomTestCycle
        );

        processUploadingResultsResponse(jsonResponse);

        deleteFile(file);
    }

    public void uploadJUnitXmlResultFile(final String directory, final String filePath, final String projectKey,
                                         final Boolean autoCreateTestCases, final ExpandedCustomTestCycle expandedCustomTestCycle)
            throws Exception {

        File file = new FileReader().getZip(directory, filePath);
        try {
            HttpResponse<JsonNode> jsonResponse = jiraInstance.publishJUnitFormatBuildResult(
                    projectKey,
                    autoCreateTestCases,
                    file,
                    expandedCustomTestCycle
            );
            processUploadingResultsResponse(jsonResponse);
            deleteFile(file);
        } catch (Exception e) {
            logger.printf("%s An error was raised, the file will not be removed for troubleshooting purposes, when trying to send file on path:'%s' with content:%n %s %n", ERROR, file.getAbsolutePath(), ZipHandler.getContentFromFilesInZip(file));
            throw e;
        }
    }

    private void deleteFile(final File file) {
        if (!file.delete()) {
            logger.printf("%s The generated ZIP file couldn't be deleted. Please check folder permissions and delete the file manually: %s %n", INFO, file.getAbsolutePath());
        }
    }


    public void importFeatureFiles(File rootDir, FilePath workspace, String targetPath, String projectKey)
            throws Exception {
        try {
            HttpResponse<String> httpResponse = jiraInstance.downloadFeatureFile(projectKey);
            processDownloadingFeatureFilesResponse(rootDir, workspace, targetPath, httpResponse);
        } catch (UnirestException e) {
            throw new Exception("Error trying to communicate with Jira", e.getCause());
        }
    }

    private void processDownloadingFeatureFilesResponse(File rootDir, FilePath workspace, String targetPath,
                                                        HttpResponse<String> httpResponse) throws IOException, InterruptedException {
        if (isSuccessful(httpResponse)) {
            if (httpResponse.getStatus() == 204) {
                throw new NoTestCasesFoundException();
            }

            FileWriter fileWriter = new FileWriter(httpResponse.getRawBody());
            fileWriter.extractFeatureFilesFromZipAndSave(rootDir, workspace, targetPath);

            logger.printf("%s %s feature files downloaded to %s %n", INFO, fileWriter.getFileNames().size(), workspace);
        } else if (isClientError(httpResponse)) {
            if (httpResponse.getStatus() == 400) {
                processErrorMessages(httpResponse);
            }

            throw new RuntimeException(
                    "There was an error while trying to request from Jira. Http Status Code: " + httpResponse.getStatus());
        } else if (isServerError(httpResponse)) {
            throw new RuntimeException(
                    MessageFormat.format("There was an error with the Jira Instance({0}). Http Status Code: {1}", jiraInstance.name(),
                            httpResponse.getStatus()));
        }
    }

    private void processUploadingResultsResponse(HttpResponse<JsonNode> jsonResponse) {
        if (isSuccessful(jsonResponse)) {
            JSONObject testRun = (JSONObject) jsonResponse.getBody().getObject().get("testCycle");
            String testCycleKey = (String) testRun.get("key");
            String testCycleUrl = (String) testRun.get("url");
            logger.printf("%s Test Cycle created with the following KEY: %s. %s %n", INFO, testCycleKey, testCycleUrl);
            logger.printf("%s Test results published to Zephyr successfully.%n", INFO);
        } else if (isClientError(jsonResponse)) {
            processErrorMessages(jsonResponse);
            logger.printf("%s Test Cycle was not created %n", ERROR);
            throw new RuntimeException(
                    "There was an error while trying to import files to Jira. Http Status Code: " + jsonResponse.getStatus());
        } else if (isServerError(jsonResponse)) {
            throw new RuntimeException(
                    MessageFormat.format("There was an error with the Jira Instance({0}). Http Status Code: {1}", jiraInstance.name(),
                            jsonResponse.getStatus()));
        }
    }

    private void processErrorMessages(HttpResponse<?> httpResponse) {
        try {
            final JSONObject jsonObject = new JsonNode(httpResponse.getBody().toString()).getObject();

            if (jsonObject != null) {
                logger.printf("%s Error: %n%n%s%n%n", ERROR, jsonObject.toString(2));
            }

        } catch (final Exception e) {
            logger.printf("%s Could not parse error message %n", ERROR);
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
