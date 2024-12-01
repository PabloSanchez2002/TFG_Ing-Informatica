#!/bin/bash

# Parameters
ip=$1
keypath=$2
user=$3
mode=$4
repoUrl=$5
setupYamlPath=$6

# Construct the ansible-playbook command
ansible_command="ansible-playbook -i ${ip}, --private-key=${keypath} -e \"ansible_user=${user} role=${mode} gitRepo=${repoUrl}\" ${setupYamlPath}"


# Execute the ansible-playbook command
echo "Executing: ${ansible_command}"
eval ${ansible_command}
exit $?
