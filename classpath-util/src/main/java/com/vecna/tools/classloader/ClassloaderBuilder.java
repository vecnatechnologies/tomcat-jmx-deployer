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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience API for building a classloader from paths, urls, and classpath resources.
 *
 * @author ogolberg@vecna.com
 */
public class ClassLoaderBuilder {
  private final ClassLoader parent;
  private final List<URL> urls = new ArrayList<>();

  /**
   * New builder with a parent classloader.
   * @param parent parent classloader
   */
  public ClassLoaderBuilder(ClassLoader parent) {
    this.parent = parent;
  }

  /**
   * New builder, uses current thread's context classloader as the parent.
   */
  public ClassLoaderBuilder() {
    this(Thread.currentThread().getContextClassLoader());
  }

  /**
   * Add a URL.
   * @param url the url to add.
   * @return <code>this</code>
   */
  public ClassLoaderBuilder addURL(URL url) {
    this.urls.add(url);
    return this;
  }

  /**
   * Add a path.
   * @param path the path.
   * @return <code>this</code>
   */
  public ClassLoaderBuilder addPath(Path path) {
    try {
      return addURL(path.toUri().toURL());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Add tools.jar from the current JDK.
   * @return <code>this</code>
   */
  public ClassLoaderBuilder addToolsJar() {
    return addPath(Paths.get(System.getProperty("java.home")).getParent().resolve("lib").resolve("tools.jar"));
  }

  /**
   * Add a resource from the parent's classpath.
   *
   * @param path path
   * @return <code>this</code>
   */
  public ClassLoaderBuilder addClassPathResource(String path) {
    return addURL(parent.getResource(path));
  }

  /**
   * @return classloader.
   */
  public ClassLoader build() {
    return new URLClassLoader(urls.toArray(new URL[urls.size()]), this.parent);
  }
}
