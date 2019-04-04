package com.adaptavist.tm4j.jenkins.extensions.configuration;

import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.JiraCloudInstance;
import com.adaptavist.tm4j.jenkins.extensions.JiraInstance;
import com.adaptavist.tm4j.jenkins.utils.Constants;
import com.adaptavist.tm4j.jenkins.utils.FormHelper;
import com.adaptavist.tm4j.jenkins.utils.Permissions;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.adaptavist.tm4j.jenkins.utils.Constants.TM4J_GLOBAL_CONFIGURATION;

@Extension
public class Tm4jGlobalConfiguration extends GlobalConfiguration {

    private static final String CLOUD_TYPE = "cloud";
    private List<Instance> jiraInstances;

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
        Permissions.checkAdminPermission();
        request.bindParameters(this);
        Object formJiraInstances = formData.get("jiraInstances");
        try {
            this.jiraInstances = crateJiraInstances(formJiraInstances);
        } catch (Exception e) {
            throw new FormException(MessageFormat.format(Constants.ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA, e.getMessage()), "testManagementForJira");
        }
        save();
        return true;
    }

    private List<Instance> crateJiraInstances(Object formJiraInstances) throws Exception {
        List<Instance> newJiraInstances = new ArrayList<>();

        if (formJiraInstances == null) {
            return newJiraInstances;
        }

        if (formJiraInstances instanceof JSONArray) {
            JSONArray jiraInstancesList = (JSONArray) formJiraInstances;
            for (Object jiraInstance : jiraInstancesList.toArray()) {
                JSONObject instance = ((JSONObject)jiraInstance).getJSONObject("type");
                newJiraInstances.add(createInstance(instance));
            }
        } else {
            newJiraInstances.add(createInstance(((JSONObject) formJiraInstances).getJSONObject("type")));
        }
        return newJiraInstances;
    }

    private Instance createInstance(JSONObject jsonJiraInstance) throws Exception {
        if (CLOUD_TYPE.equals(jsonJiraInstance.getString("value"))) {
            return createCloudInstance(jsonJiraInstance);
        } else {
            return createServerInstance(jsonJiraInstance);
        }
    }

    private JiraInstance createServerInstance(JSONObject formJiraInstance) throws Exception {
        JiraInstance jiraInstance = new JiraInstance();
        String serverAddress = formJiraInstance.getString("serverAddress");
        String username = formJiraInstance.getString("username");
        String password = formJiraInstance.getString("password");
        if (StringUtils.isBlank(serverAddress)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_SERVER_NAME);
        }
        if (StringUtils.isBlank(username)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_USERNAME);
        }
        if (StringUtils.isBlank(password)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_PASSWORD);
        }
        jiraInstance.setServerAddress(StringUtils.removeEnd(serverAddress.trim(), "/"));
        jiraInstance.setUsername(username.trim());
        jiraInstance.setPassword(Secret.fromString(password));
        if (jiraInstance.isValidCredentials()) {
            return jiraInstance;
        }
        throw new Exception(Constants.INVALID_CREDENTIALS);
    }

    private JiraCloudInstance createCloudInstance(JSONObject formJiraInstance) throws Exception {
        JiraCloudInstance jiraInstance = new JiraCloudInstance();
        String jwt = formJiraInstance.getString("jwt");
        if (StringUtils.isBlank(jwt)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_JWT);
        }
        jiraInstance.setJwt(Secret.fromString(jwt));
        if (jiraInstance.isValidCredentials()) {
            return jiraInstance;
        }
        throw new Exception(Constants.INVALID_CREDENTIALS);
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String
            username, @QueryParameter String password, @QueryParameter String jwt, @QueryParameter Boolean cloud) {
        Permissions.checkAdminPermission();
        return new FormHelper().testConnection(serverAddress, username, password, jwt, cloud);
    }

    @POST
    public FormValidation doCheckServerAddress(@QueryParameter String serverAddress) {
        Permissions.checkAdminPermission();
        return new FormHelper().doCheckServerAddress(serverAddress);
    }

    @POST
    public FormValidation doCheckUsername(@QueryParameter String username) {
        Permissions.checkAdminPermission();
        return new FormHelper().doCheckUsername(username);
    }

    @POST
    public FormValidation doCheckPassword(@QueryParameter String password) {
        Permissions.checkAdminPermission();
        return new FormHelper().doCheckPassword(password);
    }

    @POST
    public FormValidation doCheckJwt(@QueryParameter String jwt) {
        Permissions.checkAdminPermission();
        return new FormHelper().doCheckPassword(jwt);
    }

    public List<Instance> getJiraInstances() {
        return jiraInstances;
    }

    public List<Instance> getJiraServerInstances() {
        return jiraInstances.stream().filter(instance -> !instance.cloud()).collect(Collectors.toList());
    }

    public void setJiraInstances(List<Instance> jiraInstances) {
        this.jiraInstances = jiraInstances;
    }
}
