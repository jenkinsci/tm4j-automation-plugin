package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import hudson.FilePath;

public class Tm4jPlugin {

	public boolean uploadTestResultsFiles(List<Tm4JInstance> jiraInstances, FilePath workspace, String filePath, String serverAddress, String projectKey) {
		String[] patterns = new String []{workspace + "/" + filePath};
		File file = new FileReader().getZip(patterns);
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		new RestClient().sendZip(jiraInstance.getServerAddress(), projectKey, jiraInstance.getUsername(), jiraInstance.getPassword(), file);
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
