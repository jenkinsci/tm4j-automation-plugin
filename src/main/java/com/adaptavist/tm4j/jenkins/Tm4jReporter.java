package com.adaptavist.tm4j.jenkins;

import java.io.PrintStream;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.FilePath;
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
	private Boolean autoCreateTestCases;

    @DataBoundConstructor
    public Tm4jReporter(String serverAddress, String projectKey, String filePath, Boolean autoCreateTestCases) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.filePath = filePath;
        this.autoCreateTestCases = autoCreateTestCases;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        logger = listener.getLogger();
        logger.printf("%s Examining test results...%n", pInfo);
        logger.printf(String.format("Build result is %s%n", build.getResult().toString()));
        Tm4jPlugin plugin = new Tm4jPlugin();
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
		FilePath workspace = build.getWorkspace();
		if (!plugin.uploadTestResultsFiles(jiraInstances, workspace, this.filePath, this.serverAddress, this.projectKey, this.autoCreateTestCases)) {
            logger.printf("%s Error.%n", pInfo);
            logger.printf("%s Cucumber files not found .%n", pInfo);
        	return false;
        };
    	logger.printf("%s Done.%n", pInfo);
    	return true;
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

