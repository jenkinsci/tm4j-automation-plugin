package com.adaptavist.tm4j.jenkins.extensions.postbuildactions;

import static com.adaptavist.tm4j.jenkins.utils.Constants.CUCUMBER;
import static com.adaptavist.tm4j.jenkins.utils.Constants.ERROR;
import static com.adaptavist.tm4j.jenkins.utils.Constants.INFO;
import static com.adaptavist.tm4j.jenkins.utils.Constants.JUNIT_RESULT_FILE;
import static com.adaptavist.tm4j.jenkins.utils.Constants.NAME_POST_BUILD_ACTION;

import com.adaptavist.tm4j.jenkins.extensions.CustomTestCycle;
import com.adaptavist.tm4j.jenkins.extensions.Instance;
import com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration;
import com.adaptavist.tm4j.jenkins.http.Tm4jJiraRestClient;
import com.adaptavist.tm4j.jenkins.utils.FormHelper;
import com.adaptavist.tm4j.jenkins.utils.Validator;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

public class TestResultPublisher extends Notifier implements SimpleBuildStep {

    private final Boolean customizeTestCycle;
    private final CustomTestCycle customTestCycle;
    private String serverAddress;
    private String projectKey;
    private String filePath;
    private String format;
    private Boolean autoCreateTestCases;

    @DataBoundConstructor
    public TestResultPublisher(
        final String serverAddress,
        final String projectKey,
        final String filePath,
        final Boolean autoCreateTestCases,
        final String format,
        final CustomTestCycle customTestCycle
    ) {
        this.serverAddress = serverAddress;
        this.projectKey = projectKey;
        this.filePath = filePath;
        this.autoCreateTestCases = autoCreateTestCases;
        this.format = format;
        this.customizeTestCycle = customTestCycle != null;
        this.customTestCycle = customTestCycle;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(
        @Nonnull Run<?, ?> run,
        @Nonnull FilePath workspace,
        @Nonnull EnvVars envVars,
        @Nonnull Launcher launcher,
        TaskListener listener
    ) {
        final PrintStream logger = listener.getLogger();
        publishResults(logger, run, workspace);
    }

    private void publishResults(final PrintStream logger, final Run<?, ?> run, final FilePath workspace) {
        logger.printf("%s Publishing test results...%n", INFO);

        try {
            final String directory = getDirectory(workspace, run);
            final List<Instance> jiraInstances = getDescriptor().getJiraInstances();
            validateFieldsAndUploadResults(logger, jiraInstances, directory);
        } catch (final Exception e) {
            handlePublishException(logger, run, e);
        }
    }

    private void handlePublishException(final PrintStream logger, final Run<?, ?> run, final Exception exception) {
        run.setResult(Result.FAILURE);

        logger.printf("%s There was an error while publishing test results to Zephyr Scale and they were not sent. Error details: %n",
            ERROR);

        throw new RuntimeException(exception);
    }

    private void validateFieldsAndUploadResults(PrintStream logger, List<Instance> jiraInstances, String directory) throws Exception {
        validateFields();

        Tm4jJiraRestClient tm4jJiraRestClient = new Tm4jJiraRestClient(logger, jiraInstances, this.serverAddress);

        uploadResultsFile(tm4jJiraRestClient, directory);

    }

    private void validateFields() throws Exception {
        new Validator()
            .validateProjectKey(this.projectKey)
            .validateFilePath(this.filePath)
            .validateFormat(this.format)
            .validateServerAddress(this.serverAddress);
    }

    private void uploadResultsFile(Tm4jJiraRestClient tm4jJiraRestClient, String directory) throws Exception {
        if (CUCUMBER.equals(this.format)) {
            tm4jJiraRestClient.uploadCucumberFile(directory, this.filePath, this.projectKey, this.autoCreateTestCases,
                this.customTestCycle);

            return;
        }

        if (JUNIT_RESULT_FILE.equals(this.format)) {
            tm4jJiraRestClient.uploadJUnitXmlResultFile(directory, this.filePath, this.projectKey, this.autoCreateTestCases,
                this.customTestCycle);

            return;
        }

        tm4jJiraRestClient.uploadCustomFormatFile(directory, this.projectKey, this.autoCreateTestCases, this.customTestCycle);
    }

    private String getDirectory(FilePath workspace, Run<?, ?> run) throws IOException, InterruptedException {
        if (workspace.isRemote()) {
            FilePath path = new FilePath(run.getRootDir());
            workspace.copyRecursiveTo(this.filePath, path);
            return run.getRootDir() + "/";
        }

        return workspace.getRemote() + "/";
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

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean getAutoCreateTestCases() {
        return autoCreateTestCases;
    }

    public void setAutoCreateTestCases(Boolean autoCreateTestCases) {
        this.autoCreateTestCases = autoCreateTestCases;
    }

    public Boolean getCustomizeTestCycle() {
        return customizeTestCycle;
    }

    public String getName() {
        return this.customTestCycle.getName();
    }

    public String getDescription() {
        return this.customTestCycle.getDescription();
    }

    public String getJiraProjectVersion() {
        return this.customTestCycle.getJiraProjectVersion();
    }

    public String getFolderId() {
        return this.customTestCycle.getFolderId();
    }

    public String getCustomFields() {
        return this.customTestCycle.getCustomFields();
    }

    public CustomTestCycle getCustomTestCycle() {
        return this.customTestCycle;
    }

    @Symbol("publishTestResults")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Inject
        private Tm4jGlobalConfiguration tm4jGlobalConfiguration;

        public DescriptorImpl() {
            super(TestResultPublisher.class);
            load();
        }

        @Override
        public Publisher newInstance(StaplerRequest request, @Nonnull JSONObject formData) throws FormException {
            return super.newInstance(request, formData);
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return NAME_POST_BUILD_ACTION;
        }

        public ListBoxModel doFillServerAddressItems() {
            return new FormHelper().fillServerAddressItems(getJiraInstances());
        }

        public ListBoxModel doFillFormatItems() {
            return new FormHelper().fillFormat();
        }

        @POST
        public FormValidation doCheckProjectKey(@QueryParameter String projectKey) {
            return new FormHelper().doCheckProjectKey(projectKey);
        }

        @POST
        public FormValidation doCheckFilePath(@QueryParameter String filePath) {
            return new FormHelper().doCheckFilePath(filePath);
        }

        List<Instance> getJiraInstances() {
            return tm4jGlobalConfiguration.getJiraInstances();
        }
    }
}
