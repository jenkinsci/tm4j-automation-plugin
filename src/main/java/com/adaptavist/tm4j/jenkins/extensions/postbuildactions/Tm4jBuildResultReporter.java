package com.adaptavist.tm4j.jenkins.extensions.postbuildactions;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.ERROR;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.INFO;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.NAME_POST_BUILD_ACTION;

import java.io.PrintStream;
import java.util.List;

import com.adaptavist.tm4j.jenkins.extensions.Tm4JInstance;
import com.adaptavist.tm4j.jenkins.Tm4jConstants;
import com.adaptavist.tm4j.jenkins.extensions.Tm4jFormHelper;
import com.adaptavist.tm4j.jenkins.io.Tm4jJiraRestClient;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import javax.inject.Inject;

public class Tm4jBuildResultReporter extends Notifier {

	public static PrintStream logger;

    private String serverAddress;
    private String projectKey;
	private String filePath;
	private String format;
	private Boolean autoCreateTestCases;

    @DataBoundConstructor
    public Tm4jBuildResultReporter(String serverAddress, String projectKey, String filePath, Boolean autoCreateTestCases, String format) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.filePath = filePath;
        this.autoCreateTestCases = autoCreateTestCases;
        this.format = format;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        logger = listener.getLogger();
        logger.printf("%s Publishing test results...%n", INFO);
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
		String workspace = build.getWorkspace().getRemote() + "/";
        try {
        	if (Tm4jConstants.CUCUMBER.equals(this.format)) {
        		new Tm4jJiraRestClient().uploadCucumberFile(jiraInstances, workspace, this.filePath, this.serverAddress, this.projectKey, this.autoCreateTestCases, logger);
        	} else {
        		new Tm4jJiraRestClient().uploadCustomFormatFile(jiraInstances, workspace, Tm4jConstants.CUSTOM_FORMAT_FILE_NAME, this.serverAddress, this.projectKey, this.autoCreateTestCases, logger);
        	}
        } catch (Exception e) {
        	logger.printf("%s There was an error trying to send the test results to Test Management for Jira. Error details: %n", ERROR);
            logger.printf(ERROR);
            logger.printf(" %s  %n", e.getMessage());
        	logger.printf("%s Tests results didn't send to TM4J %n", ERROR);
            return false;
        }
    	return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    
    public String getServerAddress() {return serverAddress;}
    public void setServerAddress(String serverAddress) {this.serverAddress = serverAddress;}
    public String getProjectKey() {return projectKey;}
    public void setProjectKey(String projectKey) {this.projectKey = projectKey;}
    public String getFilePath() {return  this.filePath;}
    public void setFilePath ( String filePath) {this.filePath = filePath;}
	public String getFormat() {return format;}
	public void setFormat(String format) {this.format = format;}
	public Boolean getAutoCreateTestCases() {return autoCreateTestCases;}
	public void setAutoCreateTestCases(Boolean autoCreateTestCases) {this.autoCreateTestCases = autoCreateTestCases;}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
	
		@Inject
		private Tm4jGlobalConfiguration tm4jGlobalConfiguration;
	
		public DescriptorImpl() {
			super(Tm4jBuildResultReporter.class);
			load();
		}
	
		@Override
		public Publisher newInstance(StaplerRequest reqquest, JSONObject formData) throws FormException {
			return super.newInstance(reqquest, formData);
		}
	
		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}
	
		@Override
		public String getDisplayName() {
			return NAME_POST_BUILD_ACTION;
		}
		
		public ListBoxModel doFillServerAddressItems() {
			return new Tm4jFormHelper().fillServerAddressItens(getJiraInstances());
		}

		public ListBoxModel doFillFormatItems() {
			return new Tm4jFormHelper().fillFormat();
		}

		public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
			return new Tm4jFormHelper().doCheckProjectKey(projectKey);
		}

		public FormValidation doCheckFilePath(@QueryParameter String filePath) {
			return new Tm4jFormHelper().doCheckFilePath(filePath);
		}

		public List<Tm4JInstance> getJiraInstances() {
			return tm4jGlobalConfiguration.getJiraInstances();
		}
	}
}

