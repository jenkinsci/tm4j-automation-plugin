package com.adaptavist.tm4j.jenkins.extensions;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CustomTestCycleTest {

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

        assertThat(customTestCycle.jiraProjectVersion).isNull();
        assertThat(customTestCycle.getJiraProjectVersion()).isNull();

        assertThat(customTestCycle.folderId).isNull();
        assertThat(customTestCycle.getFolderId()).isNull();
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

        assertThat(customTestCycle.customFields).isEmpty();
        assertThat(customTestCycle.getCustomFields()).isNull();
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

        assertThat(customTestCycle.isEmpty()).isTrue();
    }

    @Test
    public void validCustomTestCycle() {
        final String name = "Custom Name";
        final String description = "Description";
        final String jiraProjectVersion = "10001";
        final String folderId = "1234";

        final String customFields = "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line<br />second line\"}";

        final String expectedCustomFieldsJson = "{\"number\":50,\"single-choice\":\"option1\",\"checkbox\":true,\"userpicker\":\"5f8b5cf2ddfdcb0b8d1028bb\",\"single-line\":\"a text line\",\"datepicker\":\"2021-12-31\",\"decimal\":10.5,\"multi-choice\":[\"choice1\",\"choice2\"],\"multi-line\":\"first line\\u003cbr /\\u003esecond line\"}";

        final CustomTestCycle customTestCycle =
            new CustomTestCycle(name, description, jiraProjectVersion, folderId, customFields);

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

        assertThat(customTestCycle.name).isEqualTo(name);
        assertThat(customTestCycle.description).isEqualTo(description);
        assertThat(customTestCycle.jiraProjectVersion).isEqualTo(10001L);
        assertThat(customTestCycle.folderId).isEqualTo(1234L);
        assertThat(customTestCycle.customFields).isEqualTo(expectedCustomFieldsMap);

        assertThat(customTestCycle.getName()).isEqualTo(name);
        assertThat(customTestCycle.getDescription()).isEqualTo(description);
        assertThat(customTestCycle.getJiraProjectVersion()).isEqualTo(jiraProjectVersion);
        assertThat(customTestCycle.getFolderId()).isEqualTo(folderId);
        assertThat(customTestCycle.getCustomFields()).isEqualTo(expectedCustomFieldsJson);
    }
}
