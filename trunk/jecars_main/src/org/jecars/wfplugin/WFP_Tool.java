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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import org.jecars.CARS_Utils;
import org.jecars.tools.CARS_ToolInstanceEvent;
import org.jecars.tools.workflow.IWF_Context;
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
  public IWFP_Task getTask() {
    return new WFP_Task( getNode() );
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
  public IWF_Workflow getWorkflow() {
    return mWorkflow;
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
  public CARS_ToolInstanceEvent reportProgress( final float pProgress ) {
    try {
      return mWorkflow.getToolInterface().reportProgress( pProgress );
    } catch( Exception e ) {
      return reportException( Level.SEVERE, e );
    }
  }

  /** reportException
   * 
   * @param pLevel
   * @param pT 
   */
  @Override
  public CARS_ToolInstanceEvent reportException( final Level pLevel, final Throwable pT ) {
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
    return tie;
  }

  /** reportMessage
   * 
   * @param pLevel
   * @param pMessage 
   */
  @Override
  public CARS_ToolInstanceEvent reportMessage( final Level pLevel, final String pMessage ) {
    try {
      return mWorkflow.getToolInterface().reportMessage( pLevel, pMessage, false );
    } catch( Exception e ) {
      return reportException( Level.SEVERE, e );
    }
  }

  /** reportMessage
   * 
   * @param pLevel
   * @param pMessage
   * @param pRemoveAfterMinutes 
   */
  @Override
  public CARS_ToolInstanceEvent reportMessage( final Level pLevel, final String pMessage, final int pRemoveAfterMinutes ) {
    try {
      CARS_ToolInstanceEvent tie = mWorkflow.getToolInterface().reportMessageEvent( pLevel, pMessage, false );
      CARS_Utils.setExpireDate( tie.getEventNode(), pRemoveAfterMinutes );
      tie.getEventNode().save();
      return tie;
    } catch( Exception e ) {
      return reportException( Level.SEVERE, e );
    }
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

  /** getParameter
   * 
   * The get parameter method first looks for the parameter at;
   *   
   *   The task/{toolname (resolved)}/{parameter}
   * 
   * if not found then search in;
   * 
   *  {current workflow}/{name of the tool}.{parameter}
   * 
   * @param pParameterName
   * @param pDefault
   * @return 
   */
  @Override
  public String getParameter( final String pParameterName, final String pDefault ) {
    String val = getParameter( getNode(), pParameterName, null );
    if (val==null) {
      val = getWorkflow().getParameter( getName() + "." + pParameterName, pDefault );
    }
    return val;
  }

  /** getParameter
   * 
   * @param pParameterName
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Parameter getParameter( final String pParameterName ) throws WFP_Exception {
    try {
      if (getNode().hasNode( pParameterName )) {
        final Node n = CARS_Utils.getLinkedNode( getNode().getNode( pParameterName ));
        return new WFP_Parameter( n );
      } else if (getWorkflow().getNode().hasNode( getName() + "." + pParameterName )) {
        final Node n = CARS_Utils.getLinkedNode( getWorkflow().getNode().getNode( getName() + "." + pParameterName ));
        return new WFP_Parameter( n );
      }
      return null;
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  
  
  /** getContextParameter
   * 
   * @param pContext
   * @param pParameterName
   * @return 
   */
  @Override
  public IWFP_ContextParameter getContextParameter( final IWFP_Context pContext, final String pRegex, final String pParameterName, final boolean pMakeLocalCopy ) throws WFP_Exception {
    final List<IWFP_ContextParameter> conparms = getContextParameters( pContext, pRegex, pParameterName, pMakeLocalCopy );
    if (conparms.isEmpty()) {
      return null;
    }
    return conparms.get(0);
  }

  /** getContextParameters
   * 
   * get parameter from the pContext.
   * 
   * @param pContext
   * @param pRegex Use the regex to retrieve the parameters or NULL
   * @param pParameterName Look for this parameter or NULL
   * @param pMakeLocalCopy If true then the parameter will be resolved and(!) stored, so the jecars:Link property will be gone
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public List<IWFP_ContextParameter> getContextParameters( final IWFP_Context pContext, final String pRegex, final String pParameterName, final boolean pMakeLocalCopy ) throws WFP_Exception {
    final List<IWFP_ContextParameter> conparms = new ArrayList<IWFP_ContextParameter>();
    try {
      final IWF_Context con = pContext.getContext();
      if (pRegex==null) {
        final String parname = getName() + "." + pParameterName;
        for( Node n : con.getParameterNodes() ) {
          if (n.getName().equals( parname )) {
            if (pMakeLocalCopy) {
              n = makeLocalCopy( n );
            }
            conparms.add( new WFP_ContextParameter( n ));
          }
        }
      } else {
        // **** Check against regular expression
        Pattern nnp = Pattern.compile( pRegex );
        for( Node n : con.getParameterNodes() ) {
          if (nnp.matcher( n.getName()).find()) {
            if (pMakeLocalCopy) {
              n = makeLocalCopy( n );
            }
            conparms.add( new WFP_ContextParameter( n ) );
          }
        }

      }
    } catch( RepositoryException re ) {
      return conparms;
    }
    return conparms;
  }
  
  /** getParameter
   * 
   * @param pNode
   * @param pParameterName
   * @param pDefault
   * @return 
   */
  static public String getParameter( final Node pNode, final String pParameterName, final String pDefault ) {
    try {
      if (pNode.hasNode( pParameterName )) {
        final Node n = CARS_Utils.getLinkedNode(pNode.getNode( pParameterName ));
        final Value[] values = n.getProperty( "jecars:string" ).getValues();
        if (values.length>0) {
          return values[0].getString();
        } else {
          return pDefault;
        }
      } else {
        return pDefault;
      }
    } catch(RepositoryException re ) {
      return pDefault;
    }
  }
  
  
  
}
