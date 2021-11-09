package com.adaptavist.tm4j.jenkins.utils;

import com.mashape.unirest.http.Unirest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class UnirestUtils {
    public static void setUnirestHttpClient() {
        final HttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
        Unirest.setHttpClient(httpClient);
    }
}
