package com.adaptavist.tm4j.jenkins.extensions.buildsteps;

import com.adaptavist.tm4j.jenkins.extensions.Tm4JInstance;
import com.adaptavist.tm4j.jenkins.extensions.Tm4jFormHelper;
import com.adaptavist.tm4j.jenkins.io.Tm4jJiraRestClient;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;

import static com.adaptavist.tm4j.jenkins.Tm4jConstants.NAME_EXPORT_BUILD_STEP;
import static java.lang.String.format;

public class Tm4jFeatureFilesExporter extends Builder {

    private String serverAddress;
    private String projectKey;
    private String filePath;

    @DataBoundConstructor
    public Tm4jFeatureFilesExporter(String serverAddress, String projectKey, String filePath) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.filePath = filePath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
        String workspace = build.getWorkspace().getRemote() + "/";
        try {
            String tql = format("testCase.projectKey = '%s'", this.projectKey);
            String featureFilesPath = workspace + filePath;
            new Tm4jJiraRestClient().exportFeatureFiles(jiraInstances, featureFilesPath, serverAddress, tql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getServerAddress() {return serverAddress;}

    public void setServerAddress(String serverAddress) {this.serverAddress = serverAddress;}

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Inject
        private Tm4jGlobalConfiguration tm4jGlobalConfiguration;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return NAME_EXPORT_BUILD_STEP;
        }

        public ListBoxModel doFillServerAddressItems() {
            return new Tm4jFormHelper().fillServerAddressItens(getJiraInstances());
        }

        public List<Tm4JInstance> getJiraInstances() {
            return tm4jGlobalConfiguration.getJiraInstances();
        }

        public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
            return new Tm4jFormHelper().doCheckProjectKey(projectKey);
        }

        public FormValidation doCheckFilePath(@QueryParameter String filePath) {
            return new Tm4jFormHelper().doCheckFilePath(filePath);
        }
    }
}
