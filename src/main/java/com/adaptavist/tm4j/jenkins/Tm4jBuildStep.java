package com.adaptavist.tm4j.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class Tm4jBuildStep extends Builder {

    private String serverAddress;
    private String tql;

    @DataBoundConstructor
    public Tm4jBuildStep(String serverAddress, String tql) {
        this.serverAddress = serverAddress;
        this.tql = tql;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        List<Tm4JInstance> jiraInstances = getDescriptor().getJiraInstances();
        String workspace = build.getWorkspace().getRemote() + "/";
        try {
            new Tm4jPlugin().exportFeatureFiles(jiraInstances, workspace, serverAddress, tql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Tm4jBuildStepDescriptor getDescriptor() {
        return (Tm4jBuildStepDescriptor) super.getDescriptor();
    }

    public String getServerAddress() {return serverAddress;}

    public void setServerAddress(String serverAddress) {this.serverAddress = serverAddress;}

    public String getTql() {
        return tql;
    }

    public void setTql(String tql) {
        this.tql = tql;
    }

    @Extension
    public static class Tm4jBuildStepDescriptor extends BuildStepDescriptor<Builder> {

        @Inject
        private Tm4jGlobalConfiguration tm4jGlobalConfiguration;

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "TM4J Build Step";
        }

        public ListBoxModel doFillServerAddressItems() {
            return new Tm4jForm().fillServerAddressItens(getJiraInstances());
        }

        public List<Tm4JInstance> getJiraInstances() {
            return tm4jGlobalConfiguration.getJiraInstances();
        }
    }
}
