package com.adaptavist.tm4j.jenkins.extensions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hudson.util.Secret;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.text.MessageFormat;

public class JiraInstance implements Instance {

    private static final String CUCUMBER_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/cucumber/{1}";
    private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/{1}";
    private static final String FEATURE_FILES_ENDPOINT = "{0}/rest/atm/1.0/automation/testcases";
    private static final String TM4J_HEALTH_CHECK = "{0}/rest/atm/1.0/healthcheck/";

    private String serverAddress;
    private String username;
    private Secret password;

    public JiraInstance() {
    }

    public JiraInstance(String serverAddress, String username, Secret password) {
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
    }

    @Override
    public Boolean cloud() {
        return false;
    }

    @Override
    public String name() {
        return serverAddress;
    }

    @Override
    public Boolean isValidCredentials() {
        try {
            String url = MessageFormat.format(TM4J_HEALTH_CHECK, serverAddress);
            HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.get(url)
                    .basicAuth(username, this.getPlainTextPassword())
                    .asString();
            return response.getStatus() == 200;
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public HttpResponse<String> downloadFeatureFile(String tql) throws UnirestException {
        String url = MessageFormat.format(FEATURE_FILES_ENDPOINT, serverAddress);
        HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
        Unirest.setHttpClient(httpClient);

        return Unirest.get(url).basicAuth(username, this.getPlainTextPassword()).queryString("tql", tql).asString();
    }

    @Override
    public HttpResponse<JsonNode> publishCucumberFormatBuildResult(String projectKey, Boolean autoCreateTestCases, File zip) throws UnirestException {
        String url = MessageFormat.format(CUCUMBER_ENDPOINT, serverAddress, projectKey);
        HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
        Unirest.setHttpClient(httpClient);
        return importBuildResultsFile(autoCreateTestCases, zip, url);
    }

    @Override
    public HttpResponse<JsonNode> publishCustomFormatBuildResult(String projectKey, Boolean autoCreateTestCases, File zip) throws UnirestException {
        String url = MessageFormat.format(CUSTOM_FORMAT_ENDPOINT, serverAddress, projectKey);
        HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
        Unirest.setHttpClient(httpClient);
        return importBuildResultsFile(autoCreateTestCases, zip, url);
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Secret getPassword() {
        return password;
    }

    public void setPassword(Secret password) {
        this.password = password;
    }

    private HttpResponse<JsonNode> importBuildResultsFile(Boolean autoCreateTestCases, File zip, String url) throws UnirestException {
        return Unirest.post(url)
                .basicAuth(username, this.getPlainTextPassword())
                .queryString("autoCreateTestCases", autoCreateTestCases)
                .field("file", zip)
                .asJson();
    }

    private String getPlainTextPassword() {
        return Secret.toString(password);
    }

}
