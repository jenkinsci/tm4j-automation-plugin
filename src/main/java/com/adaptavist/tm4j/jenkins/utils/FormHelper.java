package com.adaptavist.tm4j.jenkins.utils;

import com.adaptavist.tm4j.jenkins.exception.InvalidJwtException;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.JiraCloudInstance;
import com.adaptavist.tm4j.jenkins.extensions.JiraServerInstance;
import hudson.Util;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;

import java.util.List;

import static com.adaptavist.tm4j.jenkins.utils.Constants.*;

public class FormHelper {

    public FormValidation testConnection(String serverAddress, String username, String password, String jwt, String type) {
        return "cloud".equals(type) ? testConnectionCloud(jwt) : testConnectionServer(serverAddress, username, password);
    }

    private FormValidation testConnectionServer(String serverAddress, String username, String password) {
        if (serverAddress != null && serverAddress.endsWith("/")) {
            serverAddress = serverAddress.substring(0, serverAddress.length() - 1);
        }
        if (Util.fixEmptyAndTrim(serverAddress) == null) {
            return FormValidation.error(PLEASE_ENTER_THE_SERVER_NAME);
        }
        if (Util.fixEmptyAndTrim(username) == null) {
            return FormValidation.error(PLEASE_ENTER_THE_USERNAME);
        }
        if (Util.fixEmptyAndTrim(password) == null) {
            return FormValidation.error(PLEASE_ENTER_THE_PASSWORD);
        }
        if (!(serverAddress.trim().startsWith("https://") || serverAddress.trim().startsWith("http://"))) {
            return FormValidation.error(INCORRECT_SERVER_ADDRESS_FORMAT);
        }
        if (!new JiraServerInstance(serverAddress, username, Secret.fromString(password)).isValidCredentials()) {
            return FormValidation.error(INVALID_CREDENTIALS);
        }
        return FormValidation.ok(CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED);
    }

    private FormValidation testConnectionCloud(String jwt) {
        try {
            JiraCloudInstance instance = new JiraCloudInstance(Secret.fromString(jwt));
            if (!instance.isValidCredentials()) {
                return FormValidation.error(INVALID_CREDENTIALS);
            }
        } catch (InvalidJwtException e) {
            return FormValidation.error(INVALID_CREDENTIALS);
        }
        return FormValidation.ok(CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED);
    }

    public ListBoxModel fillServerAddressItems(List<Instance> jiraInstances) {
        ListBoxModel modelbox = new ListBoxModel();
        if (jiraInstances == null || jiraInstances.isEmpty()) {
            modelbox.add(ADD_ZEPHYR_GLOBAL_CONFIG);
            return modelbox;
        }
        for (Instance server : jiraInstances) {
            modelbox.add(server.name());
        }
        return modelbox;
    }

    public ListBoxModel fillFormat() {
        ListBoxModel modelbox = new ListBoxModel();
        modelbox.add(CUCUMBER);
        modelbox.add(ZEPHYR_OUTPUT_RESULT_FOR_JUNIT);
        modelbox.add(JUNIT_RESULT_FILE);
        return modelbox;
    }


    public FormValidation doCheckProjectKey(String projectKey) {
        return Util.fixEmptyAndTrim(projectKey) == null ? FormValidation.error(Constants.PROJECT_KEY_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckFilePath(String filePath) {
        return Util.fixEmptyAndTrim(filePath) == null ? FormValidation.error(Constants.FILE_PATH_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckTargetPath(String fileTarget) {
    	return Util.fixEmptyAndTrim(fileTarget) == null ? FormValidation.error(Constants.FILE_TARGET_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckServerAddress(String serverAddress) {
        return Util.fixEmptyAndTrim(serverAddress) == null ? FormValidation.error(PLEASE_ENTER_THE_SERVER_NAME) : FormValidation.ok();
    }

    public FormValidation doCheckUsername(String userName) {
        return Util.fixEmptyAndTrim(userName) == null ? FormValidation.error(PLEASE_ENTER_THE_USERNAME) : FormValidation.ok();
    }

    public FormValidation doCheckPassword(String password) {
        return Util.fixEmptyAndTrim(password) == null ? FormValidation.error(PLEASE_ENTER_THE_PASSWORD) : FormValidation.ok();
    }

    public FormValidation doCheckJwt(String jwt) {
        return Util.fixEmptyAndTrim(jwt) == null ? FormValidation.error(PLEASE_ENTER_THE_JWT) : FormValidation.ok();
    }
}
