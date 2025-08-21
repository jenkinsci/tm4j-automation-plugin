package com.adaptavist.tm4j.jenkins.io;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FileReaderTest {

    private static final String FILE_PATH = "src/test/resources/outputFiles/";
    private static final String ALL = "**/*.json";
    private static final String JSON_ONLY = "*.json";

    @Test
    public void shouldReadAllFilesFromAFolder() throws Exception {
        final File zip = new FileReader().getZip(FILE_PATH, ALL);
        final List<String> files = getFileNames(zip);
        assertEquals(files.size(), 10);
    }

    @Test
    public void shouldReadAllFilesFromAFolderPath() throws Exception {
        final File zip = new FileReader().getZip(FILE_PATH, "**/*");
        final List<String> files = getFileNames(zip);
        assertEquals(files.size(), 10);
    }

    @Test
    public void shouldReadAllFilesFromAFolderPathWhitHyphen() throws Exception {
        final File zip = new FileReader().getZip(FILE_PATH, "result*");
        final List<String> files = getFileNames(zip);
        assertEquals(files.size(), 2);
    }

    @Test
    public void shouldReadFilesFromAFolder() throws Exception {
        final File zip = new FileReader().getZip(FILE_PATH, JSON_ONLY);
        List<String> files = getFileNames(zip);
        assertEquals(files.size(), 3);
    }

    @Test
    public void shouldReadAFileFromAFolder() throws Exception {
        final File zip = new FileReader().getZip(FILE_PATH, "result_1.json");
        final List<String> files = getFileNames(zip);
        assertEquals(files.size(), 1);
    }

    @Test
    public void shouldThrowAnExceptionWhenFileNotFound() throws Exception {
        final FileReader fileReader = new FileReader();
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> fileReader.getZip(FILE_PATH, "abc.xyz"));
        assertEquals("File not found: abc.xyz", exception.getMessage());
    }

    @Test
    public void shouldThrowAnExceptionWhenFileNotFoundForPattern() throws Exception {
        final FileReader fileReader = new FileReader();
        FileNotFoundException exception = assertThrows( FileNotFoundException.class, () -> fileReader.getZip(FILE_PATH, "*.xyz"));
        assertEquals("File not found: *.xyz", exception.getMessage());
    }

    @Test
    public void shouldThrowAnExceptionWhenPathIsWrong() throws Exception {
        final FileReader fileReader = new FileReader();
        Exception exception = assertThrows(Exception.class, () -> fileReader.getZip("/abc/xyz", JSON_ONLY));
        assertEquals(exception.getMessage(), "Path not found: /abc/xyz");
    }

    @Test
    public void shouldFindLegacyCustomFile() throws Exception {
        File zip = new FileReader().getZipForCustomFormat(FILE_PATH+ "legacy/");
        List<String> files = getFileNames(zip);
        assertEquals(1, files.size());
        assertEquals(FILE_PATH + "legacy/tm4j_result.json", files.get(0));
    }

    @Test
    public void shouldFindCustomFile() throws Exception {
        final File zip = new FileReader().getZipForCustomFormat(FILE_PATH);
        final List<String> files = getFileNames(zip);
        assertEquals(1, files.size());
        assertEquals(FILE_PATH + "zephyr_result.json", files.get(0));
    }

    @Test
    public void shouldThrowAnExceptionWhenCustomFileDoesNotExist() throws Exception {
        final FileReader fileReader = new FileReader();
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> fileReader.getZipForCustomFormat("not/found"));
        assertEquals("File not found: zephyr_result.json.", exception.getMessage());
    }

    private List<String> getFileNames(File zip) throws IOException {
        List<String> fileNames = new ArrayList<>();
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zip));
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            fileNames.add(entry.getName());
            zipInputStream.closeEntry();
        }
        return fileNames;
    }
}
