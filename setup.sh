#!/usr/bin/env bash
set -e

if [ 200 == $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
then
	curl http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar --output jenkins-cli.jar
	sleep 2
	java -jar jenkins-cli.jar -s http://localhost:8080/jenkins safe-shutdown
	sleep 2
fi

rm -r work/
mvn clean

sh ./run.sh &

echo "starting..."
while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	sleep 1
done

curl http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar --output jenkins-cli.jar
echo "wait..."
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins safe-restart

echo "stoping..."
while [ 200 == $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	sleep 1
done

echo "starting..."
while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	sleep 1
done

java -jar jenkins-cli.jar -s http://localhost:8080/jenkins install-plugin git
sleep 1
echo "restarting..."
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart

while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	sleep 1
done

java -jar jenkins-cli.jar -s http://localhost:8080/jenkins create-job tm4j-cucumber-jenkins-plugins-test < config.xml
cp com.adaptavist.tm4j.jenkins.Tm4jReporter.xml work/com.adaptavist.tm4j.jenkins.Tm4jReporter.xml
echo "restarting..."
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins restart

while [ 200 != $(curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8080/jenkins/jnlpJars/jenkins-cli.jar) ]
do
	sleep 1
done

echo "Done"
java -jar jenkins-cli.jar -s http://localhost:8080/jenkins safe-shutdown

sleep 2
echo "Jenkins stoped"
echo "Setup finished"
echo "Execute run.sh to run Jenkins";
#rm jenkins-cli.jar
