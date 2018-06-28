package com.adaptavist.tm4j.jenkins.reporter;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.CaseResult;

import static com.adaptavist.tm4j.jenkins.reporter.Tm4jConstants.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.adaptavist.tm4j.jenkins.model.TestCaseResultModel;
import com.adaptavist.tm4j.jenkins.model.Tm4jConfigModel;
import com.adaptavist.tm4j.jenkins.model.Tm4JInstance;
import com.adaptavist.tm4j.jenkins.utils.rest.Project;
import com.adaptavist.tm4j.jenkins.utils.rest.RestClient;
import com.adaptavist.tm4j.jenkins.utils.rest.ServerInfo;
import com.adaptavist.tm4j.jenkins.utils.rest.TestCaseUtil;

public class Tm4jReporter extends Notifier {

    public static PrintStream logger;

    private String serverAddress;
    private String projectKey;
    private String url;


    private static final String PluginName = new String("[TM4JTestResultReporter]");
    private final String pInfo = String.format("%s [INFO]", PluginName);

    @DataBoundConstructor
    public Tm4jReporter(String serverAddress, String url) {
        this.serverAddress = serverAddress;
        this.url = url;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild build,
                           final Launcher launcher,
                           final BuildListener listener) {
        logger = listener.getLogger();
        logger.printf("%s Examining test results...%n", pInfo);
        logger.printf(String.format("Build result is %s%n", build.getResult().toString()));


        if (!validateBuildConfig()) {
            logger.println("Cannot Proceed. Please verify the job configuration");
            return false;
        }

        Tm4jConfigModel tm4jConfigModel = initializeTm4jData();

        boolean prepareTm4jTests = prepareTm4jTests(build, tm4jConfigModel);

        if (!prepareTm4jTests) {
            logger.println("Error parsing surefire reports.");
            logger.println("Please ensure \"Publish JUnit test result report is added\" as a post build action");
            return false;
        }

        TestCaseUtil.processTestCaseDetails(tm4jConfigModel);

        tm4jConfigModel.getRestClient().destroy();
        logger.printf("%s Done.%n", pInfo);
        return true;
    }

    private boolean prepareTm4jTests(final AbstractBuild build, Tm4jConfigModel tm4jConfigModel) {

        boolean status = true;
        Map<String, Boolean> tm4jTestCaseMap = new HashMap<String, Boolean>();

        TestResultAction testResultAction = build.getAction(TestResultAction.class);
        Collection<SuiteResult> suites = null;

        try {
            suites = testResultAction.getResult().getSuites();
        } catch (Exception e) {
            logger.println(e.getMessage());
        }

        if (suites == null || suites.size() == 0) {
            logger.println("Problem parsing JUnit test Results.");
            return false;
        }


        for (Iterator<SuiteResult> iterator = suites.iterator(); iterator.hasNext(); ) {
            SuiteResult suiteResult = iterator.next();
            List<CaseResult> cases = suiteResult.getCases();
            for (CaseResult caseResult : cases) {
                boolean isPassed = caseResult.isPassed();
                String name = caseResult.getFullName();
                if (!tm4jTestCaseMap.containsKey(name)) {
                    tm4jTestCaseMap.put(name, isPassed);
                }
            }
        }

        logger.print("Total Test Cases : " + tm4jTestCaseMap.size());
        List<TestCaseResultModel> testcases = new ArrayList<TestCaseResultModel>();


        Set<String> keySet = tm4jTestCaseMap.keySet();

        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            String testCaseName = iterator.next();
            Boolean isPassed = tm4jTestCaseMap.get(testCaseName);


            JSONObject isssueType = new JSONObject();
            isssueType.put("id", tm4jConfigModel.getTestIssueTypeId() + "");

            JSONObject project = new JSONObject();
            project.put("id", tm4jConfigModel.getTm4JProjectId());

            JSONObject fields = new JSONObject();
            fields.put("project", project);
            fields.put("summary", testCaseName);
            fields.put("description", "Creating the Test via Jenkins");
            fields.put("issuetype", isssueType);

            JSONObject issue = new JSONObject();
            issue.put("fields", fields);

            TestCaseResultModel caseWithStatus = new TestCaseResultModel();
            caseWithStatus.setPassed(isPassed);
            caseWithStatus.setTestCase(issue.toString());
            caseWithStatus.setTestCaseName(testCaseName);
            testcases.add(caseWithStatus);
        }

        tm4jConfigModel.setTestCases(testcases);

        return status;
    }

    private boolean validateBuildConfig() {
        boolean valid = true;
        if (StringUtils.isBlank(serverAddress)
                || StringUtils.isBlank(projectKey)
                || ADD_TM4J_GLOBAL_CONFIG.equals(serverAddress.trim())
                || ADD_TM4J_GLOBAL_CONFIG.equals(projectKey.trim())) {

            logger.println("Cannot Proceed");
            valid = false;
        }
        return valid;
    }

    private void determineTestIssueTypeId(Tm4jConfigModel tm4jConfig) {
        long testIssueTypeId = ServerInfo.findTestIssueTypeId(tm4jConfig.getRestClient());
        tm4jConfig.setTestIssueTypeId(testIssueTypeId);
    }

    private Tm4jConfigModel initializeTm4jData() {
        Tm4jConfigModel tm4jConfigModel = new Tm4jConfigModel();

        String hostName = StringUtils.removeEnd(serverAddress, "/");
        fetchCredentials(tm4jConfigModel, hostName);

        determineProjectID(tm4jConfigModel);
        determineTestIssueTypeId(tm4jConfigModel);
        return tm4jConfigModel;
    }

    private void fetchCredentials(Tm4jConfigModel tm4jConfig, String url) {
        List<Tm4JInstance> jiraServers = getDescriptor().getJiraInstances();

        for (Tm4JInstance jiraServer : jiraServers) {
            if (StringUtils.isNotBlank(jiraServer.getServerAddress()) && jiraServer.getServerAddress().trim().equals(serverAddress)) {
                String userName = jiraServer.getUsername();
                String password = jiraServer.getPassword();

                RestClient restClient = new RestClient(url, userName, password);
                tm4jConfig.setRestClient(restClient);

                break;
            }
        }
    }

    private void determineProjectID(Tm4jConfigModel tm4jData) {
        long projectId = 0;
        try {
            projectId = Long.parseLong(projectKey);
        } catch (NumberFormatException e1) {
            logger.println("Project Key appears to be Name of the project");
            try {
                Long projectIdByName = Project.getProjectIdByName(projectKey, tm4jData.getRestClient());
                projectId = projectIdByName;
            } catch (Exception e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
        }

        tm4jData.setTm4JProjectId(projectId);
    }


    @Override
    public Tm4jDescriptor getDescriptor() {
        return (Tm4jDescriptor) super.getDescriptor();
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
    public String getUrl() {return  this.url;}
    public void setUrl ( String url) {this.url = url;}
}

