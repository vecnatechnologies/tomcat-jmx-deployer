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

package com.vecna.tools.classloader;

import junit.framework.TestCase;

/**
 * Tests for ClassLoaderBuilder.
 *
 * @author ogolberg@vecna.com
 */
public class ClassloaderBuilderTest extends TestCase {
  /**
   * Tests {@link ClassLoaderBuilder#addToolsJar()}
   */
  public void testAddToolsJar() {
    ClassLoader loader = new ClassLoaderBuilder().addToolsJar().build();

    try {
      loader.loadClass("com.sun.tools.attach.VirtualMachine");
    } catch (ClassNotFoundException e) {
      fail("class not found - tools.jar is not on the classpath");
    }
  }
}
