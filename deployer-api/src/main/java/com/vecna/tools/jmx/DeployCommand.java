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

import java.io.InputStream;

/**
 * Deploy command.
 *
 * @author ogolberg@vecna.com
 */
public class DeployCommand {
  private String host = "localhost";
  private String contextName;
  private String contextTarget;
  private InputStream contextResource;
  private String tomcatId;

  /**
   * @param contextTarget where the context XML file should be copied; if not specified; optional
   * @return <code>this</code>
   */
  public DeployCommand setContextTarget(String contextTarget) {
    if (contextTarget != null) {
      this.contextTarget = contextTarget;
    }
    return this;
  }

  /**
   * @param host Tomcat host (default = localhost)
   * @return <code>this</code>
   */
  public DeployCommand setHost(String host) {
    if (host != null) {
      this.host = host;
    }
    return this;
  }

  /**
   * @param contextResource the stream to read context XML contents from
   * @return <code>this</code>
   */
  public DeployCommand setContextResource(InputStream contextResource) {
    if (contextResource != null) {
      this.contextResource = contextResource;
    }
    return this;
  }

  /**
   * @param contextName name of the webapp context (without /)
   * @return <code>this</code>
   */
  public DeployCommand setContextName(String contextName) {
    if (contextName != null) {
      this.contextName = contextName;
    }
    return this;
  }

  /**
   * @param tomcatId Tomcat PID; if not specified, will attempt to find Tomcat among running JVM processes
   * @return <code>this</code>
   */
  public DeployCommand setTomcatId(String tomcatId) {
    if (tomcatId != null) {
      this.tomcatId = tomcatId;
    }
    return this;
  }

  public String getHost() {
    return host;
  }

  public String getContextName() {
    return contextName;
  }

  public InputStream getContextResource() {
    return contextResource;
  }

  public String getTomcatId() {
    return tomcatId;
  }

  public String getContextTarget() {
    return contextTarget;
  }
}
