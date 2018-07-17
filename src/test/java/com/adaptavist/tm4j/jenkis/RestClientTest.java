package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;

import com.adaptavist.tm4j.jenkins.FileReader;
import com.adaptavist.tm4j.jenkins.RestClient;

public class RestClientTest {

	private static final String RESULT_JSON = "src/test/resources/*.json";
	private static final String PROJECT_KEY = "JP";
	private static final String SERVER_ADDRESS = "http://localhost:2990/jira";
	private String username = "admin";
	private String password = "admin";

	@AfterClass
	public static void afterClass() throws IOException {
		FileUtils.forceDelete(new File("tm4j.zip"));
	}
	
	@Test
	public void shouldLoadTm4JReporter() throws Exception {
		RestClient restClient = new RestClient();
		List<File> files = new FileReader().getFiles(RESULT_JSON);
		int response = restClient.sendFiles(SERVER_ADDRESS, PROJECT_KEY, username , password, files);
		assertEquals(201, response);
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
		ZipFile zip = fileReader.getZip(new String[] { RESULT_JSON });
		int response = restClient.sendZip(SERVER_ADDRESS, PROJECT_KEY, username , password, zip);
		assertEquals(201, response);
	}
}
