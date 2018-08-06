#!/usr/bin/env bash
set -e
server=http://localhost:8080/jenkins

is_running() { 
	if [ 200 == $(curl -o /dev/null -s -w "%{http_code}\n" $server/jnlpJars/jenkins-cli.jar) ] 
	then
		true
	else 
		false
	fi
}

wait_start() { 
	echo "starting..."
	while ! is_running 
	do
		sleep 1
	done
}

wait_stop() { 
	echo "stoping..."
	while is_running  
	do
		sleep 1
	done
}

if is_running
then
	curl $server/jnlpJars/jenkins-cli.jar --output jenkins-cli.jar
	sleep 2
	java -jar jenkins-cli.jar -s $server safe-shutdown
	sleep 2
fi

rm -rf work/
sh ./run.sh &

wait_start
curl $server/jnlpJars/jenkins-cli.jar --output jenkins-cli.jar
java -jar jenkins-cli.jar -s $server safe-restart
wait_stop
wait_start

java -jar jenkins-cli.jar -s $server install-plugin git
sleep 1
echo "stoping..."
java -jar jenkins-cli.jar -s $server restart
wait_start

echo "setting jenkings configurations"
java -jar jenkins-cli.jar -s $server create-job tm4j-cucumber < config_cucumber.xml
echo "creating a cucumber job"
java -jar jenkins-cli.jar -s $server create-job tm4j-junit < config_junit.xml
echo "creating a junit job"
cp com.adaptavist.tm4j.jenkins.Tm4jReporter.xml work/com.adaptavist.tm4j.jenkins.Tm4jReporter.xml
echo "restarting..."
java -jar jenkins-cli.jar -s $server restart
wait_start

echo "Shutdown"
java -jar jenkins-cli.jar -s $server safe-shutdown
sleep 2
echo "Jenkins stoped"
echo "Setup finished"
echo "Execute run.sh to run Jenkins";
rm jenkins-cli.jar
