package com.adaptavist.tm4j.jenkins.io;

import java.io.File;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.List;

import com.adaptavist.tm4j.jenkins.Tm4jConstants;
import com.adaptavist.tm4j.jenkins.extensions.Tm4JInstance;
import org.apache.commons.lang.StringUtils;

public class Tm4jJiraRestClient {


	public void uploadCucumberFile(List<Tm4JInstance> jiraInstances, String workspace, String filePath, String serverAddress, String projectKey, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		File file = new FileReader().getZip(workspace, filePath);
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().sendCucumberFiles(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), file, autoCreateTestCases, logger);
		file.delete();
	}

	public void uploadCustomFormatFile(List<Tm4JInstance> jiraInstances, String workspace, String filePath, String serverAddress, String projectKey, Boolean autoCreateTestCases, PrintStream logger) throws Exception {
		File file = new FileReader().getZip(workspace, filePath);
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().sendCustomFormatFiles(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), file, autoCreateTestCases, logger);
		file.delete();
	}

	public void exportFeatureFiles(List<Tm4JInstance> jiraInstances, String featureFilesPath, String serverAddress, String tql) throws Exception {
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().downloadFeatureFiles(jiraInstance.getServerAddress(), featureFilesPath, jiraInstance.getUsername(), jiraInstance.getPassword(), tql);
	}

	private Tm4JInstance getTm4jInstance(List<Tm4JInstance> jiraInstances, String serverAddress ) throws Exception {
		if (jiraInstances == null)
			throw new IllegalStateException(Tm4jConstants.THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED);
		for (Tm4JInstance jiraInstance : jiraInstances) {
			if (StringUtils.isNotBlank(jiraInstance.getServerAddress()) && jiraInstance.getServerAddress().trim().equals(serverAddress)) {
				return jiraInstance;
			}
		}
		throw new Exception(MessageFormat.format(Tm4jConstants.JIRA_INSTANCE_NOT_FOUND, serverAddress));
	}
}
