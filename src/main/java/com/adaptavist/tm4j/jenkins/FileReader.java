package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import hudson.FilePath;

public class FileReader {

	public List<File> getFiles(FilePath workpace, String pattern) throws Exception {
		if (!pattern.contains("*.")) {
			File file = new File(workpace + "/"+ pattern);
			if (!file.exists()) {
				throw new Exception(MessageFormat.format("File not found: {0}", pattern));
			}
			return Arrays.asList(file);
		}
		String[] splited = pattern.split("\\*.");
		File directory = new File( workpace + "/" + splited[0]);
		if (!directory.isDirectory()) {
			throw new Exception(MessageFormat.format("Path not found : {0}", splited[0]));
		}
		Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter("*." + splited[1]), null);
		if (files.isEmpty()) {
			throw new Exception(MessageFormat.format("File not found : {0}", pattern));
		}
		return new ArrayList<File>(files);
	}

	public File getZip(FilePath workspace, String pattern) throws Exception {
		List<File> files = getFiles(workspace, pattern);
		File zip = File.createTempFile("tm4j", "zip");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		for (File file : files) {
			out.putNextEntry(new ZipEntry(file.getPath()));
			out.write(FileUtils.readFileToByteArray(file));
			out.closeEntry();
		}
		out.close();
		return zip;
	}
}
