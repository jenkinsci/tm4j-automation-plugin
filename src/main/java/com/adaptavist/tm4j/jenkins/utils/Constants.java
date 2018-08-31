package com.adaptavist.tm4j.jenkins.utils;

import java.io.PrintStream;

public class Constants {
	public static final String NAME_POST_BUILD_ACTION = "Test Management for Jira: Publish Test Results";
	public static final String NAME_EXPORT_BUILD_STEP = "Test Management for Jira: Export Feature Files";
    public static final String ADD_TM4J_GLOBAL_CONFIG = "Please configure at least one Jira instance in the global configuration";
	public static final String CONNECTION_TO_JIRA_HAS_BEEN_VALIDATED = "The connection to Jira has been validated";
	public static final String INVALID_USER_CREDENTIALS = "Invalid user credentials";
	public static final String INCORRECT_SERVER_ADDRESS_FORMAT = "Incorrect server address format";
	public static final String PLEASE_ENTER_THE_PASSWORD = "Please enter the password";
	public static final String PLEASE_ENTER_THE_SERVER_NAME = "Please enter the server name";
	public static final String PLEASE_ENTER_THE_USERNAME = "Please enter the username";
	public static final String TM4J_OUTPUT_RESULT_FOR_JUNIT = "Test Management for Jira Output Result For JUnit";
	public static final String CUCUMBER = "Cucumber";
	public static final String CUSTOM_FORMAT_FILE_NAME = "tm4j_result.json";
	public static final String PROJECT_KEY_IS_REQUIRED = "Project key is required.";
	public static final String FILE_PATH_IS_REQUIRED = "File path is required.";
	public static final String THERE_ARE_NO_JIRA_INSTANCES_CONFIGURED = "There are no Jira instances configured. Please, go to Jenkins global configurations and add some Jira instances.";
	public static final String JIRA_INSTANCE_NOT_FOUND = "Jira instance not found for this server address {0}";
	public static final String JIRA_INSTANCES_CAN_NOT_BE_NULL_OR_EMPTY = "Jira instances are not properly configured. Please check if the URL, username and password are valid and not empty.";
	public static final String ERROR_AT_GLOBAL_CONFIGURATIONS_OF_TEST_MANAGEMENT_FOR_JIRA = "Error at global configurations of Test Management for Jira: {0}";
	public static final String DEFAULT_FEATURE_FILES_PATH = "target/features";
	public static final String TM4J_GLOBAL_CONFIGURATION = "TM4J configuration";

	public static PrintStream logger;
	private static final String PLUGIN_NAME = "[Test Management for Jira]";
	public static String INFO = String.format("%s [INFO]", PLUGIN_NAME);
	public static String ERROR = String.format("%s [ERROR]", PLUGIN_NAME);
}
