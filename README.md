# TM4J Jenkins Plugin

A plugin for Jenkins built for integrating automated tests with [Test Management for Jira Server](https://marketplace.atlassian.com/apps/1213259/test-management-for-jira?hosting=server&tab=overview) (TM4J).  

The plugin introduces two new configuration steps for Jenkins jobs:
1) A build step to export Gherkin feature files (BDD) from TM4J
1) A post-build step to import the test results back to TM4J

These two configuration steps depends on a global configuration (Manage Jenkins > Configure System)
where the user can add/configure different Jira instances to be used on each build step.

## Support

For any issues or enquiries please get in touch with the Test Management for Jira team at Adaptavist using the [support portal](https://productsupport.adaptavist.com/servicedesk/customer/portal/27).
