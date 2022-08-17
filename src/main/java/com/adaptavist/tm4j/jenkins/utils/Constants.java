package com.adaptavist.tm4j.jenkins.utils;

public class Constants {
    public static final String NAME_POST_BUILD_ACTION = "Zephyr Scale: Publish Test Results";
    public static final String NAME_DOWNLOAD_BUILD_STEP = "Zephyr Scale: Download Feature Files";
    public static final String ADD_ZEPHYR_SCALE_GLOBAL_CONFIG = "Please configure at least one Jira instance in the global configuration";
    public static final String CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED = "The connection to Jira has been validated";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String INCORRECT_SERVER_ADDRESS_FORMAT = "Incorrect server address format";
    public static final String PLEASE_ENTER_THE_PASSWORD = "Please enter the password";
    public static final String PLEASE_ENTER_THE_SERVER_NAME = "Please enter the server name";
    public static final String PLEASE_ENTER_THE_JWT = "Please enter the API key";
    public static final String SERVER_URL_IS_REQUIRED = "Server URL is required";
    public static final String PLEASE_ENTER_THE_USERNAME = "Please enter the username";
    public static final String ZEPHYR_SCALE_OUTPUT_RESULT_FOR_JUNIT = "Zephyr Scale Output Result For JUnit";
    public static final String JUNIT_RESULT_FILE = "JUnit XML Result File";
    public static final String CUCUMBER = "Cucumber";
    public static final String CUSTOM_FORMAT_FILE_LEGACY = "tm4j_result.json";
    public static final String CUSTOM_FORMAT_FILE = "zephyrscale_result.json";
    public static final String PROJECT_KEY_IS_REQUIRED = "Project key is required.";
    public static final String FILE_PATH_IS_REQUIRED = "File path is required.";
    public static final String FILE_TARGET_IS_REQUIRED = "File target is required.";
    public static final String FORMAT_IS_REQUIRED = "The test framework is required.";
    public static final String THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED = "There are no Jira instances configured. Please, go to Jenkins global configurations and add some Jira instances.";
    public static final String JIRA_INSTANCE_NOT_FOUND = "Jira instance not found for this server address {0}";
    public static final String ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA = "Error at global configurations of Zephyr Scale: {0}";
    public static final String DEFAULT_FEATURE_FILES_PATH = "target/features";
    public static final String ZEPHYR_SCALE_GLOBAL_CONFIGURATION = "Zephyr Scale configuration";
    private static final String PLUGIN_NAME = "[Zephyr Scale]";
    public static final String INFO = String.format("%s [INFO]", PLUGIN_NAME);
    public static final String ERROR = String.format("%s [ERROR]", PLUGIN_NAME);
    public static final String INVALID_INSTANCE_TYPE = "Invalid instance type. value must be either 'cloud' or 'server'";
    public static final String INVALID_URL_FORMAT= "Invalid url format";
}
