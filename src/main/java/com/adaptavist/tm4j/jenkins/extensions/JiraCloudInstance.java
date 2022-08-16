package com.adaptavist.tm4j.jenkins.extensions;

import com.adaptavist.tm4j.jenkins.exception.InvalidJwtException;
import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import com.nimbusds.jwt.JWTParser;
import hudson.util.Secret;
import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class JiraCloudInstance extends Instance {

    private static final String CUCUMBER_ENDPOINT = "{0}/v2/automations/executions/cucumber";
    private static final String JUNIT_ENDPOINT = "{0}/v2/automations/executions/junit";
    private static final String CUSTOM_FORMAT_ENDPOINT = "{0}/v2/automations/executions/custom";
    private static final String FEATURE_FILES_ENDPOINT = "{0}/v2/automations/testcases";
    private static final String HEALTH_CHECK_ENDPOINT = "{0}/v2/healthcheck";
    private static final String API_BASE_URL = "https://api.zephyrscale.smartbear.com";

    private static final Logger LOGGER = Logger.getLogger(JiraCloudInstance.class.getName());

    private String cloudAddress;
    private Secret jwt;
    private String name;

    public JiraCloudInstance() {
        setUnirestHttpClient(getNewHttpClient());
    }

    public JiraCloudInstance(Secret jwt) {
        setJwt(jwt);
        setUnirestHttpClient(getNewHttpClient());
    }

    @Override
    public Boolean cloud() {
        return true;
    }

    @Override
    public String name() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCloudAddress() {
        return this.cloudAddress;
    }

    @Override
    public Boolean isValidCredentials() {
        try {
            final String url = MessageFormat.format(HEALTH_CHECK_ENDPOINT, API_BASE_URL);

            final HttpResponse<String> response = Unirest.get(url)
                .header("Authorization", "Bearer " + getDecryptedJwt())
                .asString();

            return response.getStatus() == 200;
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public HttpResponse<JsonNode> publishCucumberFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases,
                                                                   final File zip, final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {

        String url = MessageFormat.format(CUCUMBER_ENDPOINT, API_BASE_URL);
        return exportResultsFile(url, projectKey, autoCreateTestCases, zip, expandedCustomTestCycle);
    }

    @Override
    public HttpResponse<JsonNode> publishCustomFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                                 final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {

        String url = MessageFormat.format(CUSTOM_FORMAT_ENDPOINT, API_BASE_URL);
        return exportResultsFile(url, projectKey, autoCreateTestCases, zip, expandedCustomTestCycle);
    }

    @Override
    public HttpResponse<JsonNode> publishJUnitFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                                final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {
        String url = MessageFormat.format(JUNIT_ENDPOINT, API_BASE_URL);
        return exportResultsFile(url, projectKey, autoCreateTestCases, zip, expandedCustomTestCycle);
    }

    @Override
    public HttpResponse<String> downloadFeatureFile(final String projectKey) throws UnirestException {
        String url = MessageFormat.format(FEATURE_FILES_ENDPOINT, API_BASE_URL);

        return Unirest.get(url)
            .header("Authorization", "Bearer " + getDecryptedJwt())
            .header("Accept", "application/zip")
            .queryString("projectKey", projectKey)
            .asString();
    }

    public Secret getJwt() {
        return jwt;
    }

    public void setJwt(Secret jwt) {
        this.jwt = jwt;
        this.name = getBaseUrl();
        this.cloudAddress = name;
    }

    public Boolean getCloud() {
        return true;
    }

    private String getBaseUrl() {
        try {
            Object context = JWTParser.parse(getDecryptedJwt()).getJWTClaimsSet().getClaim("context");
            if (context instanceof JSONObject) {
                return (String) ((JSONObject) context).get("baseUrl");
            }
            LOGGER.log(Level.WARNING, "Invalid jwt. Please provide a valid Zephyr scale jwt by updating your jira cloud configuration");
            return null;

        } catch (ParseException e) {
            throw new InvalidJwtException(e);
        }
    }

    private HttpResponse<JsonNode> exportResultsFile(final String url, final String projectKey, final Boolean autoCreateTestCases,
                                                     final File zip, final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException {
        final MultipartBody body = Unirest.post(url)
            .header("Authorization", "Bearer " + getDecryptedJwt())
            .header("zscale-source", "Jenkins Plugin")
            .queryString("autoCreateTestCases", autoCreateTestCases)
            .queryString("projectKey", projectKey)
            .field("file", zip);

        if (expandedCustomTestCycle != null && !expandedCustomTestCycle.isEmpty()) {
            body.field("testCycle", GsonUtils.getInstance().toJson(expandedCustomTestCycle), "application/json");
        }

        return this.getBodyAsJsonOrThrowExceptionWithBody(body);
    }

    private String getDecryptedJwt() {
        return jwt.getPlainText();
    }

    private HttpClient getNewHttpClient() {
        return HttpClientBuilder.create().disableCookieManagement().build();
    }

    protected void setUnirestHttpClient(final HttpClient httpClient) {
        Unirest.setHttpClient(httpClient);
    }

    @Override
    public void setCloudAddress(String cloudAddress) {
        this.cloudAddress = cloudAddress;
    }
}
