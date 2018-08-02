package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import hudson.FilePath;

public class Tm4jPlugin {

	private static final String CUSTOM_FORMAT_FILE_NAME = "tm4j_result.json";

	public boolean uploadCucumberFile(List<Tm4JInstance> jiraInstances, FilePath workspace, String filePath, String serverAddress, String projectKey, Boolean autoCreateTestCases) {
		String[] patterns = new String []{workspace + "/" + filePath};
		File file = new FileReader().getZip(patterns);
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().sendCucumberFiles(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), file, autoCreateTestCases);
		file.delete();
		return true;
	}

	public void uploadCustomFormatFile(List<Tm4JInstance> jiraInstances, FilePath workspace, String serverAddress, String projectKey, Boolean autoCreateTestCases) throws IOException {
		String tm4jResultFilePath = workspace + "/" + CUSTOM_FORMAT_FILE_NAME;

		if (customFormatFileExists(tm4jResultFilePath)) {
			File file = new FileReader().getZip(tm4jResultFilePath);
			Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
			new RestClient().sendCustomFormatFiles(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), file, autoCreateTestCases);
			file.delete();
		}
	}

	private boolean customFormatFileExists(String tm4jResultFilePath) {
		File file = new File(tm4jResultFilePath);
		return file.exists() && file.isFile();
	}

	private Tm4JInstance getTm4jInstance(List<Tm4JInstance> jiraInstances, String serverAddress ) {
		for (Tm4JInstance jiraInstance : jiraInstances) {
			if (StringUtils.isNotBlank(jiraInstance.getServerAddress()) && jiraInstance.getServerAddress().trim().equals(serverAddress)) {
				return jiraInstance;
			}
		}
		return null;
	}
}
