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
        final String name,
        final String description,
        final String jiraProjectVersion,
        final String folderId,
        final String customFields
    ) {
        this.name = setStringIfNotBlank(name);
        this.description = setStringIfNotBlank(description);
        this.jiraProjectVersion = jiraProjectVersion(jiraProjectVersion);
        this.folderId = jiraProjectVersion(folderId);
        this.customFields = convertCustomFieldsToMapIfValid(customFields);
    }

    public String getName() {
        return name;
    }

//    public String getTestCycleName() {
//        return this.getName();
//    }

    public String getDescription() {
        return description;
    }

//    public String getTestCycleDescription() {
//        return this.getDescription();
//    }

//    public Long getJiraProjectVersion() {
//        return jiraProjectVersion;
//    }

    public String getJiraProjectVersion() {
        return this.jiraProjectVersion == null ? null : this.jiraProjectVersion.toString();
    }

//    public Long getFolderId() {
//        return folderId;
//    }

    public String getFolderId() {
        return this.folderId == null ? null : this.folderId.toString();
    }

//    public Map<String, Object> getCustomFields() {
//        return this.customFields;
//    }

    public String getCustomFields() {
        return this.customFields.isEmpty() ? null : GsonUtils.getInstance().toJson(this.customFields);
    }

    public boolean isEmpty() {
        return isBlank(name)
            && isBlank(description)
            && (customFields == null || customFields.size() == 0)
            && (jiraProjectVersion == null || jiraProjectVersion == 0)
            && (folderId == null || folderId == 0);
    }

    private String setStringIfNotBlank(final String value) {
        return isBlank(value)
            ? null
            : value;
    }

    private Long jiraProjectVersion(final String value) {
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
