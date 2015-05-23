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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;

/**
 * Maven plugin to deploy to Tomcat through JMX.
 */
@Mojo(name = "deploy")
public class TomcatJmxDeployerMojo extends AbstractMojo {
  /**
   * Tomcat host.
   */
  @Parameter(defaultValue = "localhost")
  private String host;
  /**
   * Context name (without /).
   */
  @Parameter(required = true)
  private String contextName;
  /**
   * The directory where the context xml file needs to be placed.
   */
  @Parameter
  private String contextTarget;

  /**
   * Context xml file.
   */
  @Parameter
  private File contextResource;

  private final TomcatJmxDeployer deployer = new TomcatJmxDeployerImpl();

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    WebappStatusEnum status;

    try {
      status = this.deployer.deploy(new DeployCommand()
              .setHost(this.host)
              .setContextName(this.contextName)
              .setContextTarget(this.contextTarget)
              .setContextResource(new FileInputStream(this.contextResource)));
    } catch (Exception e) {
      throw new MojoFailureException("failed to deploy", e);
    }

    if (status == WebappStatusEnum.STARTED) {
      getLog().info("Deployed successfully to /" + this.contextName);
    } else if (status == WebappStatusEnum.TIMED_OUT) {
      throw new MojoFailureException("Timed out while waiting for the application to start");
    } else {
      throw new MojoFailureException("Failed to deploy. Check application logs.");
    }
  }
}
