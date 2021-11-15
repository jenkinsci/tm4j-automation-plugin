# Zephyr Scale Jenkins Plugin

Jenkins plugin built for integrating automated tests with [Zephyr Scale](https://marketplace.atlassian.com/apps/1213259/zephyr-scale-test-management-for-jira) 
on any Jira deployment (Cloud, Server and DataCenter).

Zephyr Scale is the enterprise test management app to plan, manage, and measure your entire testing life-cycle inside Jira for both agile and waterfall methodologies. 
By using Zephyr Scale you'll empower agile teams with BDD at scale with Cucumber or your chosen gherkin compatible tool for collaboration between developers, 
testers and domain experts. With our powerful FREE REST API, easily integrate CI/CD servers, DevOps and test automation tools and frameworks you already use 
(including Selenium, JUnit, Nunit, Robot, Behave, Calabash) to save time and effort. 
Zephyr Scale is used by more than 3.000 clients worldwide and is the top rated QA and Testing app for Jira.

The plugin introduces two new configuration steps for Jenkins jobs:

1. A build step to download Gherkin feature files (BDD) from Zephyr Scale
1. A post-build step to publish the test results back to Zephyr Scale

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

For the Junit integration with the post-build action use the `Zephyr Scale Output Result For JUnit` format, with the `zephyrscale_result.json` as filepath.

## Documentation

Check out the documentation for [Zephyr Scale Cloud here](https://support.smartbear.com/zephyr-scale-cloud/docs/api-and-test-automation/jenkins-integration.html) and 
for [Zephyr Scale Server/DC here](https://support.smartbear.com/zephyr-scale-server/docs/test-automation/integrations/jenkins.html).

## Support

For any issues or enquiries please get in touch with the Zephyr Scale team at SmartBear using the [support portal](https://smartbear.atlassian.net/servicedesk/customer/portals).


## Running the Zephyr Scale Jenkins plugin locally

You can try the plugin on you local machine before getting it installed on your production Jenkins instance.

After cloning this repository, these are the 2 steps you should follow:
1. Run `./setup.sh`: this step will download and setup Jenkins on your local machine
2. Run `./run.sh`: this will run Jenkins with the Zephyr Scale plugin installed
3. Browse to http://localhost:8080/jenkins to access Jenkins

## Trigger pipeline manually for forked pull requests

Eventually, CircleCi doesn't trigger the pipeline from pull requests of forked repositories. 
If that happens block us to merge the pull request. 
It's possible to trigger the pipeline manually running the following command:

```
curl --request POST \
  --url https://circleci.com/api/v2/project/gh/jenkinsci/tm4j-automation-plugin/pipeline \
  --header 'Circle-Token: {CIRCLE_TOKEN}' \
  --header 'content-type: application/json' \
  --data '{"branch":"pull/{PULL_REQUEST_NUMBER}/head"}'
```