package com.adaptavist.tm4j.jenkins.io;

import hudson.FilePath;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FileWriterTest {
    private static final String FILE_PATH = "src/test/resources/featureFiles/";
    private File targetDir;
    private File zipFile;
    private FileWriter fileWriter;
    private final File rootDir = new File("");
    private final FilePath workspace = new FilePath(new File(""));

    @Before
    public void setUp() throws Exception {
        zipFile = new File(FILE_PATH, "featureFiles.zip");
        targetDir = new File(FILE_PATH, "uncompressed");
        fileWriter = new FileWriter(new FileInputStream(zipFile));
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(targetDir);
    }

    @Test
    public void shouldCreateZipFile() throws IOException, InterruptedException {
        fileWriter.extractFeatureFilesFromZipAndSave(rootDir, workspace, targetDir.getPath());

        List<String> fileNames = fileWriter.getFileNames();
        assertEquals(2, fileNames.size());
        assertTrue(fileNames.containsAll(Arrays.asList("BA-T1.feature", "BA-T2.feature")));
    }

    @Test
    public void shouldCleanUpFolderBeforeExtractingZip() throws IOException, InterruptedException {
        Files.createDirectory(targetDir.toPath());
        Files.createFile(Paths.get(targetDir.getPath(), "someFile"));
        Files.createDirectory(Paths.get(targetDir.getPath(), "subdir"));

        fileWriter.extractFeatureFilesFromZipAndSave(rootDir, workspace, targetDir.getPath());
        String[] extractedFiles = targetDir.list();
        assertEquals(2, extractedFiles.length);
        assertTrue(Arrays.asList(extractedFiles).containsAll(Arrays.asList("BA-T1.feature", "BA-T2.feature")));
    }
}