package com.adaptavist.tm4j.jenkins.extensions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import hudson.util.Secret;

import java.io.File;

public abstract class Instance {

    private String value;
    private String serverAddress;
    private String username;
    private Secret password;
    private String cloudAddress;
    private Secret jwt;


    public abstract Boolean cloud();

    public abstract String name();

    public abstract Boolean isValidCredentials();

    public abstract HttpResponse<JsonNode> publishCucumberFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases,
                                                                            final File zip,
                                                                            final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException;

    public abstract HttpResponse<JsonNode> publishCustomFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases,
                                                                          final File zip,
                                                                          final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException;

    public abstract HttpResponse<JsonNode> publishJUnitFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases,
                                                                         final File zip,
                                                                         final ExpandedCustomTestCycle expandedCustomTestCycle)
        throws UnirestException;

    public abstract HttpResponse<String> downloadFeatureFile(final String projectKey) throws UnirestException;

    protected HttpResponse<JsonNode> getBodyAsJsonOrThrowExceptionWithBody(final MultipartBody body) throws UnirestException {
        try {
            return body.asJson();
        } catch (final UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getCloudAddress() {
        return cloudAddress;
    }

    public void setCloudAddress(String cloudAddress) {
        this.cloudAddress = cloudAddress;
    }

    public Secret getJwt() {
        return jwt;
    }

    public void setJwt(Secret jwt) {
        this.jwt = jwt;
    }
}
