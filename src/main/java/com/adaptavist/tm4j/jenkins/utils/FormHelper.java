package com.adaptavist.tm4j.jenkins.utils;

import com.adaptavist.tm4j.jenkins.exception.InvalidJwtException;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.JiraCloudInstance;
import com.adaptavist.tm4j.jenkins.extensions.JiraServerInstance;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.adaptavist.tm4j.jenkins.utils.Constants.*;

public class FormHelper {

    public FormValidation testConnection(String serverAddress, String username, String password, String jwt, String type) {
        return "cloud".equals(type) ? testConnectionCloud(jwt) : testConnectionServer(serverAddress, username, password);
    }

    private FormValidation testConnectionServer(String serverAddress, String username, String password) {
        serverAddress = StringUtils.removeEnd(serverAddress, "/");
        if (StringUtils.isBlank(serverAddress)) {
            return FormValidation.error(PLEASE_ENTER_THE_SERVER_NAME);
        }
        if (StringUtils.isBlank(username)) {
            return FormValidation.error(PLEASE_ENTER_THE_USERNAME);
        }
        if (StringUtils.isBlank(password)) {
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
            modelbox.add(ADD_ZEPHYR_SCALE_GLOBAL_CONFIG);
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
        modelbox.add(ZEPHYR_SCALE_OUTPUT_RESULT_FOR_JUNIT);
        modelbox.add(JUNIT_RESULT_FILE);
        return modelbox;
    }


    public FormValidation doCheckProjectKey(String projectKey) {
        return StringUtils.isBlank(projectKey) ? FormValidation.error(Constants.PROJECT_KEY_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckFilePath(String filePath) {
        return StringUtils.isBlank(filePath) ? FormValidation.error(Constants.FILE_PATH_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckTargetPath(String fileTarget) {
    	return StringUtils.isBlank(fileTarget) ? FormValidation.error(Constants.FILE_TARGET_IS_REQUIRED) : FormValidation.ok();
    }

    public FormValidation doCheckServerAddress(String serverAddress) {
        return StringUtils.isBlank(serverAddress) ? FormValidation.error(PLEASE_ENTER_THE_SERVER_NAME) : FormValidation.ok();
    }

    public FormValidation doCheckUsername(String userName) {
        return StringUtils.isBlank(userName) ? FormValidation.error(PLEASE_ENTER_THE_USERNAME) : FormValidation.ok();
    }

    public FormValidation doCheckPassword(String password) {
        return StringUtils.isBlank(password) ? FormValidation.error(PLEASE_ENTER_THE_PASSWORD) : FormValidation.ok();
    }

    public FormValidation doCheckJwt(String jwt) {
        return StringUtils.isBlank(jwt) ? FormValidation.error(PLEASE_ENTER_THE_JWT) : FormValidation.ok();
    }
}
