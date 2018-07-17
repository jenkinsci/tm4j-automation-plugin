package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;

import com.adaptavist.tm4j.jenkins.FileReader;

public class FileReaderTest {
	
	private String pattern = "src/main/resources/*.jelly";
	private String[] patterns = {pattern, "src/main/resources/com/adaptavist/tm4j/jenkins/Tm4jReporter/*.jelly"};

	@AfterClass
	public static void afterClass() throws IOException {
		FileUtils.forceDelete(new File("tm4j.zip"));
	}
	
	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(pattern);
		assertEquals(files.size(), 1);
	}
	
	@Test
	public void shouldCreateAZipFromAPatterm() throws Exception {
		ZipFile zip = new FileReader().getZip(patterns);
		assertEquals("tm4j.zip", zip.getName());
		ZipEntry entry = zip.getEntry("src/main/resources/index.jelly");
		assertEquals(entry.isDirectory(), false);
	}
}
