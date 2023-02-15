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
            format: 'Cucumber', 
            filePath: 'target/cucumber/*.json', 
            autoCreateTestCases: false, 
              customTestCycle: [
                name: 'Jenkins Build',
                description: 'Results from Jenkins Build', 
                jiraProjectVersion: '10001', 
                folderId: '3040527', 
                customFields: '{"number":50,"single-choice":"option1","checkbox":true,"userpicker":"5f8b5cf2ddfdcb0b8d1028bb","single-line":"a text line","datepicker":"2020-01-25","decimal":10.55,"multi-choice":["choice1","choice3"],"multi-line":"first line<br />second line"}'
              ]
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

## Configuring the Zephyr Scale Jenkins plugin using Jenkins Configuration as Code (JCasC) plugin
You can configure the plugin by following the steps below:
1. Install the JCasC plugin by visiting the "manage plugins" page and installing the Configuration as Code plugin
2. Configure your Jira instances by creating a yaml file with the following configuration
```yaml
unclassified:
  zephyr-scale:
    jiraInstances:
      - value: "server"
        serverAddress: "yourJiraServerAddress.com"
        username: "yourJiraServerUsername"
        password: "yourJiraServerPassword"
      - value: "cloud"
        jwt: "yourCloudToken"
        
```
Note: 
- For server configurations, set the `value` property to `server`, then provide the `serverAddress`, `username` and `password` 
- For cloud configurations, set the `value` property to `cloud`, then set the `jwt`. The jwt property can be generated [here](https://support.smartbear.com/zephyr-scale-cloud/docs/rest-api/generating-api-access-tokens.html)

If any of these are configured incorrectly, no instances will be created.
Note That you can configure as many instances as you want by simply adding another `jiraInstances` list entry

## Running Jenkins behind a proxy
If the Jenkins instance is running behind a proxy, please make sure that the proxy is well configured on Jenkins and that the proxy settings have been set up on JVM startup:

- Setting up Jenkins behind a proxy: https://wiki.jenkins.io/display/JENKINS/JenkinsBehindProxy
- JVM proxy configuration: https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html