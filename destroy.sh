#!/usr/bin/env bash

set -e

cf delete-job get-service-inventory-scheduled-job
cf delete-service scheduler-for-pcf
cf delete get-service-inventory-task
