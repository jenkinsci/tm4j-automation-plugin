package com.adaptavist.tm4j.jenkins.io;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileReader {

    public List<File> getFiles(String workpace, String pattern) throws Exception {
        if (!new File(workpace).isDirectory()) {
            throw new Exception(MessageFormat.format("Path not found : {0}", workpace));
        }
        if (!pattern.contains("*")) {
            File file = new File(workpace + pattern);
            if (!file.exists()) {
                throw new FileNotFoundException(MessageFormat.format("File not found: {0}", pattern));
            }
            return Arrays.asList(file);
        }
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{pattern});
        scanner.setBasedir(workpace);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] paths = scanner.getIncludedFiles();
        List<File> files = new ArrayList<>();
        for (String path : paths) {
            File file = new File(workpace + path);
            if (!file.exists()) {
                throw new FileNotFoundException(MessageFormat.format("File not found : {0}", file.getPath()));
            }
            files.add(file);
        }
        if (files.isEmpty()) {
            throw new FileNotFoundException(MessageFormat.format("File not found : {0}", pattern));
        }
        return files;
    }

    public File getZip(String workspace, String pattern) throws Exception {
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
