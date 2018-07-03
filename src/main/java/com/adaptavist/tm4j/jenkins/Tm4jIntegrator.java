package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import hudson.FilePath;

public class Tm4jIntegrator {

	public boolean perform(List<Tm4JInstance> jiraInstances, FilePath filePath, String workspace, String serverAddress, String projectKey) {
		List<File> files = this.getFiles(workspace + "/" + filePath);
		Tm4JInstance jiraInstance = getTm4jInstance(jiraInstances, serverAddress);
		String url = jiraInstance.getServerAddress() + "/rest/kanoahtests/1.0/ci/results/cucumber/" + projectKey + "/testruns";
		RestClient restClient = new RestClient();
		restClient.sendFiles(url, jiraInstance.getUsername(), jiraInstance.getPassword(), files);
		return true;
	}

	public List<File> getFiles(String pattern) {
		String[] splited = pattern.split("\\*.");
		File directory = new File(splited[0]);
		Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter("*." + splited[1]), null);
		return new ArrayList<File>(files);
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
