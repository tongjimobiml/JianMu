#!/bin/bash

deploy_path=$1
project_path="${deploy_path}/jianmu"

pid_file="${project_path}/RUNNING_PID"
if [ -f ${pid_file} ]; then
  echo "Current JianMu running at pid $(cat ${pid_file}), it will be killed and then deploy a new distribution."
  kill $(cat "${pid_file}")
else
  echo "JianMu not running"
fi

# save log at another place
test -e "${project_path}/logs/" && mv "${project_path}/logs/" "/log/jianmu_$(date +"%Y%m%d_%H%M%S")"

# delete old distribution
rm -rf "${project_path}"
