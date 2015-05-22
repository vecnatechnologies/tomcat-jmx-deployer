/**
 * Copyright 2014 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.vecna.tools.jmx;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Deploys a webapp to Tomcat by copying context XML and monitors the deployment through local JMX. Oracle JVM only.
 *
 * Designed to deploy applications <em>on the same host</em> without the security headache that comes with Tomcat Manager.
 *
 * @author ogolberg@vecna.com
 */
public class TomcatJmxDeployer {
  private static final String CONNECTOR_PROP = "com.sun.management.jmxremote.localConnectorAddress";
  private static final String TOMCAT_CLASS = "org.apache.catalina.startup.Bootstrap";
  private static final String WEBAPP_MBEAN = "Catalina:j2eeType=WebModule,name=//%s/%s,J2EEApplication=none,J2EEServer=none";
  private static final String DEPLOYER_MBEAN = "Catalina:type=Deployer,host=localhost";

  private String host = "localhost";
  private String contextName;
  private InputStream contextResource;
  private String tomcatId;
  private int pollResolution = 250;
  private int timeout = 600000;

  private VirtualMachineDescriptor findTomcat() {
    List<VirtualMachineDescriptor> descriptorList = VirtualMachine.list();

    for (VirtualMachineDescriptor vmdesc : descriptorList) {
      if (vmdesc.displayName().startsWith(TOMCAT_CLASS)) {
        return vmdesc;
      }
    }

    throw new IllegalStateException("cannot find any Tomcat instances running under the current user");
  }

  private String getAgentPath(VirtualMachine vm) throws Exception {
    return vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
  }

  private String getJmxAddress(VirtualMachine vm) throws Exception {
    String address = vm.getAgentProperties().getProperty(CONNECTOR_PROP);

    if (address == null) {
      vm.loadAgent(getAgentPath(vm));
      address = vm.getAgentProperties().getProperty(CONNECTOR_PROP);

      if (address == null) {
        throw new IllegalStateException("loaded the agent but still no address!");
      }
    }

    return address;
  }

  private VirtualMachine attachToTomcatJvm() throws Exception {
    String id = this.tomcatId;

    if (id == null) {
      id = findTomcat().id();
    }

    return VirtualMachine.attach(id);
  }

  private WebappStatusEnum waitTillDeployed(MBeanServerConnection connection) throws Exception {
    final ObjectName mbean = new ObjectName(String.format(WEBAPP_MBEAN, this.host, this.contextName));

    for (int i = 0; i < this.timeout / this.pollResolution; i++) {

      try {
        Object state = connection.getAttribute(mbean, "stateName");

        if (state instanceof String) {
          try {
            return WebappStatusEnum.valueOf((String) state);
          } catch (IllegalArgumentException e) {

          }
        }
      } catch (InstanceNotFoundException e) {

      }

      Thread.sleep(this.pollResolution);
    }

    return WebappStatusEnum.TIMED_OUT;
  }

  private Path findContextDirectory(MBeanServerConnection connection) throws Exception {
    String base = (String) connection.getAttribute(new ObjectName(String.format(DEPLOYER_MBEAN, this.host)), "configBaseName");

    return Paths.get(base);
  }

  private void copyContextFile(Path path) throws Exception {
    Files.copy(this.contextResource, path.resolve(this.contextName + ".xml"));
  }

  /**
   * Deploy the webapp.
   * @return status of the application or timeout
   * @throws Exception as a result of various error conditions (TODO: needs cleanup)
   */
  public WebappStatusEnum deploy() throws Exception {
    final VirtualMachine vm = attachToTomcatJvm();

    try {
      String address = getJmxAddress(vm);
      JMXServiceURL jmxUrl = new JMXServiceURL(address);

      try (JMXConnector connector = JMXConnectorFactory.connect(jmxUrl)) {

        final MBeanServerConnection connection = connector.getMBeanServerConnection();

        copyContextFile(findContextDirectory(connection));
        return waitTillDeployed(connection);
      }

    } finally {
      vm.detach();
    }

  }

  /**
   * @param host Tomcat host (default = localhost)
   * @return <code>this</code>
   */
  public TomcatJmxDeployer setHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * @param contextResource the stream to read context XML contents from
   * @return <code>this</code>
   */
  public TomcatJmxDeployer setContextResource(InputStream contextResource) {
    this.contextResource = contextResource;
    return this;
  }

  /**
   * @param contextName name of the webapp context (without /)
   * @return <code>this</code>
   */
  public TomcatJmxDeployer setContextName(String contextName) {
    this.contextName = contextName;
    return this;
  }

  /**
   * @param tomcatId Tomcat PID; if not specified, will attempt to find Tomcat among running JVM processes
   * @return <code>this</code>
   */
  public TomcatJmxDeployer setTomcatId(String tomcatId) {
    this.tomcatId = tomcatId;
    return this;
  }
}