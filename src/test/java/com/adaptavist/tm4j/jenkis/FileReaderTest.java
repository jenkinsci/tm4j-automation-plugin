package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.adaptavist.tm4j.jenkins.FileReader;

import hudson.FilePath;

public class FileReaderTest {

	private static final FilePath FILE_PATH = new FilePath(new File("src/test/resources/"));
	private String pattern = "*.json";

	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, pattern);
		assertEquals(files.size(), 2);
	}

	@Test
	public void shouldReadAFileFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, "result_1.json");
		assertEquals(files.size(), 1);
	}
	
	@Test
	public void shouldCreateAZipFromAPatterm() throws Exception {
		File file = new FileReader().getZip(FILE_PATH, pattern);
		assertTrue(file.exists());
	}

	@Test(expected = Exception.class)
	public void shouldThrowAnExceptionWhenFileNotFound() throws Exception {
		try {
			new FileReader().getZip(FILE_PATH, "abc.xyz");
		} catch(Exception e) {
			assertEquals(e.getMessage(), "File not found: abc.xyz");
			throw e;
		}
	}

	@Test(expected = Exception.class)
	public void shouldThrowAnExceptionWhenFileNotFoundForPattern() throws Exception {
		try {
			new FileReader().getZip(FILE_PATH, "*.xyz");
		} catch(Exception e) {
			assertEquals(e.getMessage(), "File not found : *.xyz");
			throw e;
		}
	}
	
	@Test(expected = Exception.class)
	public void shouldThrowAnExceptionWhenPathIsWrong() throws Exception {
		try {
			new FileReader().getZip(new FilePath(new File("/abc/xyz")), pattern);
		} catch(Exception e) {
			assertEquals(e.getMessage(), "Path not found : /abc/xyz");
			throw e;
		}
	}
}
