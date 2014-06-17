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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.tools.CARS_ToolInterface;

/**
 *
 * @author weert
 */
public class WF_MasterQInterface extends CARS_DefaultInterface {
  
  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools.workflow" );

  static final public Object MASTERQ_LOCK = new Object();
  
  /** addNode
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws Exception 
   */
  @Override
  public Node addNode( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pName, String pPrimType, JD_Taglist pParams) throws Exception {
    LOG.info( "MASTER Q: ADD NODE " + pParentNode.getName() + " / " + pName + " (" + pPrimType + ")" );
    final String leaf = pParentNode.getPath().substring( pInterfaceNode.getPath().length() );
    if ("/WaitingWorkflows".equals( leaf )) {
      return moveWorkflowToWaiting( pInterfaceNode, pName );
    } else if ("/RunningWorkflows".equals( leaf )) {
      return moveWorkflowToRunning( pInterfaceNode, pName );
    } else if ("/WaitingQueues".equals( leaf )) {
      return moveQueueToWaiting( pInterfaceNode, pName );
    } else if ("/RunningQueues".equals( leaf )) {
      return moveQueueToRunning( pInterfaceNode, pName );
    }
    return null;
  }

  /** copyNode
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pCopyNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws javax.jcr.RepositoryException 
   */
  @Override
  public Node copyNode( final CARS_Main pMain, final  Node pInterfaceNode, final Node pParentNode, final Node pCopyNode, final String pName, final  String pPrimType, final JD_Taglist pParams) throws RepositoryException {
    Node n = null;
    final String leaf = pParentNode.getPath().substring( pInterfaceNode.getPath().length() );
    LOG.info( "MASTER Q: COPY NODE " + leaf + " / " + pName + " from " + pCopyNode.getPath() );
    if ("/AddWorkflows".equals( leaf )) {
      n = addNewWorkflow( pInterfaceNode, pParentNode, pCopyNode );
    } else if ("/AddQueues".equals( leaf )) {
      n = addNewQueue( pInterfaceNode, pParentNode, pCopyNode );
    }
    return n;
  }
 
  /** getNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws RepositoryException 
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws RepositoryException {

    try {
      synchronized( MASTERQ_LOCK ) {
        if ("/Q/RunningWorkflows".equals( pLeaf )) {
          updateRunningWorkflows( pInterfaceNode );
        }
        if ("/Q/ReadyWorkflows".equals( pLeaf )) {
          updateReadyWorkflows( pInterfaceNode );
          updateRunningWorkflows( pInterfaceNode );
        }
        if ("/Q/WaitingWorkflows".equals( pLeaf )) {
          updateRunningWorkflows( pInterfaceNode );
        }
        if ("/Q/RunningQueues".equals( pLeaf )) {
          updateRunningQueues( pInterfaceNode );
        }
        if ("/Q/ReadyQueues".equals( pLeaf )) {
          updateReadyQueues( pInterfaceNode );
          updateRunningQueues( pInterfaceNode );
        }
        if ("/Q/WaitingQueues".equals( pLeaf )) {
          updateRunningQueues( pInterfaceNode );
        }
      }
    } finally {
      pParentNode.getSession().save();
    }

    return;
  }
  
  /** updateRunningWorkflows
   * 
   * @param pInterfaceNode
   * @throws RepositoryException 
   */
  private void updateRunningWorkflows( final Node pInterfaceNode ) throws RepositoryException {
    
    // **** Check if running workflows are still running
    NodeIterator ni = pInterfaceNode.getNode( "RunningWorkflows" ).getNodes();
    while( ni.hasNext() ) {
      Node rwn = ni.nextNode();
      if (rwn.isNodeType( "jecars:Workflow" )) {
        try {
          Node realworkflow = CARS_Utils.getLinkedNode( rwn );
          rwn.setProperty( "jecars:State", realworkflow.getProperty( "jecars:State" ).getString() );
          if (realworkflow.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED )) {
            // **** This workflow is closed
            moveWorkflowToReady( pInterfaceNode, rwn.getName() );
          }
        } catch( PathNotFoundException pe ) {
          rwn.setProperty( "jecars:State", CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_TERMINATED );
        }
      }
    }
    
