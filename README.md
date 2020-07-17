# TM4J Jenkins Plugin

Jenkins plugin built for integrating automated tests with [Test Management for Jira (TM4J)](https://marketplace.atlassian.com/apps/1213259/test-management-for-jira) on any Jira deployment (Cloud, Server and DataCenter).

Test Management for Jira (TM4J) is the enterprise test management app to plan, manage, and measure your entire testing life-cycle inside Jira for both agile and waterfall methodologies. By using TM4J you'll empower agile teams with BDD at scale with Cucumber or your chosen gherkin compatible tool for collaboration between developers, testers and domain experts. With our powerful FREE REST API, easily integrate CI/CD servers, DevOps and test automation tools and frameworks you already use (including Selenium, JUnit, Nunit, Robot, Behave, Calabash) to save time and effort. TM4J is used by more than 3.000 clients worldwide and is the top rated QA and Testing app for Jira.

The plugin introduces two new configuration steps for Jenkins jobs:

1. A build step to download Gherkin feature files (BDD) from TM4J
1. A post-build step to publish the test results back to TM4J

These two configuration steps depends on a global configuration (Manage Jenkins > Configure System)
where the user can add/configure different Jira instances to be used on each build step.

## Pipeline

This is an example using pipeline.

```
pipeline {
    agent any
    stages {
        stage('Preparation') {
            steps {
                git 'ssh://your_repository.git'
            }
        }
        stage('Download Feature Files'){
            steps {
                downloadFeatureFiles serverAddress: 'http://localhost:2990/jira',
                    projectKey: 'WEB',
                    targetPath:'src/test/resources/features'
            }
        }
        stage('Clean Work Space'){
            steps {
                sh 'mvn clean'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn test'
            }
        }
    }
    post {
        always {
            publishTestResults serverAddress: 'http://localhost:2990/jira',
                    projectKey: 'WEB',
                    filePath:'target/cucumber/*.json',
                    format: 'Cucumber',
                    autoCreateTestCases: false
        }
    }
}

```

For the Junit integration with the post-build action use the `Test Management for Jira Output Result For JUnit` format, with the `tm4j_result.json` as filepath.

## Documentation

Check out the documentation for [TM4J Cloud here](https://support.smartbear.com/tm4j-cloud/docs/api-and-test-automation/jenkins-integration.html) and for [TM4J Server/DC here](https://support.smartbear.com/tm4j-server/docs/test-automation/integrations/jenkins.html).

## Support

For any issues or enquiries please get in touch with the Test Management for Jira team at SmartBear using the [support portal](https://smartbear.atlassian.net/servicedesk/customer/portals).
