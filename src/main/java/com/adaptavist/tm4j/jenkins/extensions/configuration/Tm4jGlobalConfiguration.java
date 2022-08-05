package com.adaptavist.tm4j.jenkins.extensions.configuration;

import com.adaptavist.tm4j.jenkins.extensions.*;
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
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.*;

import static com.adaptavist.tm4j.jenkins.utils.Constants.ZEPHYR_SCALE_GLOBAL_CONFIGURATION;

@Extension
@Symbol("zephyr-scale")
public class Tm4jGlobalConfiguration extends GlobalConfiguration {

    private static final String CLOUD_TYPE = "cloud";
    private static final String SERVER_TYPE = "server";
    private static final String JIRA_INSTANCES = "jiraInstances";
    private Collection<JiraInstance> jiraInstances;

    public Tm4jGlobalConfiguration() {
        load();
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return ZEPHYR_SCALE_GLOBAL_CONFIGURATION;
    }

    @Override
    public boolean configure(StaplerRequest request, JSONObject formData) throws FormException {
        Permissions.checkAdminPermission();
        request.bindParameters(this);
        List<JiraInstance> newJiraInstances = new ArrayList<>();
        try {
            Object formJiraInstances = formData.get(JIRA_INSTANCES);
            JSONArray jiraInstancesList = new JSONArray();

            if(formJiraInstances instanceof JSONArray) {
                jiraInstancesList = formData.getJSONArray(JIRA_INSTANCES);
            } else {
                jiraInstancesList.add(formData.getJSONObject(JIRA_INSTANCES));
            }

            for (Object jiraInstance : jiraInstancesList.toArray()) {
                JSONObject instance = ((JSONObject)jiraInstance).getJSONObject("type");
                JiraInstance formInstance = new JiraInstance();
                formInstance.setValue(instance.getString("value"));

                if (CLOUD_TYPE.equalsIgnoreCase(formInstance.getValue())) {
                    formInstance.setCloudAddress((String) instance.getOrDefault("cloudAddress", null));
                    formInstance.setJwt(Secret.fromString((String) instance.getOrDefault("jwt", null)));
                } else if (SERVER_TYPE.equalsIgnoreCase(formInstance.getValue())) {
                    formInstance.setServerAddress((String) instance.getOrDefault("serverAddress", null));
                    formInstance.setUsername((String) instance.getOrDefault("username", null));
                    formInstance.setPassword(Secret.fromString((String) instance.getOrDefault("password", null)));
                } else {
                    throw new Exception(Constants.INVALID_INSTANCE_TYPE);
                }

                validate(formInstance);
                newJiraInstances.add(formInstance);

            }

            this.jiraInstances = newJiraInstances;

            createJiraInstances(this.jiraInstances);
        } catch (Exception e) {
            throw new FormException(MessageFormat.format(Constants.ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA, e.getMessage()), "testManagementForJira");
        }
        save();
        return true;
    }

    private List<Instance> createJiraInstances(Collection<JiraInstance> formJiraInstances) throws Exception {
        List<Instance> newJiraInstances = new ArrayList<>();

        if (formJiraInstances == null) {
            return newJiraInstances;
        }

        for(JiraInstance instance: formJiraInstances){
            newJiraInstances.add(createInstance(instance));
        }

        return newJiraInstances;
    }

    private Instance createInstance(JiraInstance jsonJiraInstance) throws Exception {
        if (CLOUD_TYPE.equals(jsonJiraInstance.getValue())) {
            return createCloudInstance(jsonJiraInstance);
        } else {
            return createServerInstance(jsonJiraInstance);
        }
    }

    private JiraServerInstance createServerInstance(JiraInstance formJiraInstance) throws Exception {

        JiraServerInstance jiraServerInstance = getServerInstance(formJiraInstance);

        if (jiraServerInstance.isValidCredentials()) {
            return jiraServerInstance;
        }
        throw new Exception(Constants.INVALID_CREDENTIALS);
    }

