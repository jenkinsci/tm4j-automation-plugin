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
	echo "Starting..."
	while ! is_running 
	do
		sleep 1
	done
}

wait_stop() { 
	echo "Stoping..."
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

sleep 20
java -jar jenkins-cli.jar -s $server install-plugin workflow-aggregator
java -jar jenkins-cli.jar -s $server install-plugin git
sleep 2
echo "Stoping..."
java -jar jenkins-cli.jar -s $server safe-restart
wait_start

echo "Setting Jenkins configurations"
java -jar jenkins-cli.jar -s $server create-job tm4j-cucumber < setup/config_cucumber.xml
echo "Creating a cucumber job"
java -jar jenkins-cli.jar -s $server create-job tm4j-junit < setup/config_junit.xml
echo "Creating a pipeline job for cucumber"
java -jar jenkins-cli.jar -s $server create-job tm4j-cucumber-pipeline < setup/config_cucumber_pipeline.xml
echo "Creating a junit job"
cp setup/com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration.xml work/
echo "Restarting..."
java -jar jenkins-cli.jar -s $server safe-restart
wait_start

echo "Shutdown"
java -jar jenkins-cli.jar -s $server safe-shutdown

sleep 2
echo "Jenkins stoped"
echo "Setup finished"
echo "Execute run.sh to run Jenkins";
rm jenkins-cli.jar
