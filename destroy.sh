#!/usr/bin/env bash

set -e

cf delete-job get-service-details-scheduled-job
cf delete-service scheduler-for-pcf
cf delete get-service-details-task
