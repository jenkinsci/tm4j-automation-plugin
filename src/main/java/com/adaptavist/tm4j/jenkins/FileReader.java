package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;


public class FileReader {

	public List<File> getFiles(String pattern) {
		String[] splited = pattern.split("\\*.");
		File directory = new File(splited[0]);
	    Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter( "*." +  splited[1]), null);
	    return new ArrayList<File>(files);
	}
}
