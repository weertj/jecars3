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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.par.IPAR_Balancer;
import org.jecars.par.IPAR_Core;
import org.jecars.par.IPAR_Execute;
import org.jecars.par.IPAR_ResourceWish;
import org.jecars.par.IPAR_ToolRun;
import org.jecars.par.PAR_Balancer;

/**
 * CARS_SystemsApp

 */
public class CARS_SystemsApp extends CARS_DefaultInterface {

  static private InetAddress LOCALHOST = null;
  
  static private final boolean SPECTRE = false;
  
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
                core.addNode( "queued", "jecars:unstructured" );
                core.addNode( "running", "jecars:unstructured" );
                core.addNode( "finished", "jecars:unstructured" );
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
                  core.addNode( "queued", "jecars:unstructured" );
                  core.addNode( "running", "jecars:unstructured" );
                  core.addNode( "finished", "jecars:unstructured" );
//                  core.addNode( "WorkflowQueued", "jecars:unstructured" );
//                  core.addNode( "WorkflowRunning", "jecars:unstructured" );
//                  core.addNode( "WorkflowFinished", "jecars:unstructured" );
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
        synchronized( core ) {
          final Node stateNode = pParentNode.addNode( "Status", "jecars:unstructured" );
          stateNode.setProperty( "jecars:State",          core.coreType().name() );
          stateNode.setProperty( "jecars:CurrentRunning", core.currentRunning() );
          stateNode.setProperty( "jecars:ReadyRunning",   core.readyRunning() );
          stateNode.setProperty( "jecars:MaxLoad",        core.maxLoad() );
          stateNode.setProperty( "jecars:CurrentLoad",    core.currentLoad() );
          stateNode.setProperty( "jecars:ExpectedLoad",   core.expectedLoad() );

          // **** queued Execs
          final List tbr = new ArrayList(8);
          for( Object o : core.queuedExecs() ) {
            final IPAR_Execute exec = (IPAR_Execute)o;
            final Node runNode = pParentNode.getNode( "queued" ).addNode( exec.id() );
            final IPAR_ToolRun trun = exec.toolRun();
            if (jecarsSpecials( appSession, runNode, trun.path() )) {
              tbr.add(o);
              runNode.remove();
            } else {
              runNode.setProperty( "jecars:ToolRun",  trun.name() );
              runNode.setProperty( "jecars:ToolPath", trun.path() );
              runNode.setProperty( "jecars:Created",  trun.created() );          
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
          core.removeQueuedExecs( tbr );
          tbr.clear();

          // **** running Execs
          for( Object o : core.runningExecs() ) {
            final IPAR_Execute exec = (IPAR_Execute)o;
            final Node runNode = pParentNode.getNode( "running" ).addNode( exec.id() );
            final IPAR_ToolRun trun = exec.toolRun();
            if (jecarsSpecials( appSession, runNode, trun.path() )) {
              tbr.add( o );
              runNode.remove();
            } else {
              runNode.setProperty( "jecars:ToolRun",  trun.name() );
              runNode.setProperty( "jecars:ToolPath", trun.path() );
              runNode.setProperty( "jecars:Created",  trun.created() );          
              runNode.setProperty( "jecars:Started",  trun.started() );          
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
          core.removeRunningExecs( tbr );
          tbr.clear();

          // **** finished Execs
          for( Object o : core.finishedExecs() ) {
            final IPAR_Execute exec = (IPAR_Execute)o;
            final Node runNode = pParentNode.getNode( "finished" ).addNode( exec.id() );
            final IPAR_ToolRun trun = exec.toolRun();
            if (jecarsSpecials( appSession, runNode, trun.path() )) {
              tbr.add( o );
              runNode.remove();
            } else {
              runNode.setProperty( "jecars:ToolRun",  trun.name() );
              runNode.setProperty( "jecars:ToolPath", trun.path() );
              runNode.setProperty( "jecars:Created",  trun.created() );          
              runNode.setProperty( "jecars:Started",  trun.started() );          
              runNode.setProperty( "jecars:Finished",  trun.finished() );          
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
          core.removeFinishedExecs( tbr );
          tbr.clear();
        }
        
      }      
    }
    
    return;
  }  
    
  /** jecarsSpecials
   * 
   * @param pSession
   * @param pNode
   * @param pPath
   * @return true if this node doesn't exists anymore
   * @throws RepositoryException
   */
  private boolean jecarsSpecials( Session pSession, Node pNode, String pPath ) throws RepositoryException {
    if (SPECTRE) {
      if (pPath!=null && !"<empty>".equals(pPath)) {
        try {          
          Node wfnode = pSession.getNode( pPath );
          if (wfnode.hasProperty( "jecars:ToolTemplate" )) {
            Node ttnode = wfnode.getProperty( "jecars:ToolTemplate" ).getNode();
            if (ttnode.hasProperty( "jecars:Title" )) {
              pNode.setProperty( "jecars:ToolTemplateTitle", ttnode.getProperty( "jecars:Title" ).getString() );
            }
          }
          if (wfnode.hasProperty( "jecars:Modified" )) {
            pNode.setProperty( "jecars:Modified", wfnode.getProperty( "jecars:Modified" ).getDate() );
          }
          if (wfnode.hasProperty( "jecars:PercCompleted" )) {
            pNode.setProperty( "jecars:PercCompleted", wfnode.getProperty( "jecars:PercCompleted" ).getDouble() );
          }
          if (wfnode.hasProperty( "jecars:State" )) {
            pNode.setProperty( "jecars:State", wfnode.getProperty( "jecars:State" ).getString() );
          }
          Node parentTool = wfnode;
          Node metaData = null;
          while( parentTool.hasProperty( "jecars:ParentTool" ) ) {
            if (metaData==null && parentTool.hasNode( "metaData" )) {
              metaData = parentTool.getNode( "metaData" );
            }
            parentTool = parentTool.getProperty( "jecars:ParentTool" ).getNode();
          }
          if (metaData!=null) {
            if (metaData.hasProperty( "jecars:Rawfile" )) {
              pNode.setProperty( "jecars:Rawfile", metaData.getProperty( "jecars:Rawfile" ).getString() );
            }
            if (metaData.hasProperty( "jecars:FixedWorkingDirectoryParent" )) {
              pNode.setProperty( "jecars:FixedWorkingDirectoryParent", metaData.getProperty( "jecars:FixedWorkingDirectoryParent" ).getString() );
            }
          }
          if (parentTool.hasProperty( "jecars:Title" )) {
            pNode.setProperty( "jecars:Title", parentTool.getProperty( "jecars:Title" ).getString() );
          }
          pNode.setProperty( "jecars:Username", parentTool.getParent().getName() );
          Node project = parentTool.getParent().getParent().getParent().getParent();
          if (project.hasProperty( "jecars:Title" )) {
            pNode.setProperty( "jecars:Projectname", project.getProperty( "jecars:Title" ).getString() );
          } else {
            pNode.setProperty( "jecars:Projectname", project.getName() );            
          }
          setSPeCTREProperties( parentTool, pNode );
          setSPeCTREProperties( wfnode, pNode );
        } catch( RepositoryException re ) {
          return true;
        }
      }
    }
    return false;
  }
  
  /** setSPeCTREProperties
   * 
   * @param pWFNode
   * @param pTargetNode
   * @throws RepositoryException 
   */
  private void setSPeCTREProperties( final Node pWFNode, final Node pTargetNode ) throws RepositoryException {
    if (pWFNode.hasNode( "wfType" )) {
      Node wftype = pWFNode.getNode( "wfType" );
      if (wftype.hasProperty( "jecars:string" )) {
        pTargetNode.setProperty( "jecars:wfType", wftype.getProperty( "jecars:string" ).getValues()[0].getString() );
      }
    }
    if (pWFNode.hasNode( "wfSetup" )) {
      Node wftype = pWFNode.getNode( "wfSetup" );
      if (wftype.hasProperty( "jecars:string" )) {
        pTargetNode.setProperty( "jecars:wfSetup", wftype.getProperty( "jecars:string" ).getValues()[0].getString() );
      }
    }
    return;
  }
  
}
