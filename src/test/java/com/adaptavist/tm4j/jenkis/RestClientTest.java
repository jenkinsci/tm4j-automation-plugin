package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import com.adaptavist.tm4j.jenkins.io.FileReader;
import com.adaptavist.tm4j.jenkins.io.RestClient;

@Ignore
public class RestClientTest {

	private static final String RESULT_JSON = "src/test/resources/*.json";
	private static final String PROJECT_KEY = "JP";
	private static final String SERVER_ADDRESS = "http://localhost:2990/jira";
	private String username = "admin";
	private String password = "admin";

	@AfterClass
	public static void afterClass() throws IOException {
		FileUtils.forceDelete(new File("/tmp/tm4j.zip"));
	}
	
	@Test
	public void shouldValidateCredentials() throws Exception {
		RestClient restClient = new RestClient();
		assertEquals(true, restClient.isValidCredentials(SERVER_ADDRESS, username, password));
	}
	
	@Test
	public void shouldLoadTm4JReporterCompressed() throws Exception {
		FileReader fileReader = new FileReader();
		RestClient restClient = new RestClient();
		File zip = fileReader.getZip("", RESULT_JSON);
		int response = restClient.sendCucumberFiles(SERVER_ADDRESS, PROJECT_KEY, username , password, zip, false, null);
		assertEquals(201, response);
	}
}
