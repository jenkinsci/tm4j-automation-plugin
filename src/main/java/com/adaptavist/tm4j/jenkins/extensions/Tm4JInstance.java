package com.adaptavist.tm4j.jenkins.extensions;

import org.kohsuke.stapler.DataBoundConstructor;

public class Tm4JInstance {

	private String serverAddress;
	private String username;
	private String password;

	public Tm4JInstance() {
	}

	@DataBoundConstructor
	public Tm4JInstance(String serverAddress, String username, String password) {
		this.serverAddress = serverAddress;
		this.username = username;
		this.password = password;
	}

	public String getServerAddress() {
		return serverAddress;
	}
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
