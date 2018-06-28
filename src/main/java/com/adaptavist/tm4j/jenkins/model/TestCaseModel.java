package com.adaptavist.tm4j.jenkins.model;

public class TestCaseModel {

	private String name;
	private long testCaseId;
	private long phaseTestCaseId;
	private long remoteRepositoryId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(long testCaseId) {
		this.testCaseId = testCaseId;
	}

	public long getPhaseTestCaseId() {
		return phaseTestCaseId;
	}

	public void setPhaseTestCaseId(long phaseTestCaseId) {
		this.phaseTestCaseId = phaseTestCaseId;
	}

	public long getRemoteRepositoryId() {
		return remoteRepositoryId;
	}

	public void setRemoteRepositoryId(long remoteRepositoryId) {
		this.remoteRepositoryId = remoteRepositoryId;
	}

}