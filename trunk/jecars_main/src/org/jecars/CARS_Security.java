/*
 * Copyright 2014 NLR - National Aerospace Laboratory
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
package org.jecars;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author weert
 */
public class CARS_Security {

  private static final Logger       LOG                 = Logger.getLogger(CARS_Security.class.getName());  
  private static final List<String> ALLOWED_EXPORT_PATH = new ArrayList<String>( 8 );
  private static       boolean      ALWAYS_ALLOW_EXPORT_PATH = true;
  private static final List<String> ALLOWED_JAVA_CLASSES = new ArrayList<String>( 8 );
  private static       boolean      ALWAYS_ALLOW_JAVA_CLASSES = true;

  /** addAllowedExportPath
   * 
   * @param pPath 
   */
  static public void addAllowedExportPath( final String pPath ) {
    if (!ALLOWED_EXPORT_PATH.contains( pPath )) {
      ALLOWED_EXPORT_PATH.add( pPath );
      LOG.log( Level.INFO, "Add SECURITY_ALLOWED_EXPORT_PATH {0}", pPath);
    }
    return;
  }
  
  /** exportPathAllowed
   * 
   * @param pAllowed 
   */
  static public void exportPathAllowed( final boolean pAllowed ) {
    ALWAYS_ALLOW_EXPORT_PATH = pAllowed;
    return;
  }
  
  /** isExportPathAllowed
   * 
   * @param pPath
   * @return 
   */
  static public boolean isExportPathAllowed( final String pPath ) {
    if (!ALWAYS_ALLOW_EXPORT_PATH) {
      return ALLOWED_EXPORT_PATH.contains( pPath );
    }
    return true;
  }

  /** addAllowedJavaClass
   * 
   * @param pClassPath 
   */
  static public void addAllowedJavaClass( final String pClassPath ) {
    if (!ALLOWED_JAVA_CLASSES.contains( pClassPath )) {
      ALLOWED_JAVA_CLASSES.add( pClassPath );
      LOG.log( Level.INFO, "Add ALLOWED_JAVA_CLASS {0}", pClassPath);
    }
    return;
  }
  
  /** javaClassesAllowed
   * 
   * @param pAllowed 
   */
  static public void javaClassesAllowed( final boolean pAllowed ) {
    ALWAYS_ALLOW_JAVA_CLASSES = pAllowed;
    return;
  }

  
  /** isExportPathAllowed
   * 
   * @param pClassPath 
   * @return 
   */
  static public boolean isJavaClassAllowed( final String pClassPath ) {
    if (!ALWAYS_ALLOW_JAVA_CLASSES) {
      return ALLOWED_JAVA_CLASSES.contains( pClassPath );
    }
    return true;
  }

  /** CARS_Security
   * 
   */
  private CARS_Security() {
  }
  
}
