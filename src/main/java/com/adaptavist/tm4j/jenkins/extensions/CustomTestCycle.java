package com.adaptavist.tm4j.jenkins.extensions;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.adaptavist.tm4j.jenkins.utils.GsonUtils;
import com.google.gson.reflect.TypeToken;
import hudson.EnvVars;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.stapler.DataBoundConstructor;

public class CustomTestCycle {
    protected String name;
    protected String description;
    protected Long jiraProjectVersion;
    protected Long folderId;
    protected Map<String, Object> customFields;

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
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getJiraProjectVersion() {
        return this.jiraProjectVersion == null
            ? null
            : this.jiraProjectVersion.toString();
    }

    public String getFolderId() {
        return this.folderId == null
            ? null
            : this.folderId.toString();
    }

    public String getCustomFields() {
        return this.customFields.isEmpty()
            ? null
            : GsonUtils.getInstance().toJson(this.customFields);
    }

    public boolean isEmpty() {
        return isBlank(name)
            && isBlank(description)
            && (customFields == null || customFields.size() == 0)
            && (jiraProjectVersion == null || jiraProjectVersion == 0)
            && (folderId == null || folderId == 0);
    }

    public void expandEnvVars(final EnvVars envVars) {
        final String expandedName = envVars.expand(this.getName());
        final String expandedDescription = envVars.expand(this.getDescription());
        final String expandedJiraProjectVersion = envVars.expand(this.getJiraProjectVersion());
        final String expandedFolderId = envVars.expand(this.getFolderId());
        final String expandedCustomFields = envVars.expand(this.getCustomFields());

        this.name = setStringIfNotBlank(expandedName);
        this.description = setStringIfNotBlank(expandedDescription);
        this.jiraProjectVersion = jiraProjectVersion(expandedJiraProjectVersion);
        this.folderId = jiraProjectVersion(expandedFolderId);
        this.customFields = convertCustomFieldsToMapIfValid(expandedCustomFields);
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