    return;
  }
  
  /** getWorkflowByID
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node getWorkflowByID( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    if (pInterfaceNode.getNode( "AddWorkflows" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "AddWorkflows" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "WaitingWorkflows" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "WaitingWorkflows" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "RunningWorkflows" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "RunningWorkflows" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "ReadyWorkflows" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "ReadyWorkflows" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "AddQueues" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "AddQueues" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "WaitingQueues" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "WaitingQueues" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "RunningQueues" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "RunningQueues" ).getNode( pID );
    }
    if (pInterfaceNode.getNode( "ReadyQueues" ).hasNode( pID )) {
      return pInterfaceNode.getNode( "ReadyQueues" ).getNode( pID );
    }
    return null;
  }

  /** moveWorkflowToReady
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveWorkflowToReady( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "ReadyWorkflows" );
      wfl.remove();
      return addWorkflow( waiting, pID, wfllink );
    }
    return null;
  }

  /** moveWorkflowToWaiting
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveWorkflowToWaiting( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "WaitingWorkflows" );
      wfl.remove();
      return addWorkflow( waiting, pID, wfllink );
    }
    return null;
  }

  /** moveWorkflowToRunning
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveWorkflowToRunning( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "RunningWorkflows" );
      wfl.remove();
      
      return addWorkflow( waiting, pID, wfllink );
    }
    return null;
  }

  /** addWorkflow
   * 
   * @param pParentNode
   * @param pID
   * @param pLinkToRealWorkflow
   * @return
   * @throws RepositoryException 
   */
  private Node addWorkflow( final Node pParentNode, String pID, Node pLinkToRealWorkflow ) throws RepositoryException {
    Node workflow = pParentNode.addNode( pID, "jecars:Workflow" );
    workflow.addMixin( "jecars:mix_link" );
    workflow.setProperty( "jecars:Link", pLinkToRealWorkflow.getPath() );
    workflow.setProperty( "jecars:State", pLinkToRealWorkflow.getProperty( "jecars:State" ).getString() );
    return workflow;
  }

  /** addNewWorkflow
   * 
   * @param pInterfaceNode
   * @param pParentNode
   * @param pFromWorkflow
   * @return
   * @throws RepositoryException 
   */
  private Node addNewWorkflow( final Node pInterfaceNode, final Node pParentNode, final Node pFromWorkflow ) throws RepositoryException {
    synchronized( MASTERQ_LOCK ) {
      long id = pInterfaceNode.getProperty( "CurrentID" ).getLong();
      Node workflow = addWorkflow( pParentNode, String.valueOf(id), pFromWorkflow );
      id++;
      pInterfaceNode.setProperty( "CurrentID", id );
      return workflow;
    }
  }

  /** addQueue
   * 
   * @param pParentNode
   * @param pID
   * @param pLinkToRealQueue
   * @return
   * @throws RepositoryException 
   */
  private Node addQueue( final Node pParentNode, String pID, Node pLinkToRealQueue ) throws RepositoryException {
    Node workflow = pParentNode.addNode( pID, "jecars:Workflow" );
    workflow.addMixin( "jecars:mix_link" );
    workflow.setProperty( "jecars:Link", pLinkToRealQueue.getPath() );
    workflow.setProperty( "jecars:State", pLinkToRealQueue.getProperty( "jecars:State" ).getString() );
    return workflow;
  }

  
  /** addNewQueue
   * 
   * @param pInterfaceNode
   * @param pParentNode
   * @param pFromQueue
   * @return
   * @throws RepositoryException 
   */
  private Node addNewQueue( final Node pInterfaceNode, final Node pParentNode, final Node pFromQueue ) throws RepositoryException {
    synchronized( MASTERQ_LOCK ) {
      long id = pInterfaceNode.getProperty( "CurrentID" ).getLong();
      Node workflow = addQueue( pParentNode, String.valueOf(id), pFromQueue );
      id++;
      pInterfaceNode.setProperty( "CurrentID", id );
      return workflow;
    }
  }

  /** moveQueueToReady
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveQueueToReady( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "ReadyQueues" );
      wfl.remove();
      return addQueue( waiting, pID, wfllink );
    }
    return null;
  }

  /** moveQueueToWaiting
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveQueueToWaiting( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "WaitingQueues" );
      wfl.remove();
      
      return addQueue( waiting, pID, wfllink );
    }
    return null;
  }

  
  /** moveQueueToRunning
   * 
   * @param pInterfaceNode
   * @param pID
   * @return
   * @throws RepositoryException 
   */
  private Node moveQueueToRunning( final Node pInterfaceNode, final String pID ) throws RepositoryException {
    Node wfl = getWorkflowByID( pInterfaceNode, pID );
    if (wfl!=null) {
      Node wfllink = wfl.getProperty( "jecars:Link" ).getNode();
      Node waiting = pInterfaceNode.getNode( "RunningQueues" );
      wfl.remove();
      
      return addQueue( waiting, pID, wfllink );
    }
    return null;
  }

  /** updateRunningQueues
   * 
   * @param pInterfaceNode
   * @throws RepositoryException 
   */
  private void updateRunningQueues( final Node pInterfaceNode ) throws RepositoryException {
    
    // **** Check if running queues are still running
    NodeIterator ni = pInterfaceNode.getNode( "RunningQueues" ).getNodes();
    while( ni.hasNext() ) {
      Node rwn = ni.nextNode();
      if (rwn.isNodeType( "jecars:Workflow" )) {
        try {
          Node realworkflow = CARS_Utils.getLinkedNode( rwn );
          rwn.setProperty( "jecars:State", realworkflow.getProperty( "jecars:State" ).getString() );
          if (realworkflow.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED )) {
            // **** This queue is closed
            moveQueueToReady( pInterfaceNode, rwn.getName() );
          }
        } catch( PathNotFoundException pe ) {
          rwn.setProperty( "jecars:State", CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_TERMINATED );
        }
      }
    }
    
    return;
  }

  /** updateReadyWorkflows
   * 
   * @param pInterfaceNode
   * @throws RepositoryException 
   */
  private void updateReadyWorkflows( final Node pInterfaceNode ) throws RepositoryException {
    
    final List<Node> tbrWorkflows = new ArrayList<>();
    
    // **** Check if ready workflows are still available
    NodeIterator ni = pInterfaceNode.getNode( "ReadyWorkflows" ).getNodes();
    while( ni.hasNext() ) {
      Node rwn = ni.nextNode();
      if (rwn.isNodeType( "jecars:Workflow" )) {
        try {
          Node realworkflow = CARS_Utils.getLinkedNode( rwn );
        } catch( ItemNotFoundException pe ) {
          tbrWorkflows.add( rwn );
        }
      }
    }
    
    // **** Remove the broken workflows
    for( Node tbrw : tbrWorkflows ) {
      tbrw.remove();
    }    
    
    return;
  }

  /** updateReadyQueues
   * 
   * @param pInterfaceNode
   * @throws RepositoryException 
   */
  private void updateReadyQueues( final Node pInterfaceNode ) throws RepositoryException {
    
    final List<Node> tbrQueues = new ArrayList<>();
    
    // **** Check if ready queues are still available
    NodeIterator ni = pInterfaceNode.getNode( "ReadyQueues" ).getNodes();
    while( ni.hasNext() ) {
      Node rwn = ni.nextNode();
      if (rwn.isNodeType( "jecars:Workflow" )) {
        try {
          Node realworkflow = CARS_Utils.getLinkedNode( rwn );
        } catch( ItemNotFoundException pe ) {
          tbrQueues.add( rwn );
        }
      }
    }
    
    // **** Remove the broken workflows
    for( Node tbrw : tbrQueues ) {
      tbrw.remove();
    }    
    
    return;
  }

  
}
