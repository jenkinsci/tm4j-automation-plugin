package com.adaptavist.tm4j.jenkins.extensions;

import static java.lang.String.format;

import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import hudson.util.Secret;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class JiraServerInstance extends Instance {

    private static final String CUCUMBER_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/cucumber/{1}";
    private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/rest/atm/1.0/automation/execution/{1}";
    private static final String FEATURE_FILES_ENDPOINT = "{0}/rest/atm/1.0/automation/testcases";
    private static final String HEALTH_CHECK_ENDPOINT = "{0}/rest/atm/1.0/healthcheck/";

    private static final Logger LOGGER = Logger.getLogger(JiraServerInstance.class.getName());

    private String serverAddress;
    private String username;
    private Secret password;

    public JiraServerInstance() {
        setUnirestHttpClient(getNewHttpClient());
    }

    public JiraServerInstance(final String serverAddress, final String username, final Secret password) {
        setUnirestHttpClient(getNewHttpClient());
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
    }

    @Override
    public final Boolean cloud() {
        return false;
    }

    @Override
    public String name() {
        return serverAddress;
    }

    @Override
    public final Boolean isValidCredentials() {
        try {
            final String url = MessageFormat.format(HEALTH_CHECK_ENDPOINT, serverAddress);

            final HttpResponse<String> response = Unirest.get(url)
                .basicAuth(username, this.getPlainTextPassword())
                .asString();

            return response.getStatus() == 200;
        } catch (UnirestException e) {
            LOGGER.log(Level.WARNING, "Invalid server instance credentials", e);
        }

        return false;
    }

    @Override
    public HttpResponse<String> downloadFeatureFile(final String projectKey) throws UnirestException {
        final String url = MessageFormat.format(FEATURE_FILES_ENDPOINT, serverAddress);

        final String tql = format("testCase.projectKey = '%s'", projectKey);

        return Unirest.get(url).basicAuth(username, this.getPlainTextPassword()).queryString("tql", tql).asString();
    }

    @Override
    public HttpResponse<JsonNode> publishCucumberFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases,
                                                                   final File zip, final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {

        final String url = MessageFormat.format(CUCUMBER_ENDPOINT, serverAddress, projectKey);

        return importBuildResultsFile(autoCreateTestCases, zip, url, expandedCustomTestCycle);
    }

    @Override
    public HttpResponse<JsonNode> publishCustomFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                                 final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {

        final String url = MessageFormat.format(CUSTOM_FORMAT_ENDPOINT, serverAddress, projectKey);

        return importBuildResultsFile(autoCreateTestCases, zip, url, expandedCustomTestCycle);
    }

    @Override
    public HttpResponse<JsonNode> publishJUnitFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                                final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {
        throw new RuntimeException("Not implemented for Zephyr Scale Server/DC");
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

    private HttpResponse<JsonNode> importBuildResultsFile(final Boolean autoCreateTestCases, final File zip, String url,
                                                          final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {

        final MultipartBody body = Unirest.post(url)
            .basicAuth(username, this.getPlainTextPassword())
            .queryString("autoCreateTestCases", autoCreateTestCases)
            .field("file", zip);

        if (expandedCustomTestCycle != null && !expandedCustomTestCycle.isEmpty()) {
            body.field("testCycle", GsonUtils.getInstance().toJson(expandedCustomTestCycle), "application/json");
        }

        return this.getBodyAsJsonOrThrowExceptionWithBody(body);
    }

    private String getPlainTextPassword() {
        return Secret.toString(password);
    }

    private HttpClient getNewHttpClient() {
        return HttpClientBuilder.create().disableCookieManagement().build();
    }

    protected void setUnirestHttpClient(final HttpClient httpClient) {
        Unirest.setHttpClient(httpClient);
    }

}
