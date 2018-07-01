package com.adaptavist.tm4j.jenkins;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;

public class Tm4jReporter extends Notifier {

    public static PrintStream logger;
    private static final String PluginName = new String("[TM4JTestResultReporter]");
    private final String pInfo = String.format("%s [INFO]", PluginName);
    private String serverAddress;
    private String projectKey;
	private String filePath;

    @DataBoundConstructor
    public Tm4jReporter(String serverAddress, String projectKey, String filePath ) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.filePath = filePath;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build,
                           final Launcher launcher,
                           final BuildListener listener) {
        logger = listener.getLogger();
        logger.printf("%s Examining test results...%n", pInfo);
        logger.printf(String.format("Build result is %s%n", build.getResult().toString()));
        
        Tm4JInstance jiraInstance = this.getTm4jInstance();
    	ReadFiles read = new ReadFiles();
    	List<File> files = read.getFiles(build.getWorkspace() + "/" + this.filePath);

    	SendFiles tm4jSendFile = new SendFiles();
    	String url = jiraInstance.getServerAddress() + "/rest/kanoahtests/1.0/ci/results/cucumber/" + this.projectKey + "/testruns";
		tm4jSendFile.sendFiles(url, jiraInstance.getUsername(), jiraInstance.getPassword(), files);
        logger.printf("%s Done.%n", pInfo);
        return true;
    }

    private Tm4JInstance getTm4jInstance() {
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
        for (Tm4JInstance jiraInstance : jiraInstances) {
            if (StringUtils.isNotBlank(jiraInstance.getServerAddress()) && jiraInstance.getServerAddress().trim().equals(serverAddress)) {
                return jiraInstance;
            }
        }
		return null;
    }

    @Override
    public Tm4jDescriptor getDescriptor() {
        return (Tm4jDescriptor) super.getDescriptor();
    }
    public String getServerAddress() {
        return serverAddress;
    }
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    public String getProjectKey() {
        return projectKey;
    }
    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
    public String getFilePath() {return  this.filePath;}
    public void setFilePath ( String filePath) {this.filePath = filePath;}
}

