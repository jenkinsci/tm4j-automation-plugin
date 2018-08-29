package com.adaptavist.tm4j.jenkins.extensions.configuration;

import com.adaptavist.tm4j.jenkins.extensions.Tm4JInstance;
import com.adaptavist.tm4j.jenkins.Tm4jConstants;
import com.adaptavist.tm4j.jenkins.extensions.Tm4jFormHelper;
import com.adaptavist.tm4j.jenkins.http.Tm4jJiraRestClient;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.TM4J_GLOBAL_CONFIGURATION;

@Extension
public class Tm4jGlobalConfiguration extends GlobalConfiguration {

    private List<Tm4JInstance> jiraInstances;

    public Tm4jGlobalConfiguration() {
        load();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return TM4J_GLOBAL_CONFIGURATION;
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
        request.bindParameters(this);
        Object formJiraInstances = formData.get("jiraInstances");
        try {
            this.jiraInstances = crateJiraInstances(formJiraInstances);
        } catch (Exception e) {
            throw new FormException(MessageFormat.format(Tm4jConstants.ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA, e.getMessage()), "testManagementForJira");
        }
        save();
        return super.configure(request, formData);
    }

    private List<Tm4JInstance> crateJiraInstances(Object formJiraInstances) throws Exception {
        if (formJiraInstances == null) {
            throw new Exception(Tm4jConstants.JIRA_INSTANCES_CAN_NOT_BE_NULL_OR_EMPTY);
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

    private Tm4JInstance createAnInstance(JSONObject formJiraInstance) throws Exception {
        Tm4JInstance tm4jInstance = new Tm4JInstance();
        String serverAddres = formJiraInstance.getString("serverAddress");
        String username = formJiraInstance.getString("username");
        String password = formJiraInstance.getString("password");
        if (StringUtils.isBlank(serverAddres)) {
            throw new Exception(Tm4jConstants.PLEASE_ENTER_THE_SERVER_NAME);
        }
        if (StringUtils.isBlank(username)) {
            throw new Exception(Tm4jConstants.PLEASE_ENTER_THE_USERNAME);
        }
        if (StringUtils.isBlank(password)){
            throw new Exception(Tm4jConstants.PLEASE_ENTER_THE_PASSWORD);
        }
        tm4jInstance.setServerAddress(StringUtils.removeEnd(serverAddres.trim(), "/"));
        tm4jInstance.setUsername(username.trim());
        tm4jInstance.setPassword(password.trim());

        if (tm4jInstance.isValidCredentials()) {
            return tm4jInstance;
        }
        throw new Exception(Tm4jConstants.INVALID_USER_CREDENTIALS);
    }

    public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String username, @QueryParameter String password) {
        return new Tm4jFormHelper().testConnection(serverAddress, username, password);
    }

    public FormValidation doCheckServerAddress(@QueryParameter String serverAddress) {
        return new Tm4jFormHelper().doCheckServerAddress(serverAddress);
    }

    public FormValidation doCheckUsername(@QueryParameter String username) {
        return new Tm4jFormHelper().doCheckUsername(username);
    }

    public FormValidation doCheckPassword(@QueryParameter String password) {
        return new Tm4jFormHelper().doCheckPassword(password);
    }

    public List<Tm4JInstance> getJiraInstances() {
        return jiraInstances;
    }

    public void setJiraInstances(List<Tm4JInstance> jiraInstances) {
        this.jiraInstances = jiraInstances;
    }
}
