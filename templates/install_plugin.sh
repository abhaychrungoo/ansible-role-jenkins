#!/bin/bash

set -e

if [ $# -eq 0 ]; then
  echo "USAGE: $0 plugin1 plugin2 ..."
  exit 1
fi

plugin_dir={{ jenkins_root }}/var/lib/jenkins/plugins
file_owner={{ jenkins_user }}.{{ jenkins_group }}

mkdir -p ${plugin_dir}

installPlugin() {
  ## Test if plugin exists locally
  if [ -f ${plugin_dir}/${1}.hpi -o -f ${plugin_dir}/${1}.jpi ]; then
    echo "Skipped: $1 (already installed)"
    return 0
  fi
  ## Test if plugin exists on remote repo
  curl -I --silent {{ jenkins_plugins_nexus_url }}/${1}.hpi >/dev/null
  echo "Installing: $1"
  curl -L --fail --silent --output ${plugin_dir}/${1}.hpi  {{ jenkins_plugins_nexus_url }}/${1}.hpi
  installDependency "$1"
  return 0

}

installDependency() {

  echo "Check for missing dependecies ..."
  for f in "${plugin_dir}/$1.hpi" ; do
    # without optionals
    deps=$( unzip -p ${f} META-INF/MANIFEST.MF | tr -d '\r' | sed -e ':a;N;$!ba;s/\n //g' | grep -e "^Plugin-Dependencies: " | awk '{ print $2 }' | tr ',' '\n' | grep -v "resolution:=optional" | awk -F ':' '{ print $1 }' | tr '\n' ' ' )
    # with optionals
    # deps=$( unzip -p ${f} META-INF/MANIFEST.MF | tr -d '\r' | sed -e ':a;N;$!ba;s/\n //g' | grep -e "^Plugin-Dependencies: " | awk '{ print $2 }' | tr ',' '\n' | awk -F ':' '{ print $1 }' | tr '\n' ' ' )
    for plugin in $deps; do
      echo "found dependency  $plugin"
      installPlugin "$plugin" 
    done
  done
}

for plugin in $*
do
    installPlugin "$plugin" 
done



echo "fixing permissions"

chown ${file_owner} ${plugin_dir} -R

echo "all done"