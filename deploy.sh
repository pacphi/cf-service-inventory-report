#!/usr/bin/env bash

set -e

if [ $# -ne 1 ]; then
    echo "Usage: ./deploy.sh {pivotal cloud foundry api endpoint}"
    exit 1
fi

cf login -a $1

cf push get-service-inventory-task -p ./build/libs/cf-service-inventory-report-0.1-SNAPSHOT.jar -m 1G --no-start
cf set-env get-service-inventory-task SPRING_PROFILES_ACTIVE jdbc
cf start get-service-inventory-task
