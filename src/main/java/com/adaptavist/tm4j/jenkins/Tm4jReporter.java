package com.adaptavist.tm4j.jenkins;

import java.io.IOException;
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
    private static final String PluginName = new String("[TM4JTestResultReporter]");
    private final String pInfo = String.format("%s [INFO]", PluginName);
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
        logger.printf("%s Examining test results...%n", pInfo);
        logger.printf(String.format("Build result is %s%n", build.getResult().toString()));
        Tm4jPlugin plugin = new Tm4jPlugin();
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
		FilePath workspace = build.getWorkspace();
        try {
        	if (Tm4jConstants.CUCUMBER.equals(this.format)) {
        		plugin.uploadCucumberFile(jiraInstances, workspace, this.filePath, this.serverAddress, this.projectKey, this.autoCreateTestCases);
        	} else {
        		plugin.uploadCustomFormatFile(jiraInstances, workspace, this.serverAddress, this.projectKey, this.autoCreateTestCases);
        	}
        } catch (IOException e) {
            logger.printf("%s Error.%n", pInfo);
            logger.printf("%s Stack trace: %n", e);
            return false;
        }
    	logger.printf("%s Done.%n", pInfo);
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
			this.jiraInstances = new ArrayList<Tm4JInstance>();
			Object jiraInstances = formData.get("jiraInstances");
			if (jiraInstances instanceof JSONArray) {
				JSONArray jiraInstancesList = (JSONArray) jiraInstances;
				for (Object jiraInstance :  jiraInstancesList.toArray()) {
					createAnInstance((JSONObject)jiraInstance);
				}
			} else {
				createAnInstance(formData.getJSONObject("jiraInstances"));
			}
			save();
			return super.configure(request, formData);
		}
	
		private void createAnInstance(JSONObject jiraInstance) {
			Tm4JInstance tm4jInstance = new Tm4JInstance();
			tm4jInstance.setServerAddress(StringUtils.removeEnd(jiraInstance.getString("serverAddress").trim(), "/"));
			tm4jInstance.setUsername(jiraInstance.getString("username").trim());
			tm4jInstance.setPassword(jiraInstance.getString("password").trim());
			RestClient restClient = new RestClient();
			if (restClient.isValidCredentials(tm4jInstance.getServerAddress(), tm4jInstance.getUsername(), tm4jInstance.getPassword())) {
				this.jiraInstances.add(tm4jInstance);
			}
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

		public List<Tm4JInstance> getJiraInstances() {
			return jiraInstances;
		}
	
		public void setJiraInstances(List<Tm4JInstance> jiraInstances) {
			this.jiraInstances = jiraInstances;
		}
	}
}

