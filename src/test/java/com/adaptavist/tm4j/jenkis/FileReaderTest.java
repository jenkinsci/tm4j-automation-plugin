package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.adaptavist.tm4j.jenkins.FileReader;

public class FileReaderTest {

	private String pattern = "src/main/resources/*.jelly";
	
	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(pattern);
		assertEquals(files.size(), 1);
	}
}
