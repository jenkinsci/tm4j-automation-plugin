package com.adaptavist.tm4j.jenkins.extensions;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.ADD_TM4J_GLOBAL_CONFIG;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.CUCUMBER;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.INCORRECT_SERVER_ADDRESS_FORMAT;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.INVALID_USER_CREDENTIALS;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.PLEASE_ENTER_THE_PASSWORD;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.PLEASE_ENTER_THE_SERVER_NAME;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.PLEASE_ENTER_THE_USERNAME;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.TM4J_OUTPUT_RESULT_FOR_JUNIT;

import java.util.List;

import com.adaptavist.tm4j.jenkins.Tm4jConstants;
import com.adaptavist.tm4j.jenkins.io.RestClient;
import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class Tm4jFormHelper
{

	public FormValidation testConnection(String serverAddress, String username, String password) {
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
		RestClient restClient = new RestClient();
		if (!restClient.isValidCredentials(serverAddress, username, password)) {
			return FormValidation.error(INVALID_USER_CREDENTIALS);
		}
		return FormValidation.ok(CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED);
	}

	public ListBoxModel fillServerAddressItens(List<Tm4JInstance> jiraInstances) {
		ListBoxModel modelbox = new ListBoxModel();
		if (jiraInstances == null || jiraInstances.isEmpty()) {
			modelbox.add(ADD_TM4J_GLOBAL_CONFIG);
			return modelbox;
		}
		for (Tm4JInstance server : jiraInstances) {
			modelbox.add(server.getServerAddress());
		}
		return modelbox;
	}

	public ListBoxModel fillFormat() {
		ListBoxModel modelbox = new ListBoxModel();
		modelbox.add(CUCUMBER);
		modelbox.add(TM4J_OUTPUT_RESULT_FOR_JUNIT);
		return modelbox;
	}

	public FormValidation doCheckProjectKey(String projectKey) {
		return StringUtils.isBlank(projectKey) ? FormValidation.error(Tm4jConstants.PROJECT_KEY_IS_REQUIRED) : FormValidation.ok() ;
	}

	public FormValidation doCheckFilePath(String filePath) {
		return StringUtils.isBlank(filePath) ? FormValidation.error(Tm4jConstants.FILE_PATH_IS_REQUIRED) : FormValidation.ok() ;
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
}
