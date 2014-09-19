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
import java.net.UnknownHostException;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.par.IPAR_Balancer;
import org.jecars.par.IPAR_Core;
import org.jecars.par.IPAR_Execute;
import org.jecars.par.IPAR_ResourceWish;
import org.jecars.par.PAR_Balancer;

/**
 * CARS_SystemsApp

 */
public class CARS_SystemsApp extends CARS_DefaultInterface {

  static private InetAddress LOCALHOST = null;
  
  static {
    try {
      LOCALHOST = InetAddress.getLocalHost();
    } catch( UnknownHostException e ) {
      e.printStackTrace();
    }
  }
  
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
          {
            final Node systems = appSession.getNode( pParentNode.getPath() );
            final String computername= LOCALHOST.getHostName();
            if (!systems.hasNode( computername )) {
              Node cn = systems.addNode( computername, "jecars:RES_System" );
              cn.addMixin( "jecars:mixin_unstructured" );
              cn.setProperty( "jecars:Title", computername );
              Node cpu = cn.addNode( "CPU", "jecars:RES_CPU" );
              cpu.addMixin( "jecars:mixin_unstructured" );
              cpu.setProperty( "jecars:Title", "CPU" );
            }
            Node system = systems.getNode( computername );
            Node cpu    = system.getNode( "CPU" );
            system.setProperty( "jecars:MainMemory", Runtime.getRuntime().maxMemory() );
            for( int i=Runtime.getRuntime().availableProcessors()-1; i>=0; i-- ) {
              if (!cpu.hasNode( "Core_" + i )) {
                Node core = cpu.addNode( "Core_" + i, "jecars:RES_Core" );
                core.setProperty( "jecars:Title", "Core_" + i );
                core.addMixin( "jecars:mixin_unstructured" );
              }
            }
          }
          
          {
            final Node systems = appSession.getNode( pParentNode.getPath() );
            // **** Check the multi jecars
            for( CARS_Factory.MultiJeCARS mj : CARS_Factory.gMultiJeCARS ) {
              if (!systems.hasNode( mj.mServer )) {
                Node cn = systems.addNode( mj.mServer, "jecars:RES_System" );
                cn.addMixin( "jecars:mixin_unstructured" );
                cn.setProperty( "jecars:Title", mj.mServer );
                cn.setProperty( "jecars:JeCARSURL", mj.mJeCARSURL );
                Node cpu = cn.addNode( "CPU", "jecars:RES_CPU" );
                cpu.addMixin( "jecars:mixin_unstructured" );
                cpu.setProperty( "jecars:Title", "CPU" );              
              }              
              Node system = systems.getNode( mj.mServer );
              Node cpu    = system.getNode( "CPU" );
              system.setProperty( "jecars:MainMemory", Runtime.getRuntime().maxMemory() );
              for( int i=mj.mNumberOfCores-1; i>=0; i-- ) {
                if (!cpu.hasNode( "Core_" + i )) {
                  Node core = cpu.addNode( "Core_" + i, "jecars:RES_Core" );
                  core.setProperty( "jecars:Title", "Core_" + i );
                  core.addMixin( "jecars:mixin_unstructured" );
                }
              }              
              
            }
            
          }
        } catch( Exception e ) {
          gLog.log( Level.WARNING, e.getMessage(), e );
        } finally {
          appSession.save();
        }
      }
    } else if (pParentNode.isNodeType( "jecars:RES_System" )) {
      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {

        // ***********************************************************************
        // **** jecars:RES_System
        final IPAR_Balancer bal = PAR_Balancer.BALANCER();
        for( IPAR_ResourceWish rw : bal.currentResources() ) {
          Node rwn = pParentNode.addNode( rw.wishID(), "jecars:unstructured" );
          rwn.setProperty( "jecars:ExpectedLoad",   rw.expectedLoad() );
          rwn.setProperty( "jecars:MaxNumberOfRunsPerSystem", rw.maxNumberOfRunsPerSystem() );
          rwn.setProperty( "jecars:NumberOfCores",  rw.numberOfCores() );
          rwn.setProperty( "jecars:ResourceID",     rw.resourceID() );
          rwn.setProperty( "jecars:RunOnSystem",    rw.runOnSystem() );
          rwn.setProperty( "jecars:RunOnCPU",       rw.runOnCPU() );
          rwn.setProperty( "jecars:RunOnCore",      rw.runOnCore() );
        }
      }      
    } else if (pParentNode.isNodeType( "jecars:RES_Core" )) {
      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {

        // ***********************************************************************
        // **** jecars:RES_Core
        final Node cpu = pParentNode.getParent();
        final Node sys = cpu.getParent();
        final IPAR_Core core = PAR_Balancer.BALANCER().
                system( sys.getProperty( "jecars:Title" ).getString() ).
                  cpu( cpu.getProperty( "jecars:Title" ).getString() ).
                    core( pParentNode.getName() );
        final Node stateNode = pParentNode.addNode( "Status", "jecars:unstructured" );
        stateNode.setProperty( "jecars:State",          core.coreType().name() );
        stateNode.setProperty( "jecars:CurrentRunning", core.currentRunning() );
        stateNode.setProperty( "jecars:ReadyRunning",   core.readyRunning() );
        stateNode.setProperty( "jecars:MaxLoad",        core.maxLoad() );
        stateNode.setProperty( "jecars:CurrentLoad",    core.currentLoad() );
        stateNode.setProperty( "jecars:ExpectedLoad",   core.expectedLoad() );

        // **** queued Execs
        for( Object o : core.queuedExecs() ) {
          IPAR_Execute exec = (IPAR_Execute)o;
          final Node runNode = pParentNode.addNode( "Queued_" + exec.id() );
          runNode.setProperty( "jecars:ToolRun", exec.toolRun().name() );
          final IPAR_ResourceWish rw = exec.toolRun().resourceWish();
          runNode.setProperty( "jecars:ExpectedLoad",   rw.expectedLoad() );
          runNode.setProperty( "jecars:MaxNumberOfRunsPerSystem", rw.maxNumberOfRunsPerSystem() );
          runNode.setProperty( "jecars:NumberOfCores",  rw.numberOfCores() );
          runNode.setProperty( "jecars:ResourceID",     rw.resourceID() );
          runNode.setProperty( "jecars:RunOnSystem",    rw.runOnSystem() );
          runNode.setProperty( "jecars:RunOnCPU",       rw.runOnCPU() );
          runNode.setProperty( "jecars:RunOnCore",      rw.runOnCore() );
        }

        // **** running Execs
        for( Object o : core.runningExecs() ) {
          IPAR_Execute exec = (IPAR_Execute)o;
          final Node runNode = pParentNode.addNode( "Running_" + exec.id() );
          runNode.setProperty( "jecars:ToolRun", exec.toolRun().name() );
          final IPAR_ResourceWish rw = exec.toolRun().resourceWish();
          runNode.setProperty( "jecars:ExpectedLoad",   rw.expectedLoad() );
          runNode.setProperty( "jecars:MaxNumberOfRunsPerSystem", rw.maxNumberOfRunsPerSystem() );
          runNode.setProperty( "jecars:NumberOfCores",  rw.numberOfCores() );
          runNode.setProperty( "jecars:ResourceID",     rw.resourceID() );
          runNode.setProperty( "jecars:RunOnSystem",    rw.runOnSystem() );
          runNode.setProperty( "jecars:RunOnCPU",       rw.runOnCPU() );
          runNode.setProperty( "jecars:RunOnCore",      rw.runOnCore() );
        }
      }      
    }
    
    return;
  }  
    
}
