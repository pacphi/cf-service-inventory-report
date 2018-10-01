#!/usr/bin/env bash

set -x

export APP_NAME=cf-service-inventory-report

cf app ${APP_NAME} --guid

if [ $? -eq 0 ]; then
	cf stop $APP_NAME
	cf unbind-service $APP_NAME $APP_NAME-secrets
	cf delete-service $APP_NAME-secrets -f
	cf delete $APP_NAME -r -f
else
    echo "$APP_NMAE does not exist"
fi
