#!groovy

import jenkins.model.*
import com.datapipe.jenkins.vault.*

def vaultPlugin = Jenkins.getInstance().getDescriptor("com.datapipe.jenkins.vault.VaultBuildWrapper")

vaultPlugin.setVaultUrl("{{ jenkins_plugin_hashicorp_vault.url }}");
vaultPlugin.setAuthToken("{{ jenkins_plugin_hashicorp_vault.token }}");

vaultPlugin.save();

Jenkins.instance.save()
