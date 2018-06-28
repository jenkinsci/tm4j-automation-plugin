package com.adaptavist.tm4j.jenkins.model;

import java.util.List;

import com.adaptavist.tm4j.jenkins.utils.rest.RestClient;

public class Tm4jConfigModel {

	private List<TestCaseResultModel> testCases;
	private Long tm4JProjectId;
	private Long versionId;
	private long testIssueTypeId;
	private RestClient restClient;


	public long getTestIssueTypeId() {
		return testIssueTypeId;
	}
	public void setTestIssueTypeId(long testIssueTypeId) {
		this.testIssueTypeId = testIssueTypeId;
	}
	public List<TestCaseResultModel> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<TestCaseResultModel> testCases) {
		this.testCases = testCases;
	}
	public Long getTm4JProjectId() {
		return tm4JProjectId;
	}
	public void setTm4JProjectId(Long tm4JProjectId) {
		this.tm4JProjectId = tm4JProjectId;
	}
	public RestClient getRestClient() {
		return restClient;
	}
	public void setRestClient(RestClient restClient) {
		this.restClient = restClient;
	}
	public Long getVersionId() {
		return versionId;
	}
	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}
}