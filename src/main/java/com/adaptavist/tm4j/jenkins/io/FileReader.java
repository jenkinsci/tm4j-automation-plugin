package com.adaptavist.tm4j.jenkins.io;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.adaptavist.tm4j.jenkins.utils.Constants.CUSTOM_FORMAT_FILE;
import static com.adaptavist.tm4j.jenkins.utils.Constants.CUSTOM_FORMAT_FILE_LEGACY;

public class FileReader {

    public File getZip(String directory, String pattern) throws Exception {
        return createZip(findFiles(directory, pattern));
    }

    public File getZipForCustomFormat(String directory) throws Exception {
        return createZip(Collections.singletonList(getCustomFileFormat(directory)));
    }

    private List<File> findFiles(String directory, String pattern) throws Exception {
        if (!new File(directory).isDirectory()) {
            throw new Exception(MessageFormat.format("Path not found: {0}", directory));
        }
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{pattern});
        scanner.setBasedir(directory);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] paths = scanner.getIncludedFiles();
        List<File> files = new ArrayList<>();
        for (String path : paths) {
            File file = new File(directory + path);
            if (!file.exists()) {
                throw new FileNotFoundException(MessageFormat.format("File not found: {0}", file.getPath()));
            }
            files.add(file);
        }
        if (files.isEmpty()) {
            throw new FileNotFoundException(MessageFormat.format("File not found: {0}", pattern));
        }
        return files;
    }

    private File createZip(List<File> files) throws IOException {
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

    private File getCustomFileFormat(String directory) throws FileNotFoundException {
        final File file = new File(directory + CUSTOM_FORMAT_FILE);
        if (file.exists()){
            return file;
        }
        final File legacy = new File(directory + CUSTOM_FORMAT_FILE_LEGACY);
        if (!legacy.exists()) {
            throw new FileNotFoundException(MessageFormat.format("File not found: {0}.", CUSTOM_FORMAT_FILE));
        }
        return legacy;
    }
}
