#!/usr/bin/env bash

sh ./run.sh &

while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	echo "starting..."
	sleep 1
done


curl http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar --output jenkins-cli.jar
sleep 1
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins install-plugin git
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart

while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	echo "restarting..."
	sleep 1
done

java -jar jenkins-cli.jar -s http://localhost:8080/jenkins create-job tm4j-cucumber-jenkins-plugins-test < config.xml
cp com.adaptavist.tm4j.jenkins.Tm4jReporter.xml work/com.adaptavist.tm4j.jenkins.Tm4jReporter.xml
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart

while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	echo "restarting..."
	sleep 1
done

echo "Done"

rm jenkins-cli.jar
