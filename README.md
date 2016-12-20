# Ansible Role: Jenkins CI


Installs and Configures Jenkins on RHEL 6,7
This is being designed and rewritten for offline installs, with access to limited resources on the network, for example, a maven repository.

## Requirements

ansible 2+
Network Resource: Maven repo with the necessary war and hpi's


## Role Variables

Available variables are listed below, along with default values (see `defaults/main.yml`):

    jenkins_hostname: localhost

Used to talk to the jenkins installation for configuration. Does not configure Jenkins.

    jenkins_root: /apps/jenkins

The directory which is used for installing Jenkins in a self-contained custom location

    jenkins_http_port: 8080

The HTTP port for Jenkins' web interface.

    jenkins_user: jenkins
	jenkins_group: jenkins
Default user/group that owns the jenkins installation and runs the server.    
    
    jenkins_admin_username: admin
    jenkins_admin_password: admin

Default admin account credentials which will be phased out for a AD admin group/DN

    jenkins_jar_location: /opt/jenkins-cli.jar

The location at which the `jenkins-cli.jar` jarfile will be kept. This is relative to {{ jenkins_root }}

    jenkins_plugins:
    - git
    - credentials
    - blueocean

Jenkins plugins to be installed automatically during provisioning. Plugins are accepted as an array of short-names. Version support is currently unavailable. The latest plugins and their latest dependencies are installed. Follows "atleast these" semantics. Existing plugins are not removed.

    jenkins_nexus_url: http://mirrors.jenkins.io/war-stable/latest/jenkins.war
	jenkins_plugins_nexus_url: http://updates.jenkins-ci.org/latest

Temporary urls, that will be refactored to support nexus based jenkins war and plugins installation. End user would not be expected to know exact urls, but just the maven repo url.

    jenkins_url_prefix: ""

Used for setting a URL prefix for your Jenkins installation. The option is added as `--prefix={{ jenkins_url_prefix }}` to the Jenkins initialization `java` invocation, so you can access the installation at a path like `http://www.example.com{{ jenkins_url_prefix }}`. Make sure you start the prefix with a `/` (e.g. `/jenkins`).

    jenkins_connection_delay: 5
    jenkins_connection_retries: 60

Amount of time and number of times to wait when connecting to Jenkins after initial startup, to verify that Jenkins is running. Total time to wait = `delay` * `retries`, so by default this role will wait up to 300 seconds before timing out.

    jenkins_java_options: "-Djenkins.install.runSetupWizard=false"

Extra Java options for the Jenkins launch command configured in the init file can be set with the var `jenkins_java_options`. By default the option to disable the Jenkins 2.0 setup wizard is added.

    jenkins_init_changes:
      - option: "JENKINS_ARGS"
        value: "--prefix={{ jenkins_url_prefix }}"
      - option: "JENKINS_JAVA_OPTIONS"
        value: "{{ jenkins_java_options }}"

Changes made to the Jenkins init script; the default set of changes set the configured URL prefix and add in configured Java options for Jenkins' startup. You can add other option/value pairs if you need to set other options for the Jenkins init file.

	jenkins_plugin_*
	
Additional config options per plugin. See sample playbook for usage	

## Dependencies

## Example Playbook

```
- hosts: localhost
  vars_files:
    - secrets.yml
  vars:
  - jenkins_nexus_url: http://mirrors.jenkins.io/war-stable/latest/jenkins.war
  - jenkins_plugins_nexus_url: http://updates.jenkins-ci.org/latest
  - jenkins_root: /apps/jenkins
  - jenkins_http_port: 9000
  - jenkins_user: jenkins
  - jenkins_group: jenkins
  - jenkins_plugin_hashicorp_vault:
      force: true
      url: 'http://localhost:8200'
      token: "{{ secrets_vault_token }}"
  - jenkins_plugin_ssh_slaves:
      force: true
      slaves:
        - host: localhost  
          user: kslave
          pass: "{{ secrets_kslave_password }}"
        - host: localhost
          user: jslave
          pass: "{{ lookup('vault', 'secret/jslave').password }}" 
          
  - jenkins_plugin_role_strategy:
      force: true
      journeys:
      - aoo
      - pca
      - red
  - jenkins_plugins:
    - git
    - credentials
    - blueocean
    - active-directory
    - role-strategy
    - matrix-auth
    - ldap
    - cloudbees-folder
    - github-organization-folder
    - pam-auth
    - swarm
    - ssh-slaves
    - active-directory
    - hashicorp-vault-plugin
```
## TODO

* jenkins_plugin_role_strategy should be removed from this role, and moved to a custom role that templates the role strategy.
* Input validation across the board


## License

MIT (Expat) / BSD

## Source Information

This role was initially forked from  https://github.com/geerlingguy/ansible-role-jenkins. It has undergone complete tranformation, and has been practically rewritten. 