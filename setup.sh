#!/usr/bin/env bash
set -e
server=http://localhost:8080/jenkins

echo "[==== SETUP ====] Starting Zephyr Jenkins plugin setup..."

is_running() {
	if [ 200 == $(curl -o /dev/null -s -w "%{http_code}\n" $server/jnlpJars/jenkins-cli.jar) ]
	then
		true
	else
		false
	fi
}

wait_start() {
	echo "[==== SETUP ====] Starting Jenkins..."
	while ! is_running
	do
		sleep 1
	done
}

wait_stop() {
	echo "[==== SETUP ====] Stopping Jenkins..."
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

sleep 5
java -jar jenkins-cli.jar -s $server install-plugin workflow-aggregator
java -jar jenkins-cli.jar -s $server install-plugin git
sleep 2
echo "[==== SETUP ====] Stopping Jenkins..."
java -jar jenkins-cli.jar -s $server safe-restart
wait_start

echo "[==== SETUP ====] Setting Jenkins configurations"
echo "[==== SETUP ====] Creating jobs"
java -jar jenkins-cli.jar -s $server create-job zephyr-junit-integration-example-legacy-version < setup/zephyr-junit-integration-example-legacy-version.xml
java -jar jenkins-cli.jar -s $server create-job zephyr-cucumber-calculator-example < setup/zephyr-cucumber-calculator-example.xml
java -jar jenkins-cli.jar -s $server create-job zephyr-cucumber-integration-example < setup/zephyr-cucumber-integration-example.xml
java -jar jenkins-cli.jar -s $server create-job zephyr-cucumber-integration-example-pipeline < setup/zephyr-cucumber-integration-example-pipeline.xml
java -jar jenkins-cli.jar -s $server create-job zephyr-junit-integration-example < setup/zephyr-junit-integration-example.xml

cp setup/com.adaptavist.tm4j.jenkins.extensions.configuration.Tm4jGlobalConfiguration.xml work/

echo "[==== SETUP ====] Restarting Jenkins..."
java -jar jenkins-cli.jar -s $server safe-restart
wait_start

echo "[==== SETUP ====] Shutdown Jenkins"
java -jar jenkins-cli.jar -s $server safe-shutdown

sleep 2
echo "[==== SETUP ====] Jenkins stopped"
echo "[==== SETUP ====] Setup finished"
echo "[==== SETUP ====] Execute run.sh to run Jenkins";
rm jenkins-cli.jar
