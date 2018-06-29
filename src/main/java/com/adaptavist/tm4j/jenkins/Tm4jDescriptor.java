package com.adaptavist.tm4j.jenkins;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.ADD_TM4J_GLOBAL_CONFIG;
import static com.adaptavist.tm4j.jenkins.Tm4jConstants.NAME_POST_BUILD_ACTION;

import java.util.ArrayList;
import java.util.Iterator;
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

	@Override
	public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
		req.bindParameters(this);
		this.jiraInstances = new ArrayList<Tm4JInstance>();
		Object object = formData.get("jiraInstances");
		if (object instanceof JSONArray) {
			JSONArray jArr = (JSONArray) object;
			for (Iterator<?> iterator = jArr.iterator(); iterator.hasNext();) {
				JSONObject jObj = (JSONObject) iterator.next();
				createAnInstance(jObj);
			}
		} else if (object instanceof JSONObject) {
			JSONObject jObj = formData.getJSONObject("jiraInstances");
			createAnInstance(jObj);
		}
		save();
		return super.configure(req, formData);
	}

	private void createAnInstance(JSONObject jObj) {
		Tm4JInstance tm4JInstance = new Tm4JInstance();
		String server = jObj.getString("serverAddress").trim();
		String user = jObj.getString("username").trim();
		String pass = jObj.getString("password").trim();
		server = StringUtils.removeEnd(server, "/");
		tm4JInstance.setServerAddress(server);
		tm4JInstance.setUsername(user);
		tm4JInstance.setPassword(pass);
		RestClient restClient = new RestClient(server, user, pass);
		if (ConfigurationValidator.validateTm4JConfiguration(restClient)) {
			this.jiraInstances.add(tm4JInstance);
		}
		restClient.destroy();
	}

	@Override
	public String getDisplayName() {
		return NAME_POST_BUILD_ACTION;
	}

	public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String username, @QueryParameter String password) {

		serverAddress = StringUtils.removeEnd(serverAddress, "/");

		if (StringUtils.isBlank(serverAddress)) {
			return FormValidation.error("Please enter the server name");
		}

		if (StringUtils.isBlank(username)) {
			return FormValidation.error("Please enter the username");
		}

		if (StringUtils.isBlank(password)) {
			return FormValidation.error("Please enter the password");
		}

		if (!(serverAddress.trim().startsWith("https://") || serverAddress.trim().startsWith("http://"))) {
			return FormValidation.error("Incorrect server address format");
		}

		String jiraURL = URLValidator.validateURL(serverAddress);

		if (!jiraURL.startsWith("http")) {
			return FormValidation.error(jiraURL);
		}
		RestClient restClient = new RestClient(serverAddress, username, password);

		if (!ServerInfo.findServerAddressIsValidTm4JURL(restClient)) {
			return FormValidation.error("This is not a valid Jira Server");
		}

		if (!ServerInfo.validateCredentials(restClient)) {
			return FormValidation.error("Invalid user credentials");
		}
		restClient.destroy();
		return FormValidation.ok("Connection to JIRA has been validated");
	}

	public ListBoxModel doFillServerAddressItems(@QueryParameter String serverAddress) {
		ListBoxModel modelbox = new ListBoxModel();
		if (this.jiraInstances != null && this.jiraInstances.size() > 0) {
			for (Tm4JInstance s : this.jiraInstances) {
				modelbox.add(s.getServerAddress());
			}
		} else if (StringUtils.isBlank(serverAddress) || serverAddress.trim().equals(ADD_TM4J_GLOBAL_CONFIG)) {
			modelbox.add(ADD_TM4J_GLOBAL_CONFIG);
		} else {
			modelbox.add(ADD_TM4J_GLOBAL_CONFIG);
		}
		return modelbox;
	}

	public List<Tm4JInstance> getJiraInstances() {
		return jiraInstances;
	}

	public void setJiraInstances(List<Tm4JInstance> jiraInstances) {
		this.jiraInstances = jiraInstances;
	}

}