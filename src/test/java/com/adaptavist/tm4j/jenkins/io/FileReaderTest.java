package com.adaptavist.tm4j.jenkins.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class FileReaderTest {

	private static final String FILE_PATH = "src/test/resources/outputFiles/";
	private static final String ALL = "**/*.json";
	private static final String JSON_ONLY = "*.json";

	@Test
	public void shouldReadAllFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, ALL);
		assertEquals(files.size(), 8);
	}
	
	@Test
	public void shouldReadAllFilesFromAFolderPath() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, "**/*");
		assertEquals(files.size(), 8);
	}

	@Test
	public void shouldReadAllFilesFromAFolderPathWhitHyphen() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, "result*");
		assertEquals(files.size(), 2);
	}

	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, JSON_ONLY);
		assertEquals(files.size(), 2);
	}

	@Test
	public void shouldReadAFileFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(FILE_PATH, "result_1.json");
		assertEquals(files.size(), 1);
	}
	
	@Test
	public void shouldCreateAZipFromAPatterm() throws Exception {
		File file = new FileReader().getZip(FILE_PATH, JSON_ONLY);
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
			new FileReader().getZip("/abc/xyz", JSON_ONLY);
		} catch(Exception e) {
			assertEquals(e.getMessage(), "Path not found : /abc/xyz");
			throw e;
		}
	}
}
