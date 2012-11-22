/*
 * Copyright 2012 NLR - National Aerospace Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jecars.tools;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author weert
 */
public class CARS_FileClassLoader extends URLClassLoader {

   public CARS_FileClassLoader() {
     this( new URL[]{});
   }

   public CARS_FileClassLoader(URL[] urls) {
      this(urls, null);
   }

   public CARS_FileClassLoader(ClassLoader parent) {
      this(new URL[]{}, parent);
   }

   public CARS_FileClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
   }

   @Override
   public void addURL(URL url) {
      super.addURL(url);
   }

   public Class createClass( final InputStream pStream ) throws IOException {
     byte[] bytes = new byte[pStream.available()];
     int read = pStream.read(bytes);
     if (read != bytes.length) {
       return null;
     }
     return createClass(bytes);
   }

   public Class createClass(byte[] bytes) {
      Class clss = defineClass(null, bytes, 0, bytes.length);
      resolveClass(clss);
      return clss;
   }   
}
