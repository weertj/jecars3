/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;

/**
 * CARS_SystemsApp

 */
public class CARS_SystemsApp extends CARS_DefaultInterface {

  /** Creates a new instance of CARS_SystemsApp
   */
  public CARS_SystemsApp() {
    super();
  }
 
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": " + CARS_Definitions.PRODUCTNAME + " version=" + CARS_Definitions.VERSION_ID + " CARS_SystemsApp";
  }

  /** getNodes
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws java.lang.Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws RepositoryException {

    if (pParentNode.isNodeType( "jecars:CARS_Interface" )) {
      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {
        try {
          final Node systems = appSession.getNode( pParentNode.getPath() );
          final String computername= InetAddress.getLocalHost().getHostName();
          if (!systems.hasNode( computername )) {
            Node cn = systems.addNode( computername, "jecars:RES_System" );
            cn.addNode( "CPU", "jecars:RES_CPU" );
          }
          Node system = systems.getNode( computername );
          Node cpu    = system.getNode( "CPU" );
          system.setProperty( "jecars:MainMemory", Runtime.getRuntime().maxMemory() );
          for( int i=Runtime.getRuntime().availableProcessors()-1; i>=0; i-- ) {
            if (!cpu.hasNode( "Core_" + i )) {
              cpu.addNode( "Core_" + i, "jecars:RES_Core" );
            }
          }
        } catch( Exception e ) {
          gLog.log( Level.WARNING, e.getMessage(), e );
        } finally {
          appSession.save();
        }
      }
    } else {
    }
    return;
  }  
    
}