    private JiraCloudInstance createCloudInstance(JiraInstance formJiraInstance) throws Exception {

        JiraCloudInstance jiraInstance = getCloudInstance(formJiraInstance);
        Secret jwt = formJiraInstance.getJwt();
        jiraInstance.setJwt(jwt);
        if (jiraInstance.isValidCredentials()) {
            return jiraInstance;
        }
        throw new Exception(Constants.INVALID_CREDENTIALS);
    }

    @POST
    public FormValidation doTestConnection(@QueryParameter String serverAddress, @QueryParameter String
            username, @QueryParameter String password, @QueryParameter String jwt, @QueryParameter String type) {
        Permissions.checkAdminPermission();
        return new FormHelper().testConnection(serverAddress, username, password, jwt, type);
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
        return new FormHelper().doCheckJwt(jwt);
    }

    public List<Instance> getJiraInstances() {
        //Here, no authentication is done on each instance as it has been done during configuration

        if (Objects.isNull(jiraInstances)) {
            return Collections.emptyList();
        }

        List<Instance> instances = new ArrayList<>();

        for (JiraInstance instance : this.jiraInstances){
            try {
                validate(instance);

                if (CLOUD_TYPE.equals(instance.getValue())) {
                    instances.add(getCloudInstance(instance));
                } else {
                    instances.add(getServerInstance(instance));
                }

            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return instances;
    }

    @DataBoundSetter
    public void setJiraInstances(Collection<JiraInstance> jiraInstances) {
        this.jiraInstances = jiraInstances;
    }

    private JiraCloudInstance getCloudInstance(JiraInstance formJiraInstance) {
        JiraCloudInstance jiraCloudInstance = new JiraCloudInstance();
        jiraCloudInstance.setValue(CLOUD_TYPE);
        Secret jwt = formJiraInstance.getJwt();
        jiraCloudInstance.setJwt(jwt);
        jiraCloudInstance.setCloudAddress(formJiraInstance.getCloudAddress());
        return jiraCloudInstance;
    }

    private JiraServerInstance getServerInstance(JiraInstance formJiraInstance) {
        JiraServerInstance jiraServerInstance = new JiraServerInstance();
        String serverAddress = formJiraInstance.getServerAddress();
        String username = formJiraInstance.getUsername();
        Secret password = formJiraInstance.getPassword();
        jiraServerInstance.setServerAddress(StringUtils.removeEnd(serverAddress.trim(), "/"));
        jiraServerInstance.setUsername(username.trim());
        jiraServerInstance.setPassword(password);
        jiraServerInstance.setValue(SERVER_TYPE);

        return jiraServerInstance;

    }

    private void validate(JiraInstance jiraInstance) throws Exception {

        if(jiraInstance.getValue() == null) {
            throw new Exception(Constants.INVALID_INSTANCE_TYPE);
        }

        if (CLOUD_TYPE.equalsIgnoreCase(jiraInstance.getValue())) {
            validateCloudInstance(jiraInstance);
        } else if (SERVER_TYPE.equalsIgnoreCase(jiraInstance.getValue())) {
            validateServerInstance(jiraInstance);
        } else {
            throw new Exception(Constants.INVALID_INSTANCE_TYPE);
        }

    }

    private void validateServerInstance(JiraInstance jiraInstance) throws Exception {
        String serverAddress = jiraInstance.getServerAddress();
        String username = jiraInstance.getUsername();
        Secret password = jiraInstance.getPassword();
        if (StringUtils.isBlank(serverAddress)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_SERVER_NAME);
        }
        if (StringUtils.isBlank(username)) {
            throw new Exception(Constants.PLEASE_ENTER_THE_USERNAME);
        }
        if (StringUtils.isBlank(password.getPlainText())) {
            throw new Exception(Constants.PLEASE_ENTER_THE_PASSWORD);
        }

    }

    private void validateCloudInstance(JiraInstance jiraInstance) throws Exception {
        Secret jwt = jiraInstance.getJwt();
        if (StringUtils.isBlank(jwt.getPlainText())) {
            throw new Exception(Constants.PLEASE_ENTER_THE_JWT);
        }

    }
}
