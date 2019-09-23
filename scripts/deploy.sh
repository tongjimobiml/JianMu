#!/bin/bash
# -e flag causes the script to exit as soon as one command returns a non-zero exit code
# -v flag makes the shell print all lines in the script before executing them
# set -ev

deploy_path=$1
secret_key=$2
upload_token=$3

project_path="${deploy_path}/jianmu"

chmod +x "${project_path}/bin/jianmu"

nohup "${project_path}/bin/jianmu" -Dconfig.resource=deploy.conf -Dplay.http.secret.key=${secret_key} -Dupload-dataset.token=${upload_token} -Dhttp.port=55555 > /dev/null 2>&1 &
