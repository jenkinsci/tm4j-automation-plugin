package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class FileReader {
	public List<File> getFiles(String pattern) {
		String[] splited = pattern.split("\\*.");
		File directory = new File(splited[0]);
		Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter("*." + splited[1]), null);
		return new ArrayList<File>(files);
	}

	public File getZip(String[] patterns) {
		List<File> files = new ArrayList<File>();
		for (String pattern : patterns) {
			files.addAll(getFiles(pattern));
		}
		try {
			File zip = File.createTempFile("tm4j", "zip");
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
			for (File file : files) {
				out.putNextEntry(new ZipEntry(file.getPath()));
				out.write(FileUtils.readFileToByteArray(file));
				out.closeEntry();
			}
			out.close();
			return zip;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public File getZip(String filePath) throws IOException {
		File zip = File.createTempFile("tm4j_junit_results", "zip");

		File file = new File(filePath);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
		out.putNextEntry(new ZipEntry(file.getPath()));
		out.write(FileUtils.readFileToByteArray(file));
		out.closeEntry();
		out.close();

		return zip;
	}
}
