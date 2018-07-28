#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
    echo "Usage: ./deploy.sh {pivotal cloud foundry api endpoint}"
    exit 1
fi

cf login -a $1

cf push get-service-details-task --no-route --health-check-type none -p ./build/libs/cf-get-service-details-0.0.1-SNAPSHOT.jar -m 1G --no-start
cf set-env get-service-details-task SPRING_PROFILES_ACTIVE jdbc
cf start get-service-details-task
cf run-task get-service-details-task ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf create-service scheduler-for-pcf standard get-service-details-job
cf bind-service get-service-details-task get-service-details-job
cf create-job get-service-details-task get-service-details-scheduled-job ".java-buildpack/open_jdk_jre/bin/java org.springframework.boot.loader.JarLauncher"
cf run-job get-service-details-scheduled-job
cf schedule-job get-service-details-scheduled-job "0 8 ? * * "
