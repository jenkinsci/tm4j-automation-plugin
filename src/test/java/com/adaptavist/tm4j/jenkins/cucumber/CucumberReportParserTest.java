package com.adaptavist.tm4j.jenkins.cucumber;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CucumberReportParserTest {

    private CucumberFileProcessor cucumberFileProcessor;

    @Before
    public void setUp(){
        cucumberFileProcessor = new CucumberFileProcessor(System.out, "");
        cucumberFileProcessor.setTmpDirectory("tmp");
    }

    @Test
    public void shouldFilterFile() throws Exception {

        File oldFile = new File("src/test/resources/outputFiles/result_1.json");
        File newFile = cucumberFileProcessor.filterCucumberFile(oldFile, true);

        try {
            String data = FileUtils.readFileToString(newFile, "UTF-8");
            String data2 = FileUtils.readFileToString(new File("src/test/resources/cucumber/result_1_filtered.json"), "UTF-8");
            assertTrue(newFile.exists());
            assertEquals(data, data2);
        } finally {
            newFile.delete();
        }
    }

    @Test
    public void shouldFilterBigFile() throws Exception {

        File oldFile = new File("src/test/resources/cucumber/big_result.json");
        File newFile = cucumberFileProcessor.filterCucumberFile(oldFile, true);
        try {
            String data = FileUtils.readFileToString(newFile, "UTF-8");
            String data2 = FileUtils.readFileToString(new File("src/test/resources/cucumber/big_result_filtered.json"), "UTF-8");
            assertTrue(newFile.exists());
            assertEquals(data, data2);
        } finally {
            newFile.delete();
        }

    }

    @Test
    public void shouldFilterWithNullValuesFile() throws Exception {

        File oldFile = new File("src/test/resources/cucumber/result_with_Null_Values.json");
        File newFile = cucumberFileProcessor.filterCucumberFile(oldFile, true);

        try {
            String data = FileUtils.readFileToString(newFile, "UTF-8");
            String data2 = FileUtils.readFileToString(new File("src/test/resources/cucumber/result_with_Null_Values_filtered.json"), "UTF-8");
            assertTrue(newFile.exists());
            assertEquals(data, data2);
        } finally {
            newFile.delete();
        }
    }

    @Test(expected = java.lang.RuntimeException.class)
    public void shouldFailWithWrongFormat() {
        File oldFile = new File("src/test/resources/cucumber/wrong_result.json");
        cucumberFileProcessor.filterCucumberFile(oldFile, true);

    }
}
