package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.adaptavist.tm4j.jenkins.Tm4jIntegrator;

public class FileReaderTest {

	private String pattern = "src/main/resources/*.jelly";
	
	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		Tm4jIntegrator readFiles = new Tm4jIntegrator();
		List<File> files = readFiles.getFiles(pattern);
		assertEquals(files.size(), 1);
	}
}
