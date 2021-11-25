package com.adaptavist.tm4j.jenkins.extensions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import java.io.File;

public abstract class Instance {

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
            throw new RuntimeException(body.asString().getBody());
        }
    }
}
