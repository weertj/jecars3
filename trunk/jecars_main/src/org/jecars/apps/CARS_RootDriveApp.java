/*
 * Copyright 2007-2008 NLR - National Aerospace Laboratory
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
package org.jecars.apps;

import java.io.*;
import javax.jcr.Node;
import javax.jcr.Session;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;

/**
 * CARS_RootDriveApp
 *
 */
public class CARS_RootDriveApp extends CARS_DefaultInterface implements CARS_Interface {

  private boolean mReadOnly = true;
    
  /** Creates a new instance of CARS_RootDriveApp
   */
  public CARS_RootDriveApp() {
  }

  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Definitions.VERSION_ID + " $Id: CARS_RootDriveApp";
  }
  
  @Override
  public void getNodes( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception {
    CARS_DirectoryApp.gFORCEREADONLY = true;
    
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      try {
        // **** sys* nodes have all rights.
        final Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
        final File[] roots = File.listRoots();
        for( final File root : roots ) {
          final String driveLetter = root.getPath().substring(0,1);
          if ("/".equals( driveLetter )) {
            if (!sysParentNode.hasNode( "slash" )) {
              final Node dl = sysParentNode.addNode( "slash", "jecars:localstorage" );
              dl.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_DirectoryApp" );
              dl.setProperty( "jecars:StorageDirectory", "(ABS)/" );
            }            
          } else if (!sysParentNode.hasNode( driveLetter )) {
            final Node dl = sysParentNode.addNode( driveLetter, "jecars:localstorage" );
            dl.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_DirectoryApp" );
            dl.setProperty( "jecars:StorageDirectory", "(ABS)" + root.getPath() );
          }
  //          System.out.println("root - - " + root );
        }
      } finally {
        appSession.save();
      }
    }
    return;
  }

  
}
