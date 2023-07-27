package com.adaptavist.tm4j.jenkins.utils;

import com.adaptavist.tm4j.jenkins.exception.PrintingZipFileContentException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipHandler {
    private ZipHandler(){
        //no need of instance
    }
    public static String getContentFromFilesInZip(File file) {
        if (file.exists()) {
            try (ZipFile zipFile = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                StringBuilder builder = new StringBuilder();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    builder.append(String.format("file with name: '%s' and content:%n", entry.getName()));
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        builder.append("[start content]%n");
                        builder.append(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        builder.append("not possible to read the content");
                    }
                    builder.append("%n[end content]%n");
                }
                return builder.toString();
            } catch (Exception e) {
                throw new PrintingZipFileContentException(String.format("was not possible to read the content for file %s", file.getAbsolutePath(), e));
            }
        } else {
            throw new PrintingZipFileContentException(String.format("the temporal zip file:%s does not exist", file.getAbsolutePath()));
        }
    }
}
