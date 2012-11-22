/*
 * CARS_Version.java
 * 
 * Created on 19-Oct-2007, 11:45:16
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.version;

import javax.jcr.version.Version;

/**
 * CARS_Version
 *
 * @version $Id: CARS_Version.java,v 1.2 2009/06/22 22:36:10 weertj Exp $
 */
public class CARS_Version {

  private final Version mJCRVersion;
  
  /** CARS_Version
   * 
   * @param pVersion
   */
  public CARS_Version( final Version pVersion ) {
    mJCRVersion = pVersion;
    return;
  }

//  public void setJCRVersion( Version pVersion ) {
//    mJCRVersion = pVersion;
//    return;
//  }
  
  public Version getJCRVersion() {
    return mJCRVersion;
  }
  
}
