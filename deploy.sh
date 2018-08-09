#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
    echo "Usage: ./deploy.sh {pivotal cloud foundry api endpoint}"
    exit 1
fi

cf login -a $1

cf push get-service-inventory-task --no-route --health-check-type none -p ./build/libs/cf-service-inventory-report-0.1-SNAPSHOT.jar -m 1G --no-start
cf set-env get-service-inventory-task SPRING_PROFILES_ACTIVE jdbc
cf start get-service-inventory-task
cf run-task get-service-inventory-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf create-service scheduler-for-pcf standard get-service-inventory-job
cf bind-service get-service-inventory-task get-service-inventory-job
cf create-job get-service-inventory-task get-service-inventory-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf run-job get-service-inventory-scheduled-job
cf schedule-job get-service-inventory-scheduled-job "0 8 ? * * "
