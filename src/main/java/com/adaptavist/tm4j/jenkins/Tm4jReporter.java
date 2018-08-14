package com.adaptavist.tm4j.jenkins;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.NAME_POST_BUILD_ACTION;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.FilePath;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Tm4jReporter extends Notifier {

    public static PrintStream logger;
    private static final String PLUGIN_NAME = new String("[Test Management for Jira]");
    private static String INFO = String.format("%s [INFO]", PLUGIN_NAME);
    private static String ERROR = String.format("%s [ERROR]", PLUGIN_NAME);
    private String serverAddress;
    private String projectKey;
	private String filePath;
	private String format;
	private Boolean autoCreateTestCases;

    @DataBoundConstructor
    public Tm4jReporter(String serverAddress, String projectKey, String filePath, Boolean autoCreateTestCases, String format) {
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
        logger.printf("%s Examining test results...%n", INFO);
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
		String workspace = build.getWorkspace().getRemote() + "/";
        try {
        	if (Tm4jConstants.CUCUMBER.equals(this.format)) {
        		new Tm4jPlugin().uploadCucumberFile(jiraInstances, workspace, this.filePath, this.serverAddress, this.projectKey, this.autoCreateTestCases);
        	} else {
        		new Tm4jPlugin().uploadCustomFormatFile(jiraInstances, workspace, Tm4jConstants.CUSTOM_FORMAT_FILE_NAME, this.serverAddress, this.projectKey, this.autoCreateTestCases);
        	}
        } catch (Exception e) {
        	logger.printf("%s There was an error trying to send the test results to Test Management for Jira. Error details: %n", ERROR);
            logger.printf(ERROR);
            logger.printf(" %s  %n", e.getMessage());
        	logger.printf("%s Tests results didn't send to TM4J %n", ERROR);
            return false;
        }
    	logger.printf("%s  Test results sent to Test Management for Jira successfully.%n", INFO);
    	return true;
    }

    @Override
    public Tm4jDescriptor getDescriptor() {
        return (Tm4jDescriptor) super.getDescriptor();
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
	public static final class Tm4jDescriptor extends BuildStepDescriptor<Publisher> {
	
		private List<Tm4JInstance> jiraInstances;
	
		public Tm4jDescriptor() {
			super(Tm4jReporter.class);
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
		public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
			request.bindParameters(this);
			Object formJiraInstances = formData.get("jiraInstances");
			this.jiraInstances = crateJiraInstances(formJiraInstances);
			save();
			return super.configure(request, formData);
		}

		private List<Tm4JInstance> crateJiraInstances(Object formJiraInstances) {
			if (formJiraInstances == null) {
				return null;
			}
			List<Tm4JInstance> newJiraInstances = new ArrayList<>();
			if (formJiraInstances instanceof JSONArray) {
				JSONArray jiraInstancesList = (JSONArray) formJiraInstances;
				for (Object jiraInstance :  jiraInstancesList.toArray()) {
					newJiraInstances.add(createAnInstance((JSONObject) jiraInstance));
				}
			} else {
				newJiraInstances.add(createAnInstance((JSONObject) formJiraInstances));
			}
			return newJiraInstances;
		}
	
		private Tm4JInstance createAnInstance(JSONObject formJiraInstance) {
			Tm4JInstance tm4jInstance = new Tm4JInstance();
			String serverAddres = formJiraInstance.getString("serverAddress");
			String username = formJiraInstance.getString("username");
			String password = formJiraInstance.getString("password");
			if (StringUtils.isBlank(serverAddres) || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
				return null;
			}
			tm4jInstance.setServerAddress(StringUtils.removeEnd(serverAddres.trim(), "/"));
			tm4jInstance.setUsername(username.trim());
			tm4jInstance.setPassword(password.trim());
			RestClient restClient = new RestClient();
			if (restClient.isValidCredentials(tm4jInstance.getServerAddress(), tm4jInstance.getUsername(), tm4jInstance.getPassword())) {
				return tm4jInstance;
			}
			return null;
		}
	
		@Override
		public String getDisplayName() {
			return NAME_POST_BUILD_ACTION;
		}
		
		public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String username, @QueryParameter String password) {
			return new Tm4jForm().testConnection(serverAddress, username, password);
		}
		
		public ListBoxModel doFillServerAddressItems() {
			return new Tm4jForm().fillServerAddressItens(this.jiraInstances);
		}
		
		public ListBoxModel doFillFormatItems() {
			return new Tm4jForm().fillFormat();
		}
		
		public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
			return new Tm4jForm().doCheckProjectKey(projectKey);
		}

		public FormValidation doCheckFilePath(@QueryParameter String filePath) {
			return new Tm4jForm().doCheckFilePath(filePath);
		}
		
		public List<Tm4JInstance> getJiraInstances() {
			return jiraInstances;
		}
	
		public void setJiraInstances(List<Tm4JInstance> jiraInstances) {
			this.jiraInstances = jiraInstances;
		}
	}
}

