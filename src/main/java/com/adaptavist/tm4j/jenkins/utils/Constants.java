package com.adaptavist.tm4j.jenkins.utils;

public class Constants {
    public static final String NAME_POST_BUILD_ACTION = "Test Management for Jira: Publish Test Results";
    public static final String NAME_DOWNLOAD_BUILD_STEP = "Test Management for Jira: Download Feature Files";
    public static final String ADD_TM4J_GLOBAL_CONFIG = "Please configure at least one Jira instance in the global configuration";
    public static final String CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED = "The connection to Jira has been validated";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String INCORRECT_SERVER_ADDRESS_FORMAT = "Incorrect server address format";
    public static final String PLEASE_ENTER_THE_PASSWORD = "Please enter the password";
    public static final String PLEASE_ENTER_THE_SERVER_NAME = "Please enter the server name";
    public static final String PLEASE_ENTER_THE_JWT = "Please enter the JSON Web Token (JWT)";
    public static final String SERVER_URL_IS_REQUIRED = "Server URL is required";
    public static final String PLEASE_ENTER_THE_USERNAME = "Please enter the username";
    public static final String TM4J_OUTPUT_RESULT_FOR_JUNIT = "Test Management for Jira Output Result For JUnit";
    public static final String CUCUMBER = "Cucumber";
    public static final String CUSTOM_FORMAT_FILE_NAME = "tm4j_result.json";
    public static final String PROJECT_KEY_IS_REQUIRED = "Project key is required.";
    public static final String FILE_PATH_IS_REQUIRED = "File path is required.";
    public static final String FILE_TARGET_IS_REQUIRED = "File target is required.";
    public static final String FORMAT_IS_REQUIRED = "The test framework is required.";
    public static final String THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED = "There are no Jira instances configured. Please, go to Jenkins global configurations and add some Jira instances.";
    public static final String JIRA_INSTANCE_NOT_FOUND = "Jira instance not found for this server address {0}";
    public static final String ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA = "Error at global configurations of Test Management for Jira: {0}";
    public static final String DEFAULT_FEATURE_FILES_PATH = "target/features";
    public static final String TM4J_GLOBAL_CONFIGURATION = "TM4J configuration";
    private static final String PLUGIN_NAME = "[Test Management for Jira]";
    public static final String INFO = String.format("%s [INFO]", PLUGIN_NAME);
    public static final String ERROR = String.format("%s [ERROR]", PLUGIN_NAME);
}
