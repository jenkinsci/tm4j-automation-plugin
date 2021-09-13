package com.adaptavist.tm4j.jenkins.cucumber;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CucumberReportParserTest {

    public final static String TMP_FOLDER = "tmp/";

    @Test
    public void shouldFilterFile() throws Exception {

        File oldFile = new File("src/test/resources/outputFiles/result_1.json");
        File newFile = CucumberFileUtil.filterCucumberFiles(oldFile, TMP_FOLDER, true);

        String data = FileUtils.readFileToString(newFile, "UTF-8");
        String data2 = FileUtils.readFileToString(new File("src/test/resources/cucumber/result_1_filtered.json"), "UTF-8");
        assertTrue(newFile.exists());
        assertEquals(data, data2);
        newFile.delete();
    }

    @Test
    public void shouldFilterBigFile() throws Exception {

        File oldFile = new File("src/test/resources/cucumber/big_result.json");
        File newFile = CucumberFileUtil.filterCucumberFiles(oldFile, TMP_FOLDER, true);

        String data = FileUtils.readFileToString(newFile, "UTF-8");
        String data2 = FileUtils.readFileToString(new File("src/test/resources/cucumber/big_result_filtered.json"), "UTF-8");
        assertTrue(newFile.exists());
        assertEquals(data, data2);
        newFile.delete();

    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWithWrongFormat() throws Exception {
        File oldFile = new File("src/test/resources/cucumber/wrong_result.json");
        CucumberFileUtil.filterCucumberFiles(oldFile, TMP_FOLDER, true);
    }
}
