#!groovy

import jenkins.model.*
import hudson.model.*
import hudson.slaves.*
import hudson.plugins.sshslaves.*
import java.util.ArrayList;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;


def allSlaves = new groovy.json.JsonSlurper().parseText ''' {{ jenkins_plugin_ssh_slaves.slaves |to_json }} ''' 

List <Node> releaseableSlaves = Jenkins.instance.getNodes()



for (Object jenkinsSlave: allSlaves) {

  def slaveName = jenkinsSlave.user + "-" + jenkinsSlave.host
  def slaveUser = jenkinsSlave.user
  def slavePass = jenkinsSlave.pass
  def slaveHost = jenkinsSlave.host

  Node namedNode = Jenkins.instance.getNode(slaveName)
  DumbSlave desiredSlave = null;
  
  if (namedNode != null && namedNode instanceof DumbSlave) {
	  desiredSlave = (DumbSlave) namedNode;
  }
  
  
 if ( desiredSlave == null)	{
	
  List<Entry> env = new ArrayList<Entry>();
  env.add(new Entry("M2_HOME_programmatic","I dont know"))
  env.add(new Entry("JAVA","global or local?"))
  EnvironmentVariablesNodeProperty envPro = new EnvironmentVariablesNodeProperty(env);
  Slave slave = new DumbSlave(
                    slaveName,"Desc: " + slaveName,
                    "/home/" + slaveUser,
                    "4",
                    Node.Mode.NORMAL,
                    "local, unix, slow",
                    new SSHLauncher(slaveHost ,22,slaveUser,"${slavePass}","","","","",""),
                    new RetentionStrategy.Always(),
                    new LinkedList())
  slave.getNodeProperties().add(envPro)
  Jenkins.instance.addNode(slave) 
} else {
	desiredSlave.setLauncher( new SSHLauncher(slaveHost ,22,slaveUser,"${slavePass}","","","","",""))
	Jenkins.instance.updateNode(desiredSlave)
	releaseableSlaves.remove(desiredSlave)
}

for (Node rslave : releaseableSlaves) {
	Jenkins.instance.removeNode(rslave)
}
Jenkins.instance.save()
}