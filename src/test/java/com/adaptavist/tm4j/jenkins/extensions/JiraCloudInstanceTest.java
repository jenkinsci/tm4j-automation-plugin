package com.adaptavist.tm4j.jenkins.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.adaptavist.tm4j.jenkins.exception.InvalidJwtException;
import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import hudson.util.Secret;
import java.io.File;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class JiraCloudInstanceTest {

    private static final String PROJECT_KEY = "DEFAULT";
    private static final String BASE_URL = "https://example.atlassian.net";
    private static final String CUCUMBER_ENDPOINT = "v2/automations/executions/cucumber";
    private static final String JUNIT_ENDPOINT = "v2/automations/executions/junit";
    private static final String CUSTOM_FORMAT_ENDPOINT = "v2/automations/executions/custom";
    private static final String FEATURE_FILES_ENDPOINT = "v2/automations/testcases";
    private static final String HEALTH_CHECK_ENDPOINT = "v2/healthcheck";

    private static final String VALID_JWT =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MzcxNDg1NTQsImV4cCI6MTYzNzE1MjE1NCwiaXNzIjoiY29tLmthbm9haC50ZXN0LW1hbmFnZXIiLCJzdWIiOiJjbGllbnQta2V5IiwiY29udGV4dCI6eyJiYXNlVXJsIjoiaHR0cHM6Ly9leGFtcGxlLmF0bGFzc2lhbi5uZXQiLCJ1c2VyIjp7ImFjY291bnRJZCI6IjRmN2I3ZGYxZGRmZWRjOWI3ZTA5MzdiYiJ9fX0.xW7BNqE3XVP2_MLIcKBic4-N1gdsTGfDe03Xm1rEP-w";

    private static final String INVALID_JWT =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MzcxNDg1NTQsImV4cCI6MTYzNzE1MjF1NCwiaXNzIjoiY29tLmthbm9haC50ZXN0LW1hbmFnZXIiLCJzdWIiOiJjbGllbnQta2V5IiwiY29udGV4dCI6eyJiYXNlVXJsIjoiaHR0cHM6Ly9leGFtcGxlLmF0bGFzc2lgbi5uZXQiLCJ1c2VyIjp7ImFjY291bnRJZCI6IjRmN2I3ZGYxZGRmZWRjOWI3ZTA5MzdiYiJ9fX0.xW7BNqE3XVP2_MLIcKBic4-N1gdsTGfDe03Xm1rEP-w";

    private static final String VALID_JWT_WITHOUT_CONTEXT =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MzcxNDk0NzEsImV4cCI6MTYzNzE1MzA3MSwiaXNzIjoiY29tLmthbm9haC50ZXN0LW1hbmFnZXIiLCJzdWIiOiJjbGllbnQta2V5In0.rfx5VvrKlpR1rm6G6ct8dT8tz3tViG9uD7xMWRi2r4o";

    private static final String VALID_JWT_WITHOUT_BASE_URL =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2MzcxNDkyNzMsImV4cCI6MTYzNzE1Mjg3MywiaXNzIjoiY29tLmthbm9haC50ZXN0LW1hbmFnZXIiLCJzdWIiOiJjbGllbnQta2V5IiwiY29udGV4dCI6eyJ1c2VyIjp7ImFjY291bnRJZCI6IjRmN2I3ZGYxZGRmZWRjOWI3ZTA5MzdiYiJ9fX0.ZJBPnhJx_BwjYtIsFC3FnseIDziHV0uI4VxOECMFdiM";

    private static CustomTestCycle getCustomTestCycle(final String name) {
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";

        final String customFields =
            "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line<br />second line\"}";

        return new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);
    }

    @Test
    public void emptyConstructor() {
        final JiraCloudInstance jiraCloudInstance = new JiraCloudInstance();

        assertThat(jiraCloudInstance.name()).isNull();
        assertThat(jiraCloudInstance.getJwt()).isNull();
    }

    @Test
    public void constructorWithArguments() {
        final Secret secret = getSecret(VALID_JWT);
        final JiraCloudInstance jiraCloudInstance = new JiraCloudInstance(secret);

        assertThat(jiraCloudInstance.getJwt()).isEqualTo(secret);
        assertThat(jiraCloudInstance.name()).isEqualTo(BASE_URL);
        assertThat(jiraCloudInstance.getCloudAddress()).isEqualTo(BASE_URL);
    }

    @Test
    public void constructorWithArguments_invalidJwt() {
        final Secret secret = getSecret(INVALID_JWT);

        assertThatExceptionOfType(InvalidJwtException.class)
            .isThrownBy(() -> new JiraCloudInstance(secret))
            .withMessage("java.text.ParseException: Payload of JWS object is not a valid JSON object");
    }

    @Test
    public void constructorWithArguments_validJwtMissingContextProperty() {
        final Secret secret = getSecret(VALID_JWT_WITHOUT_CONTEXT);

        final JiraCloudInstance jiraCloudInstance = new JiraCloudInstance(secret);

        assertThat(jiraCloudInstance.getJwt()).isEqualTo(secret);
        assertThat(jiraCloudInstance.name()).isNull();
        assertThat(jiraCloudInstance.getCloudAddress()).isNull();
    }

    @Test
    public void constructorWithArguments_validJwtMissingBaseUrlProperty() {
        final Secret secret = getSecret(VALID_JWT_WITHOUT_BASE_URL);

        final JiraCloudInstance jiraCloudInstance = new JiraCloudInstance(secret);

        assertThat(jiraCloudInstance.getJwt()).isEqualTo(secret);
        assertThat(jiraCloudInstance.name()).isNull();
        assertThat(jiraCloudInstance.getCloudAddress()).isNull();
    }

    @Test
    public void isCloud() {
        final JiraCloudInstance jiraCloudInstance = new JiraCloudInstance();
        assertThat(jiraCloudInstance.cloud()).isTrue();
        assertThat(jiraCloudInstance.getCloud()).isTrue();
    }

    @Test
    public void publishCucumberFormatBuildResult_withCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final CustomTestCycle customTestCycle = getCustomTestCycle("Cucumber Build");
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponseJsonNode = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUCUMBER_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.field("testCycle", GsonUtils.getInstance().toJson(customTestCycle), "application/json")).thenReturn(
                multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponseJsonNode);

            jiraCloudInstance.publishCucumberFormatBuildResult(PROJECT_KEY, true, zip, customTestCycle);

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponseJsonNode);
        }
    }

    @Test
    public void publishCucumberFormatBuildResult_withoutCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUCUMBER_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraCloudInstance.publishCucumberFormatBuildResult(PROJECT_KEY, true, zip, null);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verify(multipartBody, never()).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void publishCustomFormatBuildResult_withCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final CustomTestCycle customTestCycle = getCustomTestCycle("Custom Format Build");
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUSTOM_FORMAT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.field("testCycle", GsonUtils.getInstance().toJson(customTestCycle), "application/json")).thenReturn(
                multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraCloudInstance.publishCustomFormatBuildResult(PROJECT_KEY, true, zip, customTestCycle);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void publishCustomFormatBuildResult_withoutCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(CUSTOM_FORMAT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraCloudInstance.publishCustomFormatBuildResult(PROJECT_KEY, true, zip, null);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verify(multipartBody, never()).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void publishJUnitFormatBuildResult_withCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final CustomTestCycle customTestCycle = getCustomTestCycle("JUnit Build");
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(JUNIT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.field("testCycle", GsonUtils.getInstance().toJson(customTestCycle), "application/json")).thenReturn(
                multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraCloudInstance.publishJUnitFormatBuildResult(PROJECT_KEY, true, zip, customTestCycle);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void publishJUnitFormatBuildResult_withoutCustomTestCycle() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final File zip = mock(File.class);
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final HttpRequestWithBody httpRequestWithBody = mock(HttpRequestWithBody.class);
            final MultipartBody multipartBody = mock(MultipartBody.class);
            final HttpResponse<JsonNode> httpResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.post(endsWith(JUNIT_ENDPOINT))).thenReturn(httpRequestWithBody);

            when(httpRequestWithBody.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.header("zscale-source", "Jenkins Plugin")).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("autoCreateTestCases", true)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequestWithBody);
            when(httpRequestWithBody.field("file", zip)).thenReturn(multipartBody);

            when(multipartBody.asJson()).thenReturn(httpResponse);

            HttpResponse<JsonNode> actualResponse = jiraCloudInstance.publishJUnitFormatBuildResult(PROJECT_KEY, true, zip, null);

            assertThat(actualResponse).isEqualTo(httpResponse);

            verify(multipartBody, never()).field(eq("testCycle"), anyString(), eq("application/json"));

            verifyNoMoreInteractions(zip);
            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(httpRequestWithBody);
            verifyNoMoreInteractions(multipartBody);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void downloadFeatureFile() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(FEATURE_FILES_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(getRequest);
            when(getRequest.header("Accept", "application/zip")).thenReturn(getRequest);
            when(getRequest.queryString("projectKey", PROJECT_KEY)).thenReturn(httpRequest);
            when(httpRequest.asString()).thenReturn(httpResponse);

            HttpResponse<String> actualResponse = jiraCloudInstance.downloadFeatureFile(PROJECT_KEY);
            assertThat(actualResponse).isEqualTo(httpResponse);

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void isValidCredentials() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(200);

            final Boolean validCredentials = jiraCloudInstance.isValidCredentials();

            assertThat(validCredentials).isTrue();

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    @Test
    public void isValidCredentials_falseIfExceptionIsThrown() throws UnirestException {
        try (final MockedStatic<Unirest> unirest = mockStatic(Unirest.class)) {
            final HttpClient httpClient = mock(HttpClient.class);

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(getRequest);
            when(getRequest.asString()).thenThrow(new UnirestException("Request Failed"));

            final Boolean validCredentials = jiraCloudInstance.isValidCredentials();

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

            final JiraCloudInstance jiraCloudInstance = getValidJiraCloudInstance();
            jiraCloudInstance.setUnirestHttpClient(httpClient);

            final GetRequest getRequest = mock(GetRequest.class);
            final HttpRequest httpRequest = mock(HttpRequest.class);
            final HttpResponse<String> httpResponse = (HttpResponse<String>) mock(HttpResponse.class);

            unirest.when(() -> Unirest.get(endsWith(HEALTH_CHECK_ENDPOINT))).thenReturn(getRequest);

            when(getRequest.header("Authorization", "Bearer " + VALID_JWT)).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getStatus()).thenReturn(statusCode);

            final Boolean validCredentials = jiraCloudInstance.isValidCredentials();

            assertThat(validCredentials).isFalse();

            verifyNoMoreInteractions(httpClient);
            verifyNoMoreInteractions(getRequest);
            verifyNoMoreInteractions(httpRequest);
            verifyNoMoreInteractions(httpResponse);
        }
    }

    private JiraCloudInstance getValidJiraCloudInstance() {
        final Secret secret = getSecret(VALID_JWT);
        return new JiraCloudInstance(secret);
    }

    private Secret getSecret(final String jwt) {
        return Secret.fromString(jwt);
    }
}
