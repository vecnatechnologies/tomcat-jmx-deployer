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

package com.vecna.tools.jmx.cli;

import com.vecna.tools.classloader.ClassLoaderBuilder;
import com.vecna.tools.jmx.DeployCommand;
import com.vecna.tools.jmx.TomcatJmxDeployer;
import com.vecna.tools.jmx.WebappStatusEnum;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.FileInputStream;

/**
 * Command-line tool that invokes TomcatJmxDeployer.
 */
public class TomcatJmxCli {
  private static final String CONTEXT_NAME = "context-name";
  private static final String CONTEXT_TARGET = "context-target";
  private static final String TOMCAT_ID = "tomcat-id";
  private static final String HOST = "host";
  private static final String CONTEXT_FILE = "context-file";
  private static final String HELP = "help";

  public static void main(String[] args) throws Exception {
    OptionParser parser = new OptionParser();

    parser.accepts(CONTEXT_NAME, "name of the webapp context").withRequiredArg();
    parser.accepts(CONTEXT_TARGET, "the directory where the context file needs to be placed").withOptionalArg();
    parser.accepts(TOMCAT_ID, "tomcat PID").withOptionalArg();
    parser.accepts(HOST, "tomcat host").withOptionalArg();
    parser.accepts(CONTEXT_FILE, "context file").withOptionalArg();

    parser.accepts(HELP).forHelp();

    OptionSet optionSet = null;

    try {
      optionSet = parser.parse(args);
    } catch (OptionException e) {

    }

    if (optionSet == null || optionSet.has(HELP)) {
      parser.printHelpOn(System.err);
    } else {
      DeployCommand command = new DeployCommand().setContextName((String) optionSet.valueOf(CONTEXT_NAME))
              .setTomcatId((String) optionSet.valueOf(TOMCAT_ID))
              .setHost((String) optionSet.valueOf(HOST))
              .setContextTarget((String) optionSet.valueOf(CONTEXT_TARGET));

      if (optionSet.hasArgument(CONTEXT_FILE)) {
        command.setContextResource(new FileInputStream(CONTEXT_FILE));
      } else {
        command.setContextResource(System.in);
      }

      Class<?> deployerClass =
              new ClassLoaderBuilder().addToolsJar().addClassPathResource("META-INF/runtime/")
                      .build().loadClass("com.vecna.tools.jmx.TomcatJmxDeployerImpl");

      TomcatJmxDeployer deployer = (TomcatJmxDeployer) deployerClass.newInstance();

      WebappStatusEnum status = null;

      try {
        status = deployer.deploy(command);
      } catch (Exception e) {
        System.err.println("An unexpected error occurred");
        e.printStackTrace(System.err);
        System.exit(-1);
      }

      if (status == WebappStatusEnum.STARTED) {
        System.out.printf("Successfully deployed to /%s\n", command.getContextName());
      } else {
        System.err.printf("Failed to deploy to /%s, check application logs\n", command.getContextName());
        System.exit(1);
      }
    }
  }
}
