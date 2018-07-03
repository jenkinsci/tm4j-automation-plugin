package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.adaptavist.tm4j.jenkins.RestClient;

public class RestClientTest {

	private static final String PROJECT_KEY = "JP";
	private static final String SERVER_ADDRESS = "https://avst-test1358.adaptavist.cloud";
	private String username = "admin";
	private String password = "Str0ngp4ss_$";
	private List<File> files = new ArrayList<File>();
	
	@Test
	@Ignore
	public void shouldLoadTm4JReporter() throws Exception {
		files.add(new File("src/test/resources/result.json"));
		RestClient restClient = new RestClient();
		int response = restClient.sendFiles(SERVER_ADDRESS, PROJECT_KEY, username , password, files);
		assertEquals(201, response);
	}

	@Test
	@Ignore
	public void shouldValidateCredentials() throws Exception {
		RestClient restClient = new RestClient();
		assertEquals(true, restClient.isValidCredentials(SERVER_ADDRESS, username, password));
	}
}
