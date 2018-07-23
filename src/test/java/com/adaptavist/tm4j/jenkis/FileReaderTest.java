package com.adaptavist.tm4j.jenkis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Test;

import com.adaptavist.tm4j.jenkins.FileReader;

public class FileReaderTest {
	
	private String pattern = "src/main/resources/*.jelly";
	private String[] patterns = {pattern, "src/main/resources/com/adaptavist/tm4j/jenkins/Tm4jReporter/*.jelly"};

	@Test
	public void shouldReadFilesFromAFolder() throws Exception {
		List<File> files = new FileReader().getFiles(pattern);
		assertEquals(files.size(), 1);
	}
	
	@SuppressWarnings("resource")
	@Test
	public void shouldCreateAZipFromAPatterm() throws Exception {
		File file = new FileReader().getZip(patterns);
		assertTrue(file.exists());
		ZipFile zip = new ZipFile(file);
		ZipEntry entry = zip.getEntry("src/main/resources/index.jelly");
		assertEquals(entry.isDirectory(), false);
		file.delete();
		assertFalse(file.exists());
	}
}
