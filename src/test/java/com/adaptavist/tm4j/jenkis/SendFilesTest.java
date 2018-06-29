package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.adaptavist.tm4j.jenkins.SendFiles;

public class SendFilesTest {

	String URL = "http://localhost:2990/jira/rest/kanoahtests/1.0/ci/results/cucumber/DEF/testruns";
	private String username = "admin";
	private String password = "admin";
	private List<File> files = new ArrayList<File>();
	
	@Test
	public void shouldLoadTm4JReporter() throws Exception {
		files.add(new File("/home/chico/dev/source/adaptavist/tm4j-automation-tests/target/cucumber/atmServerCloneTestCase_result.json"));
		SendFiles sf = new SendFiles();
		int response = sf.sendFiles(URL, username , password, files);
		assertEquals(201, response);
	}

}
