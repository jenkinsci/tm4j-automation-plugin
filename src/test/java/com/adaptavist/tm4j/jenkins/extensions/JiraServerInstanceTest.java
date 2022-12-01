package com.adaptavist.tm4j.jenkins.extensions;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import hudson.EnvVars;
import hudson.util.Secret;
import java.io.File;
import java.util.stream.Stream;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class JiraServerInstanceTest {

    private static final String PROJECT_KEY = "DEFAULT";
    private static final String BASE_URL = "https://example.myorg.com";
    private static final String USERNAME = "user123";
    private static final String PASSWORD = "my-password";

    private static final String CUCUMBER_ENDPOINT = "rest/atm/1.0/automation/execution/cucumber/DEFAULT";
    private static final String CUSTOM_FORMAT_ENDPOINT = "rest/atm/1.0/automation/execution/DEFAULT";
    private static final String FEATURE_FILES_ENDPOINT = "rest/atm/1.0/automation/testcases";
    private static final String HEALTH_CHECK_ENDPOINT = "rest/atm/1.0/healthcheck/";

    private static ExpandedCustomTestCycle getExpandedTestCycle() {
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";

        final String customFields =
            "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line<br />second line\"}";

        final CustomTestCycle customTestCycle =  new CustomTestCycle("Custom Build", description, jiraProjectVersion, folderId, customFields);
        return new ExpandedCustomTestCycle(customTestCycle, new EnvVars());
    }

    private static Stream<Arguments> customTestCycleArgumentProvider() {
        return Stream.of(
            arguments(getExpandedTestCycle())
        );
    }

    @Test
    public void emptyConstructor() {
        final JiraServerInstance jiraServerInstance = new JiraServerInstance();

        assertThat(jiraServerInstance.name()).isNull();
        assertThat(jiraServerInstance.getServerAddress()).isNull();
        assertThat(jiraServerInstance.getUsername()).isNull();
        assertThat(jiraServerInstance.getPassword()).isNull();
    }

    @Test
    public void constructorWithArguments() {
        final Secret secret = getSecret();
        final JiraServerInstance jiraServerInstance = new JiraServerInstance(BASE_URL, USERNAME, secret);

        assertThat(jiraServerInstance.name()).isEqualTo(BASE_URL);
        assertThat(jiraServerInstance.getServerAddress()).isEqualTo(BASE_URL);
        assertThat(jiraServerInstance.getUsername()).isEqualTo(USERNAME);
        assertThat(jiraServerInstance.getPassword()).isEqualTo(secret);
    }

    @Test
    public void isCloud() {
        final JiraServerInstance jiraServerInstance = new JiraServerInstance();
        assertThat(jiraServerInstance.cloud()).isFalse();
    }

    @Test
    public void isValidCredentials() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.basicAuth(USERNAME, PASSWORD)).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(200);

            final Boolean validCredentials = jiraServerInstance.isValidCredentials();

            assertThat(validCredentials).isTrue();

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void isValidCredentials_falseIfExceptionIsThrown() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.basicAuth(USERNAME, PASSWORD)).thenReturn(getRequest);
            when(getRequest.asString()).thenThrow(new UnirestException("Request Failed"));

            final Boolean validCredentials = jiraServerInstance.isValidCredentials();

            assertThat(validCredentials).isFalse();

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @ParameterizedTest(name = "isValidCredentials for status code {0} should be false")
    @ValueSource(ints = {100, 101, 102, 201, 202, 203, 204, 205, 206, 207, 300, 301, 302, 303, 304, 305, 307, 400, 401, 402, 403, 404, 405,
        406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 419, 420, 422, 423, 424, 500, 501, 502, 503, 504, 505, 507})
    public void isValidCredentials_falseIfStatusIsNot200(final Integer statusCode) throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.basicAuth(USERNAME, PASSWORD)).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(statusCode);

            final Boolean validCredentials = jiraServerInstance.isValidCredentials();

            assertThat(validCredentials).isFalse();

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void downloadFeatureFile() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(FEATURE_FILES_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.basicAuth(USERNAME, PASSWORD)).thenReturn(getRequest);
            when(getRequest.queryString("tql", format("testCase.projectKey = '%s'", PROJECT_KEY))).thenReturn(httpRequest);
            when(httpRequest.asString()).thenReturn(httpResponse);

            HttpResponse<String> actualResponse = jiraServerInstance.downloadFeatureFile(PROJECT_KEY);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @ParameterizedTest
    @NullSource
    public void publishCucumberFormatBuildResult_withoutCustomTestCycle(final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUCUMBER_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.basicAuth(USERNAME, PASSWORD)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);
            when(multipartBody.asJson()).thenReturn(httpResponse);

            jiraServerInstance.publishCucumberFormatBuildResult(PROJECT_KEY, true, zip, expandedCustomTestCycle);

            verify(multipartBody, never()).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("customTestCycleArgumentProvider")
    public void publishCucumberFormatBuildResult(final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUCUMBER_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.basicAuth(USERNAME, PASSWORD)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);
            when(multipartBody.field("testCycle", GsonUtils.getInstance().toJson(expandedCustomTestCycle), "application/json")).thenReturn(
                    multipartBody);
            when(multipartBody.asJson()).thenReturn(httpResponse);

            jiraServerInstance.publishCucumberFormatBuildResult(PROJECT_KEY, true, zip, expandedCustomTestCycle);

            verify(multipartBody).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @ParameterizedTest
    @NullSource
    public void publishCustomFormatBuildResult_withoutCustomTestCycle(final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUSTOM_FORMAT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.basicAuth(USERNAME, PASSWORD)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);
            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraServerInstance.publishCustomFormatBuildResult(PROJECT_KEY, true, zip, expandedCustomTestCycle);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verify(multipartBody, never()).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @ParameterizedTest
    @MethodSource("customTestCycleArgumentProvider")
    public void publishCustomFormatBuildResult(final ExpandedCustomTestCycle expandedCustomTestCycle) throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraServerInstance jiraServerInstance = getValidJiraInstance();
            jiraServerInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUSTOM_FORMAT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.basicAuth(USERNAME, PASSWORD)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);
            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraServerInstance.publishCustomFormatBuildResult(PROJECT_KEY, true, zip, expandedCustomTestCycle);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verify(multipartBody).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void publishJUnitFormatBuildResult() {
        final JiraServerInstance jiraServerInstance = getValidJiraInstance();
        final File zip = mock(File.class);

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> jiraServerInstance.publishJUnitFormatBuildResult(PROJECT_KEY, true, zip, null))
            .withMessage("Not implemented for Zephyr Scale Server/DC");

    }

    @Test
    public void getBodyAsJsonOrThrowExceptionWithBody() throws UnirestException {
        // given
        final MultipartBody multipartBody = mock(MultipartBody.class);

        when(multipartBody.asJson()).thenThrow(new UnirestException("Something went terribly wrong!"));

        final JiraServerInstance jiraServerInstance = this.getValidJiraInstance();

        // when then
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> jiraServerInstance.getBodyAsJsonOrThrowExceptionWithBody(multipartBody))
                .withMessageContaining("Something went terribly wrong!");
    }

    private JiraServerInstance getValidJiraInstance() {
        return new JiraServerInstance(BASE_URL, USERNAME, getSecret());
    }

    private Secret getSecret() {
        return Secret.fromString(PASSWORD);
    }
}
