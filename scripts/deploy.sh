#!/bin/bash
# -e flag causes the script to exit as soon as one command returns a non-zero exit code
# -v flag makes the shell print all lines in the script before executing them
# set -ev

deploy_path=$1
secret_key=$2
upload_token=$3
md5_hash=$4

project_path="${deploy_path}/jianmu"
artifact_name="`find ${deploy_path} -maxdepth 1 -type f -name 'jianmu*.tgz'`"
echo "> Find new distribution \"${artifact_name}\"."

# 1. ********** verify the MD5 **********
# if MD5 does not match, then exit with code 1 and stop deploy
if ! echo "${md5_hash}  ${artifact_name}" | md5sum --status -c -; then
	echo "> The hash of travis-constructed file is inconsistent with that of transmitted."
	exit 1
fi
echo "> MD5 hash values match, start deploying..."

# 2. ********** clean the old distribution **********
# I. stop old distribution process
pid_file="${project_path}/RUNNING_PID"
if [ -f ${pid_file} ]; then
  echo "> Current JianMu running at pid $(cat ${pid_file}), it will be killed and then deploy a new distribution."
  kill $(cat "${pid_file}")
else
  echo "> JianMu not running."
fi
# II. save the old log at another place
test -e "${project_path}/logs/" && mv "${project_path}/logs/" "/log/jianmu_$(date +"%Y%m%d_%H%M%S")"
echo "> Dump logs."
# III. delete old distribution
rm -rf "${project_path}"
echo "> Delete old distribution."

# 3. ********** deploy a new distribution **********
# I. decompress the artifact
echo "> Decompressing new distribution..."
tar -C "${deploy_path}" -xzf ${artifact_name}
dist_name=$(basename ${artifact_name} .tgz)
mv "${deploy_path}/${dist_name}" ${project_path}
rm ${artifact_name}
# II. start new process
echo "> Start new process..."
chmod +x "${project_path}/bin/jianmu"
nohup "${project_path}/bin/jianmu" -Dconfig.resource=deploy.conf -Dplay.http.secret.key=${secret_key} -Dupload-dataset.token=${upload_token} -Dhttp.port=8080 > /dev/null 2>&1 &

echo "> All deploy procedures finish."
