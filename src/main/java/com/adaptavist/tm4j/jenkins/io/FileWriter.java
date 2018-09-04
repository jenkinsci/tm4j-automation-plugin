package com.adaptavist.tm4j.jenkins.io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

public class FileWriter {

	private InputStream zipFile;
	private List<String> fileNames = new ArrayList<>();

	public FileWriter(InputStream zipFile) {
		this.zipFile = zipFile;
	}

	public void extractFeatureFilesFromZipAndSave(String targetFeatureFilesPath) throws IOException {
		createFolderIfItDoesNotExist(targetFeatureFilesPath);
		cleanUpFolder(targetFeatureFilesPath);

		ZipInputStream zipInputStream = new ZipInputStream(zipFile);

		ZipEntry entry;
		while ((entry = zipInputStream.getNextEntry()) != null) {
			fileNames.add(entry.getName());
			saveFeatureFile(zipInputStream, targetFeatureFilesPath, entry);
			zipInputStream.closeEntry();
		}
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	private void createFolderIfItDoesNotExist(String targetFeatureFilesPath) throws IOException {
		Path path = Paths.get(targetFeatureFilesPath);
		if (Files.notExists(path)) {
			Files.createDirectories(path);
		}
	}

	private void cleanUpFolder(String targetFeatureFilesPath) throws IOException {
		FileUtils.cleanDirectory(Paths.get(targetFeatureFilesPath).toFile());
	}

	private void saveFeatureFile(InputStream zipInputStream, String targetFeatureFilesPath, ZipEntry zipEntry) throws IOException {
		byte[] buffer = new byte[2048];
		FileOutputStream fileOutputStream = new FileOutputStream(targetFeatureFilesPath + "/" + zipEntry.getName());
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, buffer.length);

		int length;

		while ((length = zipInputStream.read(buffer, 0, buffer.length)) >= 0) {
			bufferedOutputStream.write(buffer, 0, length);
		}

		bufferedOutputStream.close();
		fileOutputStream.close();
	}
}
