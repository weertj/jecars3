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
package org.jecars.wfplugin;

import java.math.BigDecimal;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Utils;
import org.jecars.tools.CARS_ToolInstanceEvent;
import org.jecars.tools.workflow.IWF_Workflow;
import org.jecars.tools.workflow.WF_WorkflowRunner;

/** WFP_Tool
 *
 * @author weert
 */
public class WFP_Tool extends WFP_Node implements IWFP_Tool {

  private final transient IWF_Workflow    mWorkflow;
  private       transient Level           mWorstExceptionLevel = Level.FINEST;

  /** WFP_Tool
   * 
   * @param pTPath
   * @param pWorkflow 
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public WFP_Tool( final Node pNode, final IWF_Workflow pWorkflow ) {
    super( pNode );
    mWorkflow = pWorkflow;
    return;
  }

  @Override
  public String getTaskPath() {
    try {
      return getNode().getPath();
    } catch( RepositoryException re ) {
      reportException( Level.WARNING, re );
      return null;
    }
  }
  
  @Override
  public Level getWorstExceptionLevel() {
    return mWorstExceptionLevel;
  }
  
  @Override
  public IWFP_Node getTaskAsNode() {
    return new WFP_Node( getNode() );
  }
  
  /** reportProgress
   * 
   * @param pProgress 
   */
  @Override
  public void reportProgress( final float pProgress ) {
    try {
      mWorkflow.getToolInterface().reportProgress( pProgress );
    } catch( Exception e ) {
      reportException( Level.SEVERE, e );
    }
    return;
  }

  /** reportException
   * 
   * @param pLevel
   * @param pT 
   */
  @Override
  public void reportException( final Level pLevel, final Throwable pT ) {
    final CARS_ToolInstanceEvent tie = mWorkflow.getToolInterface().reportExceptionEvent( pT, pLevel );
    try {
      synchronized( WF_WorkflowRunner.WRITERACCESS ) {
        tie.getEventNode().addMixin( "jecars:mixin_unstructured" );
        tie.getEventNode().setProperty( "SourcePath", getPath() );
        tie.getEventNode().save();
      }
    } catch( RepositoryException re ) {
      re.printStackTrace();
    }
    if (pLevel.intValue()>mWorstExceptionLevel.intValue()) {
      mWorstExceptionLevel = pLevel;
    }
    return;
  }

  /** reportMessage
   * 
   * @param pLevel
   * @param pMessage 
   */
  @Override
  public void reportMessage( final Level pLevel, final String pMessage ) {
    try {
      mWorkflow.getToolInterface().reportMessage( pLevel, pMessage, false );
    } catch( Exception e ) {
      reportException( Level.SEVERE, e );
    }
  }

  /** reportMessage
   * 
   * @param pLevel
   * @param pMessage
   * @param pRemoveAfterMinutes 
   */
  @Override
  public void reportMessage( final Level pLevel, final String pMessage, final int pRemoveAfterMinutes ) {
    try {
      CARS_ToolInstanceEvent tie = mWorkflow.getToolInterface().reportMessageEvent( pLevel, pMessage, false );
      CARS_Utils.setExpireDate( tie.getEventNode(), pRemoveAfterMinutes );
      tie.getEventNode().save();
    } catch( Exception e ) {
      reportException( Level.SEVERE, e );
    }
    return;
  }  
  

  @Override
  public String getName() {
    try {
      return getNode().getName();
    } catch( RepositoryException re ) {
      reportException( Level.WARNING, re );
      return null;
    }
  }

  /** getNodeFromRoot
   * 
   * @param pPath
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Node getNodeFromRoot( final String pPath ) throws WFP_Exception {
    try {
      return new WFP_Node( mWorkflow.getNode().getSession().getNode( pPath ));
    } catch( RepositoryException re ) {
      throw new WFP_Exception(re);
    }
  }
    
}
