package com.adaptavist.tm4j.jenkins.extensions;

import static org.apache.commons.lang.StringUtils.isBlank;

import hudson.EnvVars;
import org.kohsuke.stapler.DataBoundConstructor;

public class CustomTestCycle {
    protected String name;
    protected String description;
    protected String jiraProjectVersion;
    protected String folderId;
    protected String customFields;

    @DataBoundConstructor
    public CustomTestCycle(
        final String name,
        final String description,
        final String jiraProjectVersion,
        final String folderId,
        final String customFields
    ) {
        this.name = name;
        this.description = description;
        this.jiraProjectVersion = jiraProjectVersion;
        this.folderId = folderId;
        this.customFields = customFields;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getJiraProjectVersion() {
        return this.jiraProjectVersion;
    }

    public String getFolderId() {
        return this.folderId;
    }

    public String getCustomFields() {
        return this.customFields;
    }

    public boolean isEmpty() {
        return isBlank(name)
            && isBlank(description)
            && isBlank(customFields)
            && isBlank(jiraProjectVersion)
            && isBlank(folderId);
    }

    public ExpandedCustomTestCycle expandEnvVars(final EnvVars envVars) {
        return new ExpandedCustomTestCycle(this, envVars);
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
