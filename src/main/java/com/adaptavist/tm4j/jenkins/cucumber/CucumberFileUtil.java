package com.adaptavist.tm4j.jenkins.cucumber;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

public class CucumberFileUtil {

    public static File filterCucumberFiles(File file, String directory) {
        String tmpDirectoryName = getTmpDirectory(directory);
        return filterCucumberFiles(file, tmpDirectoryName, false);
    }

    private static String getTmpDirectory(String directory) {
        String fileSeparator = FileSystems.getDefault().getSeparator();
        String tmpDirectoryName = directory + (directory.endsWith(fileSeparator) ? "" : fileSeparator);
        return tmpDirectoryName + "target/cucumber_tmp/";
    }

    public static File filterCucumberFiles(File file, String tmpDirectory, boolean withFormat) {
        File directory = new File(tmpDirectory);

        if (!directory.exists()) {
            directory.mkdir();
        }

        File newFile = new File(tmpDirectory + file.getName());

        try {
            InputStream in = new FileInputStream(file);
            try (JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                OutputStream out = new FileOutputStream(newFile);
                try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
                    if (withFormat) {
                        writer.setIndent("  ");
                    }
                    final CucumberReportParser parser = new CucumberReportParser(reader, writer);
                    parser.parseFeatures();
                }
            }
            return newFile;
        } catch (IOException exception) {
            throw new RuntimeException("Exception while parsing file " + file.getName(), exception);
        }
    }

    public static boolean deleteTmpFiles(String directory) {
        String tmpDirectory = getTmpDirectory(directory);
        File directoryFile = new File(tmpDirectory);
        File[] allContents = directoryFile.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        return directoryFile.delete();
    }

}
