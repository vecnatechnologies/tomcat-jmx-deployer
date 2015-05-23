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

/**
 * API to deploy apps to Tomcat.
 * @author ogolberg@vecna.com
 */
public interface TomcatJmxDeployer {
  /**
   * Deploy a webapp to Tomcat.
   * @param command context name and other info needed to deploy the application.
   * @return the status of the application at the end of deployment
   * @throws Exception of various error conditions (TODO: clean this up).
   */
  WebappStatusEnum deploy(DeployCommand command) throws Exception;
}
