package com.adaptavist.tm4j.jenkins.extensions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.File;

public interface Instance {

    Boolean cloud();

    String name();

    Boolean isValidCredentials();

    HttpResponse<JsonNode> publishCucumberFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                            final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException;

    HttpResponse<JsonNode> publishCustomFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                          final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException;

    HttpResponse<JsonNode> publishJUnitFormatBuildResult(final String projectKey, final Boolean autoCreateTestCases, final File zip,
                                                         final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException;

    HttpResponse<String> downloadFeatureFile(final String projectKey) throws UnirestException;

}
