package com.adaptavist.tm4j.jenkins.extensions.postbuildactions;

import static com.adaptavist.tm4j.jenkins.utils.Constants.ERROR;
import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;
import static com.adaptavist.tm4j.jenkins.utils.Constants.NAME_POST_BUILD_ACTION;

import java.io.PrintStream;
import java.util.List;

import javax.inject.Inject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import com.adaptavist.tm4j.jenkins.extensions.JiraInstance;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import com.adaptavist.tm4j.jenkins.http.Tm4jJiraRestClient;
import com.adaptavist.tm4j.jenkins.utils.Constants;
import com.adaptavist.tm4j.jenkins.utils.FormHelper;

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

public class TestResultPublisher extends Notifier {

	private PrintStream logger;
	private String serverAddress;
	private String projectKey;
	private String filePath;
	private String format;
	private Boolean autoCreateTestCases;

	@DataBoundConstructor
	public TestResultPublisher(String serverAddress, String projectKey, String filePath, Boolean autoCreateTestCases, String format) {
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
		List<JiraInstance> jiraInstances = getDescriptor().getJiraInstances();
		String workspace = build.getWorkspace().getRemote() + "/";
		try {
			Tm4jJiraRestClient tm4jJiraRestClient = new Tm4jJiraRestClient(jiraInstances, this.serverAddress);
			if (Constants.CUCUMBER.equals(this.format)) {
				tm4jJiraRestClient.uploadCucumberFile(workspace, this.filePath, this.projectKey, this.autoCreateTestCases, logger);
			} else {
				tm4jJiraRestClient.uploadCustomFormatFile(workspace, Constants.CUSTOM_FORMAT_FILE_NAME, this.projectKey, this.autoCreateTestCases, logger);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.printf(
					"%s There was an error trying to publish test results to Test Management for Jira. Error details: %n",
					ERROR);
			logger.printf(ERROR);
			logger.printf(" %s  %n", e.getMessage());
			logger.printf("%s Tests results have not been sent to Test Management for Jira %n", ERROR);
			return false;
		}
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
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

	public String getFilePath() {
		return this.filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Boolean getAutoCreateTestCases() {
		return autoCreateTestCases;
	}

	public void setAutoCreateTestCases(Boolean autoCreateTestCases) {
		this.autoCreateTestCases = autoCreateTestCases;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Inject
		private Tm4jGlobalConfiguration tm4jGlobalConfiguration;

		public DescriptorImpl() {
			super(TestResultPublisher.class);
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
			return new FormHelper().fillServerAddressItems(getJiraInstances());
		}

		public ListBoxModel doFillFormatItems() {
			return new FormHelper().fillFormat();
		}

		@POST
		public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
			return new FormHelper().doCheckProjectKey(projectKey);
		}

		@POST
		public FormValidation doCheckFilePath(@QueryParameter String filePath) {
			return new FormHelper().doCheckFilePath(filePath);
		}

		public List<JiraInstance> getJiraInstances() {
			return tm4jGlobalConfiguration.getJiraInstances();
		}
	}
}
