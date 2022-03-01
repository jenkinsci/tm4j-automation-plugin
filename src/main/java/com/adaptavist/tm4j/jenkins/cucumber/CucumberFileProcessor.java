package com.adaptavist.tm4j.jenkins.cucumber;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;

import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;

public class CucumberFileProcessor {

    private final PrintStream logger;
    private String tmpDirectory;

    public CucumberFileProcessor(PrintStream logger, String directory) {
        this.logger = logger;
        this.tmpDirectory = getTmpDirectory(directory);
    }

    public File filterCucumberFile(File file) {
        return filterCucumberFile(file, false);
    }

    private static String getTmpDirectory(String directory) {
        String fileSeparator = FileSystems.getDefault().getSeparator();
        String tmpDirectoryName = directory + (directory.endsWith(fileSeparator) ? "" : fileSeparator);
        return tmpDirectoryName + "target" + fileSeparator + "cucumber_tmp" + fileSeparator;
    }

    void setTmpDirectory(String directory){
        this.tmpDirectory = directory;
    }

    @VisibleForTesting
    File filterCucumberFile(File file, boolean withFormat) {
        File directory = new File(tmpDirectory);

        if (!directory.exists()) {
            if (!directory.mkdirs()) {
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
        } catch (IllegalStateException | IOException exception) {
            deleteTmpFileAfterError(newFile);
            throw new RuntimeException(String.format("Exception while parsing file: %s ", file.getName()), exception);
        }
    }

    private  void deleteTmpFileAfterError(File file) {
        if (file.exists()) {
            if (!file.delete()) {
                logger.printf("%s The generated file couldn't be deleted after error. Please check folder permissions " +
                        "and delete the file manually: %s %n", INFO, file.getAbsolutePath());
            }
        }
    }

    public void deleteTmpFilesAndFolder() {
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
            logger.printf("%s The generated directory couldn't be deleted. Please check folder permissions and " +
                    "delete the directory manually: %s %n", INFO, directoryFile.getAbsolutePath());
        }
    }

}
