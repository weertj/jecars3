/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
  private static final List<String> ALLOWED_JAVA_CLASSES = new ArrayList<String>( 8 );

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
  
  /** isExportPathAllowed
   * 
   * @param pPath
   * @return 
   */
  static public boolean isExportPathAllowed( final String pPath ) {
    return ALLOWED_EXPORT_PATH.contains( pPath );
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
  
  /** isExportPathAllowed
   * 
   * @param pClassPath 
   * @return 
   */
  static public boolean isJavaClassAllowed( final String pClassPath ) {
    return ALLOWED_JAVA_CLASSES.contains( pClassPath );
  }

  /** CARS_Security
   * 
   */
  private CARS_Security() {
  }
  
}
