#!/usr/bin/env bash

set -e

cf stop get-service-inventory-task
cf delete get-service-inventory-task
