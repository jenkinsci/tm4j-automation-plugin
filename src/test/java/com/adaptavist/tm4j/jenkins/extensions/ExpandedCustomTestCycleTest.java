package com.adaptavist.tm4j.jenkins.extensions;


import static java.util.Arrays.asList;
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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ExpandedCustomTestCycleTest {

    private static Stream<Arguments> isEmptyIfAllValuesAreEitherBlankNullOrHasSizeZero_valueProvider() {
        return Stream.of(
            arguments(null, null, null, null, null),
            arguments("", "", "", "", ""),
            arguments(" ", " ", "1,5", ".5", "{\"checkbox\": true,}"),
            arguments(" ", " ", "0", "0", "{}")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "aNumber", "1,5", ".5", "1.", "."})
    public void jiraProjectVersionAndFolderIdAreSetToNullIfCantBeConvertedToLong(final String idValue) {
        final CustomTestCycle customTestCycle = new CustomTestCycle(
            "name",
            "desc",
            idValue,
            idValue,
            "{\"checkbox\": true}"
        );

        final ExpandedCustomTestCycle expandedCustomTestCycle = new ExpandedCustomTestCycle(customTestCycle, new EnvVars());

        assertThat(expandedCustomTestCycle.jiraProjectVersion).isNull();
        assertThat(expandedCustomTestCycle.getJiraProjectVersion()).isNull();

        assertThat(expandedCustomTestCycle.folderId).isNull();
        assertThat(expandedCustomTestCycle.getFolderId()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "{\"checkbox\": true,}", "{\"checkbox: true}", "{\"checkbox\": true", "{\"checkbox\":}", "{: true}"})
    public void customFieldsIsSetToEmptyHashIfCantBeParsed(final String customFields) {
        final CustomTestCycle customTestCycle = new CustomTestCycle(
            "name",
            "desc",
            "1",
            "2",
            customFields
        );

        final ExpandedCustomTestCycle expandedCustomTestCycle = new ExpandedCustomTestCycle(customTestCycle, new EnvVars());

        assertThat(expandedCustomTestCycle.customFields).isEmpty();
        assertThat(expandedCustomTestCycle.getCustomFields()).isNull();
    }

    @ParameterizedTest
    @MethodSource("isEmptyIfAllValuesAreEitherBlankNullOrHasSizeZero_valueProvider")
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

        final ExpandedCustomTestCycle expandedCustomTestCycle = new ExpandedCustomTestCycle(customTestCycle, new EnvVars());

        assertThat(expandedCustomTestCycle.isEmpty()).isTrue();
    }

    @Test
    public void validCustomTestCycle() {
        final String name = "Custom Name";
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";

        final String customFields =
            "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line<br />second line\"}";

        final String expectedCustomFieldsJson =
            "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line\\u003cbr /\\u003esecond line\"}";

        final CustomTestCycle customTestCycle =
            new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

        final ExpandedCustomTestCycle expandedCustomTestCycle = new ExpandedCustomTestCycle(customTestCycle, new EnvVars());

        final Map<String, Object> expectedCustomFieldsMap = new HashMap<>();
        expectedCustomFieldsMap.put("checkbox", true);
        expectedCustomFieldsMap.put("datepicker", "2021-12-31");
        expectedCustomFieldsMap.put("number", 50.0);
        expectedCustomFieldsMap.put("decimal", 10.5);
        expectedCustomFieldsMap.put("single-choice", "option1");
        expectedCustomFieldsMap.put("userpicker", "5f8b5cf2ddfdcb0b8d1028bb");
        expectedCustomFieldsMap.put("single-line", "a text line");
        expectedCustomFieldsMap.put("multi-choice", asList("choice1", "choice2"));
        expectedCustomFieldsMap.put("multi-line", "first line<br />second line");

        assertThat(expandedCustomTestCycle.name).isEqualTo(name);
        assertThat(expandedCustomTestCycle.description).isEqualTo(description);
        assertThat(expandedCustomTestCycle.jiraProjectVersion).isEqualTo(10001L);
        assertThat(expandedCustomTestCycle.folderId).isEqualTo(1234L);
        assertThat(expandedCustomTestCycle.customFields).isEqualTo(expectedCustomFieldsMap);

        assertThat(expandedCustomTestCycle.getName()).isEqualTo(name);
        assertThat(expandedCustomTestCycle.getDescription()).isEqualTo(description);
        assertThat(expandedCustomTestCycle.getJiraProjectVersion()).isEqualTo(jiraProjectVersion);
        assertThat(expandedCustomTestCycle.getFolderId()).isEqualTo(folderId);
        assertThat(expandedCustomTestCycle.getCustomFields()).isEqualTo(expectedCustomFieldsJson);
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

        final ExpandedCustomTestCycle expandedCustomTestCycle = new ExpandedCustomTestCycle(customTestCycle, envVars);

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
        final String customFields = "{\"number\":50}";

        final CustomTestCycle customTestCycle = new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

        final ExpandedCustomTestCycle expandedCustomTestCycle = customTestCycle.expandEnvVars(new EnvVars());

        final String expectedToString = "ExpandedCustomTestCycle: {\n" +
            "    name: Custom Name,\n" +
            "    description: Description,\n" +
            "    jiraProjectVersion: 10001,\n" +
            "    folderId: 1234,\n" +
            "    customFields: {number=50.0}\n" +
            "}";

        assertThat(expandedCustomTestCycle.toString()).isEqualTo(expectedToString);
    }
}
