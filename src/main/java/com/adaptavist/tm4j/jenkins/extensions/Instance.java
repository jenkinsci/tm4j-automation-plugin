package com.adaptavist.tm4j.jenkins.extensions;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;

public interface Instance {

    Boolean cloud();

    String name();

    Boolean isValidCredentials();

    HttpResponse<JsonNode> publishCucumberFormatBuildResult(String projectKey, Boolean autoCreateTestCases, File zip) throws UnirestException;

    HttpResponse<JsonNode> publishCustomFormatBuildResult(String projectKey, Boolean autoCreateTestCases, File zip) throws UnirestException;

    HttpResponse<String> downloadFeatureFile(String projectKey) throws UnirestException;

}
