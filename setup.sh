#!/usr/bin/env bash
wget http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar
sleep 1
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins install-plugin git
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart
echo "restarting..."
sleep 5
echo "restarting..."
sleep 5
echo "restarting..."
sleep 5
echo "restarting..."
sleep 5
echo "restarting..."
sleep 5
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins create-job tm4j-cucumber-jenkins-plugins-test < config.xml
cp com.adaptavist.tm4j.jenkins.Tm4jReporter.xml work/com.adaptavist.tm4j.jenkins.Tm4jReporter.xml
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart
rm jenkins-cli.jar