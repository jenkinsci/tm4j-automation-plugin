package com.adaptavist.tm4j.jenkins.extensions;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import hudson.EnvVars;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CustomTestCycleTest {

    private static Stream<Arguments> isEmptyIfAllValuesAreBlank_valueProvider() {
        return Stream.of(
            arguments(null, null, null, null, null),
            arguments("", "", "", "", ""),
            arguments("  ", "  ", "  ", "  ", "  ")
        );
    }

    @ParameterizedTest
    @MethodSource("isEmptyIfAllValuesAreBlank_valueProvider")
    public void isEmptyIfAllValuesAreEitherBlankNullOrHasSizeZero(final String name, final String description,
                                                                  final String jiraProjectVersion, final String folderId,
                                                                  final String customFields) {
        final CustomTestCycle customTestCycle = new CustomTestCycle(
            name,
            description,
            jiraProjectVersion,
            folderId,
            customFields
        );

        assertThat(customTestCycle.isEmpty()).isTrue();
    }

    @Test
    public void validCustomTestCycle() {
        final String name = "Custom Name";
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";
        final String customFields =
            "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line<br />second line\"}";

        final CustomTestCycle customTestCycle =
            new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

        assertThat(customTestCycle.getName()).isEqualTo(name);
        assertThat(customTestCycle.getDescription()).isEqualTo(description);
        assertThat(customTestCycle.getJiraProjectVersion()).isEqualTo(jiraProjectVersion);
        assertThat(customTestCycle.getFolderId()).isEqualTo(folderId);
        assertThat(customTestCycle.getCustomFields()).isEqualTo(customFields);
    }

    @Test
    public void expandVars() {
        Map<String, String> env = new HashMap<>();
        env.put("BUILD_NUMBER", "10");
        env.put("JENKINS_URL", "http://localhost:8080/jenkins/");
        env.put("BUILD_ID", "15");
        env.put("JIRA_PROJECT_VERSION", "10001");
        env.put("FOLDER_ID", "1234");
        env.put("CI", "true");

        EnvVars envVars = new EnvVars(env);

        final String name = "Build #${BUILD_NUMBER}";
        final String description = "Build ID '${BUILD_ID}' ran on ${JENKINS_URL}";
        final String jiraProjectVersion = "${JIRA_PROJECT_VERSION}";
        final String folderId = "${FOLDER_ID}";
        final String customFields = "{\"automated\": ${CI}}";

        final CustomTestCycle customTestCycle = new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

        final ExpandedCustomTestCycle expandedCustomTestCycle = customTestCycle.expandEnvVars(envVars);

        assertThat(expandedCustomTestCycle.getName()).isEqualTo("Build #10");
        assertThat(expandedCustomTestCycle.getDescription()).isEqualTo("Build ID '15' ran on http://localhost:8080/jenkins/");
        assertThat(expandedCustomTestCycle.getJiraProjectVersion()).isEqualTo("10001");
        assertThat(expandedCustomTestCycle.getFolderId()).isEqualTo("1234");
        assertThat(expandedCustomTestCycle.getCustomFields()).isEqualTo("{\"automated\":true}");
    }

    @Test
    public void _toString() {
        final String name = "Custom Name";
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";
        final String customFields =            "{\"number\":50}";

        final CustomTestCycle customTestCycle = new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

        final String expectedToString = "CustomTestCycle: {\n" +
            "    name: Custom Name,\n" +
            "    description: Description,\n" +
            "    jiraProjectVersion: 10001,\n" +
            "    folderId: 1234,\n" +
            "    customFields: {\"number\":50}\n" +
            "}";

        assertThat(customTestCycle.toString()).isEqualTo(expectedToString);
    }
}
