package com.adaptavist.tm4j.jenkins.extensions;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.stapler.DataBoundConstructor;

public class CustomTestCycle {
    private final String name;
    private final String description;
    private final Long jiraProjectVersion;
    private final Long folderId;
    private final Map<String, Object> customFields;

    @DataBoundConstructor
    public CustomTestCycle(
        final String testCycleName,
        final String testCycleDescription,
        final String testCycleJiraProjectVersionId,
        final String testCycleFolderId,
        final String testCycleCustomFields
    ) {
        this.name = testCycleName;
        this.description = testCycleDescription;
        this.jiraProjectVersion = convertToLongIfValid(testCycleJiraProjectVersionId);
        this.folderId = convertToLongIfValid(testCycleFolderId);
        this.customFields = convertCustomFieldsToMapIfValid(testCycleCustomFields);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getJiraProjectVersion() {
        return jiraProjectVersion;
    }

    public Long getFolderId() {
        return folderId;
    }

    public Map<String, Object> getCustomFields() {
        return this.customFields;
    }

    public boolean isEmpty() {
        return isBlank(name)
            && isBlank(description)
            && (customFields == null || customFields.size() == 0)
            && (jiraProjectVersion == null || jiraProjectVersion == 0)
            && (folderId == null || folderId == 0);
    }

    private Long convertToLongIfValid(final String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> convertCustomFieldsToMapIfValid(final String customFieldsJson) {
        try {
            /*
            For security reasons, Jenkins doesn't allow us to marshal
            properties that are instances of any type of Gson classes.
            That's why we need to convert Gson's LinkedTreeMap to a
            HashMap. See https://jenkins.io/redirect/class-filter
             */
            return new HashMap<>(
                GsonUtils.getInstance()
                    .fromJson(
                        customFieldsJson,
                        new TypeToken<Map<String, Object>>() {
                        }.getType()
                    )
            );
        } catch (final Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public String toString() {
        return String.format(
            "CustomTestCycle: {%n" +
                "    name: %s,%n" +
                "    description: %s,%n" +
                "    jiraProjectVersion: %s,%n" +
                "    folderId: %s,%n" +
                "    customFields: %s%n" +
                "}",
            name,
            description,
            jiraProjectVersion,
            folderId,
            customFields
        );
    }
}
