package com.adaptavist.tm4j.jenkins.cucumber;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class CucumberFileUtil {

    public static File filterCucumberFiles(File file, String directory, final PrintStream logger) {
        String tmpDirectoryName = getTmpDirectory(directory);
        return filterCucumberFiles(file, tmpDirectoryName, false, logger);
    }

    private static String getTmpDirectory(String directory) {
        String fileSeparator = FileSystems.getDefault().getSeparator();
        String tmpDirectoryName = directory + (directory.endsWith(fileSeparator) ? "" : fileSeparator);
        return tmpDirectoryName + "target/cucumber_tmp/";
    }

    public static File filterCucumberFiles(File file, String tmpDirectory, boolean withFormat, final PrintStream logger) {
        File directory = new File(tmpDirectory);

        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new RuntimeException(String.format("The directory '%s' couldn't be created. Please check " +
                        "folder permissions and try again", tmpDirectory));
            }
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
            deleteTmpFile(newFile, logger);
            throw new RuntimeException(String.format("Exception while parsing file: %s ", file.getName()), exception);
        } catch (IllegalStateException exception) {
            deleteTmpFile(newFile, logger);
            throw exception;
        }
    }

    private static void deleteTmpFile(File file, final PrintStream logger) {
        if (file.exists()) {
            if (!file.delete()) {
                logger.printf("%s The generated file couldn't be deleted. Please check folder permissions " +
                        "and delete the file manually: %s %n", INFO, file.getAbsolutePath());
            }
        }
    }

    public static void deleteTmpFiles(String directory, final PrintStream logger) {
        String tmpDirectory = getTmpDirectory(directory);
        File directoryFile = new File(tmpDirectory);
        File[] allContents = directoryFile.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (!file.delete()) {
                    logger.printf("%s The generated file couldn't be deleted. Please check folder permissions " +
                            "and delete the file manually: %s %n", INFO, file.getAbsolutePath());
                }
            }
        }
        if (!directoryFile.delete()) {
            logger.printf("%s The generated  file couldn't be deleted. Please check folder permissions and " +
                    "delete the directory manually: %s  %n", INFO, directoryFile.getAbsolutePath());
        }
    }

}
