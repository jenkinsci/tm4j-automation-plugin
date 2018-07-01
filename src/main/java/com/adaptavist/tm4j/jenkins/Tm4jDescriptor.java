package com.adaptavist.tm4j.jenkins;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.NAME_POST_BUILD_ACTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Extension
public final class Tm4jDescriptor extends BuildStepDescriptor<Publisher> {

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
		RestClientOld restClient = new RestClientOld(tm4jInstance.getServerAddress(), tm4jInstance.getUsername(), tm4jInstance.getPassword());
		if (ConfigurationValidator.validateTm4JConfiguration(restClient)) {
			this.jiraInstances.add(tm4jInstance);
		}
		restClient.destroy();
	}

	@Override
	public String getDisplayName() {
		return NAME_POST_BUILD_ACTION;
	}

	public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String username, @QueryParameter String password) {
		return new Tm4jForm().testConnection(serverAddress, username, password);
	}

	public ListBoxModel doFillServerAddressItems(@QueryParameter String serverAddress) {
		return new Tm4jForm().fillServerAddressItens(this.jiraInstances, serverAddress);
	}

	public List<Tm4JInstance> getJiraInstances() {
		return jiraInstances;
	}

	public void setJiraInstances(List<Tm4JInstance> jiraInstances) {
		this.jiraInstances = jiraInstances;
	}
}