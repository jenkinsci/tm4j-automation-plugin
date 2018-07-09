package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import hudson.FilePath;

public class Tm4jPlugin {

	public boolean uploadTestResultsFiles(List<Tm4JInstance> jiraInstances, FilePath workspace, String filePath, String serverAddress, String projectKey) {
		List<File> files = new FileReader().getFiles(workspace + "/" + filePath);
		if (files.isEmpty()) {
			return false;
		}
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().sendFiles(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), files);
		return true;
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
