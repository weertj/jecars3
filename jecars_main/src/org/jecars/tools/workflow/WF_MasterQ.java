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

package org.jecars.tools.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.nt.JC_WorkflowNode;
import org.jecars.par.IPAR_ResourceWish;
import org.jecars.tools.CARS_ToolInterface;
import org.jecars.tools.CARS_ToolsFactory;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Node;
import org.jecars.wfplugin.IWFP_Output;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WF_MasterQ implements IWFP_Interface {
  
  static private int EVENT_AGE_IN_MINUTES = 60*24;
  
  private String       mJeCARSServer =  "http://localhost/cars/";
  private String       mUsername = "Administrator";
  private String       mPassword = "admin";
  private IWFP_Tool    mTool;
  private IWFP_Context mContext;
    
  private Map<String, JC_Clientable> mClients = new HashMap<String, JC_Clientable>(8);
   
  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
    try {
      
      final JC_Clientable client = JC_Factory.createClient( mJeCARSServer );
      client.setCredentials( mUsername, mPassword.toCharArray() );
      mClients.put( mJeCARSServer, client );
      
      mTool    = pTool;
      mContext = pContext;
      System.out.println("MASTER Q Started");
      
      IWFP_Output q =  pContext.addOutput( "Q", "jecars:datafolder" );
      q.addMixin( "jecars:interfaceclass" );
      q.addMixin( "jecars:mixin_unstructured" );
      q.setProperty( "jecars:InterfaceClass", "org.jecars.tools.workflow.WF_MasterQInterface" );
      q.setProperty( "CurrentID", 1000 );
      
      final Node addQueues = q.getJCRNode().addNode( "AddQueues", "jecars:datafolder" );
      addQueues.addMixin( "jecars:mixin_unstructured" );
      final Node waitingQueues    = q.getJCRNode().addNode( "WaitingQueues", "jecars:datafolder" );
      waitingQueues.addMixin( "jecars:mixin_unstructured" );
      final Node runningQueues    = q.getJCRNode().addNode( "RunningQueues", "jecars:datafolder" );
      runningQueues.addMixin( "jecars:mixin_unstructured" );
      final Node readyQueues      = q.getJCRNode().addNode( "ReadyQueues", "jecars:datafolder" );
      readyQueues.addMixin( "jecars:mixin_unstructured" );
      final Node addWorkflows = q.getJCRNode().addNode( "AddWorkflows", "jecars:datafolder" );
      addWorkflows.addMixin( "jecars:mixin_unstructured" );
      final Node waitingWorkflows = q.getJCRNode().addNode( "WaitingWorkflows", "jecars:datafolder" );
      waitingWorkflows.addMixin( "jecars:mixin_unstructured" );
      final Node runningWorkflows = q.getJCRNode().addNode( "RunningWorkflows", "jecars:datafolder" );
      runningWorkflows.addMixin( "jecars:mixin_unstructured" );
      final Node readyWorkflows   = q.getJCRNode().addNode( "ReadyWorkflows", "jecars:datafolder" );
      readyWorkflows.addMixin( "jecars:mixin_unstructured" );
      pContext.save();
      
      // ***********************************************************************
      // **** Add observations
      
      // **** RunningWorkflows
      runningWorkflows.getSession().getWorkspace().getObservationManager().addEventListener(
              new runningWorkflowsListener(),
              Event.NODE_ADDED|Event.NODE_REMOVED|Event.PROPERTY_CHANGED,
              runningWorkflows.getPath(),
              false,
              null,
              null,
              false );

      // **** RunningQueues
      runningQueues.getSession().getWorkspace().getObservationManager().addEventListener(
              new runningQueuesListener(),
              Event.NODE_ADDED|Event.NODE_REMOVED|Event.PROPERTY_CHANGED,
              runningQueues.getPath(),
              false,
              null,
              null,
              false );
      
      
      while( 1==1 ) {
        Thread.sleep( 10000 );
        if (q.getPath()==null) {
          // **** We aren't there anymore, quit
          return WFP_InterfaceResult.ERROR().setError( new WFP_Exception( "Master Q is gone" ) );
        }
        System.out.println("Still alive: " + q.getPath() );
      }
      
    } catch( Exception e ) {      
      return WFP_InterfaceResult.ERROR().setError( e );
    }
      
//    return WFP_InterfaceResult.OK();
  }

  /** resolveWorkflowIDToNodeable
   * 
   * @param pWorkflowPath
   * @return
   * @throws WFP_Exception
   * @throws JC_Exception 
   */
  private Node resolveWorkflowIDToNodeable( final String pWorkflowPath ) throws WFP_Exception, RepositoryException {
    final IWFP_Node workflow = mTool.getNodeFromRoot( pWorkflowPath );
    return CARS_Utils.getLinkedNode( workflow.getJCRNode() );
//    if (workflow.hasProperty( "jecars:Link" )) {
//      String linkworkflow = workflow.getProperty( "jecars:Link" ).getStringValue();
//      // **** Use jecars client to resolve to JC_Nodeable
//      JC_Clientable client = mClients.get( mJeCARSServer );
//      return client.getSingleNode( linkworkflow );
//    }
//    return null;
  }
  
  private JC_Nodeable workflowToClientNode( final Node pWorkflow ) throws RepositoryException, JC_Exception {
    JC_Clientable client = mClients.get( mJeCARSServer );
    return client.getSingleNode( pWorkflow.getPath() );
  }
  
  /** runningWorkflowsListener
   * 
   */
  private class runningWorkflowsListener implements EventListener {

    @Override
    public void onEvent( final EventIterator pEvents ) {
      synchronized( WF_WorkflowRunner.WRITERACCESS ) {
        while( pEvents.hasNext() ) {
          final Event lastEvent = pEvents.nextEvent();
          switch( lastEvent.getType() ) {
            case Event.NODE_ADDED: {
              try {
                
                // **** New workflow can be executed
                final String wflPath = lastEvent.getPath();
                final Node wfl = resolveWorkflowIDToNodeable( wflPath );
                final CARS_Main main = CARS_Factory.createMain( wfl.getSession(), CARS_Factory.getLastFactory() );
                final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( main, wfl, null, true );
                IPAR_ResourceWish rwish = ti.resourceWish();
                final Node toolNode = ti.getTool();
                JC_WorkflowNode workflowNode = (JC_WorkflowNode)workflowToClientNode( wfl ).morphToNodeType();
                workflowNode.start();
                
              } catch( Exception re ) {
                mTool.reportException( Level.WARNING, re, EVENT_AGE_IN_MINUTES );
              }
              break;            
            }
          }
        }
      }

    }
    
  }

  
  /** runningQueuesListener
   * 
   */
  private class runningQueuesListener implements EventListener {

    @Override
    public void onEvent( final EventIterator pEvents ) {
      synchronized( WF_WorkflowRunner.WRITERACCESS ) {
        while( pEvents.hasNext() ) {
          Event lastEvent = pEvents.nextEvent();
          switch( lastEvent.getType() ) {
            case Event.NODE_ADDED: {
              try {
                // **** New queue can be executed
                String wflPath = lastEvent.getPath();
                Node wfl = resolveWorkflowIDToNodeable( wflPath );
                JC_WorkflowNode queueNode = (JC_WorkflowNode)workflowToClientNode( wfl ).morphToNodeType();
                queueNode.start();
                
              } catch( JC_Exception | WFP_Exception | RepositoryException re ) {
                mTool.reportException( Level.WARNING, re, EVENT_AGE_IN_MINUTES );
              }
              break;            
            }
          }
        }
      }

    }
    
  }

  
}
