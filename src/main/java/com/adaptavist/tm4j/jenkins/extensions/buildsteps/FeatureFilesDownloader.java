package com.adaptavist.tm4j.jenkins.extensions.buildsteps;

import com.adaptavist.tm4j.jenkins.exception.NoTestCasesFoundException;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import com.adaptavist.tm4j.jenkins.http.Tm4jJiraRestClient;
import com.adaptavist.tm4j.jenkins.utils.FormHelper;
import com.adaptavist.tm4j.jenkins.utils.Permissions;
import com.adaptavist.tm4j.jenkins.utils.Validator;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.PrintStream;
import java.util.List;

import static com.adaptavist.tm4j.jenkins.utils.Constants.*;
import static java.lang.String.format;

public class FeatureFilesDownloader extends Builder implements SimpleBuildStep {

    private String serverAddress;
    private String projectKey;
    private String targetPath;

    @DataBoundConstructor
    public FeatureFilesDownloader(String serverAddress, String projectKey, String targetPath) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.targetPath = targetPath;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,@Nonnull FilePath workspace,@Nonnull Launcher launcher, TaskListener listener) {
        final PrintStream logger = listener.getLogger();
        logger.printf("%s Downloading feature files...%n", INFO);
        List<Instance> jiraInstances = getDescriptor().getJiraInstances();
        try {
            new Validator().validateProjectKey(this.projectKey)
                    .validateTargetPath(this.targetPath)
                    .validateServerAddress(this.serverAddress);
            String tql = format("testCase.projectKey = '%s'", this.projectKey);
            Tm4jJiraRestClient tm4jJiraRestClient = new Tm4jJiraRestClient(jiraInstances, serverAddress);
            tm4jJiraRestClient.importFeatureFiles(run.getRootDir(), workspace, targetPath, tql, logger);
        } catch (NoTestCasesFoundException e) {
            logger.printf("%s No feature files have been found for project " +  this.projectKey + ". %n", ERROR);
            run.setResult(Result.FAILURE);
            throw new RuntimeException();
        } catch (Exception e) {
            run.setResult(Result.FAILURE);
            logger.printf("%s There was an error while trying to download feature files from Test Management for Jira. Error details: %n", ERROR);
            logger.printf(" %s  %n", e.getMessage());
            for (StackTraceElement trace : e.getStackTrace()) {
                logger.printf(" %s  %n", trace.toString());
            }
            throw new RuntimeException();
        }
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

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Symbol("downloadFeatureFiles")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Inject
        private Tm4jGlobalConfiguration tm4jGlobalConfiguration;

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return NAME_DOWNLOAD_BUILD_STEP;
        }

        public ListBoxModel doFillServerAddressItems() {
            Permissions.checkAdminPermission();
            return new FormHelper().fillServerAddressItems(getJiraInstances());
        }

        private List<Instance> getJiraInstances() {
            return tm4jGlobalConfiguration.getJiraServerInstances();
        }

        @POST
        public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
            return new FormHelper().doCheckProjectKey(projectKey);
        }

        @POST
        public FormValidation doCheckTargetPath(@QueryParameter String targetPath) {
            return new FormHelper().doCheckTargetPath(targetPath);
        }
    }
}
