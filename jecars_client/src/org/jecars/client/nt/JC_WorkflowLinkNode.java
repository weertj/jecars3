/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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

package org.jecars.client.nt;

import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Utils;

/**
 *
 * @author weert
 */
public class JC_WorkflowLinkNode extends JC_DefaultNode {
  
  static final public JC_WorkflowLinkNode NULL = new JC_WorkflowLinkNode();

  @Override
  public boolean isNull() {
    return this==NULL;
  }

  /** setFrom
   * 
   * @param pTask
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowLinkEndPointNode setFrom( final JC_WorkflowTaskNode pTask, final JC_Nodeable pRelativeFrom ) throws JC_Exception {
    final JC_Nodeable n;
    if (hasNode( "from" )) {
      n = getNode( "from" );
    } else {
      n = addNode( "from", "jecars:workflowlinkendpoint" );
    }
    final JC_WorkflowLinkEndPointNode lep = (JC_WorkflowLinkEndPointNode)n.morphToNodeType();
    if (pRelativeFrom==null) {
      lep.setProperty( "jecars:endpoint", pTask );
    } else {
      final String relp  = n.getPath().substring( pRelativeFrom.getPath().length()+1 );
      final String reltp = pTask.getPath().substring( pRelativeFrom.getPath().length()+1 );
      lep.setProperty( "jecars:endpoint", JC_Utils.toRelative( "/"+relp ) + reltp );
    }
    return lep;
  }
  
  /** getFromTask
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode getFromTask() throws JC_Exception {
    final JC_WorkflowLinkEndPointNode lep = (JC_WorkflowLinkEndPointNode)getNode( "from" ).morphToNodeType();
    return lep.getTask();
  }
  
  /** getTo
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowLinkEndPointNode getTo() throws JC_Exception {
    final JC_Nodeable n;
    if (hasNode( "to" )) {
      n = getNode( "to" ).morphToNodeType();
    } else {
      n = null;
    }
    return (JC_WorkflowLinkEndPointNode)n;
  }

  /** getFrom
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowLinkEndPointNode getFrom() throws JC_Exception {
    final JC_Nodeable n;
    if (hasNode( "from" )) {
      n = getNode( "from" ).morphToNodeType();
    } else {
      n = null;
    }
    return (JC_WorkflowLinkEndPointNode)n;
  }
  
  /** setTo
   * 
   * @param pTask
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowLinkEndPointNode setTo( final JC_WorkflowTaskNode pTask,  final JC_Nodeable pRelativeFrom ) throws JC_Exception {
    final JC_Nodeable n;
    if (hasNode( "to" )) {
      n = getNode( "to" );
    } else {
      n = addNode( "to", "jecars:workflowlinkendpoint" );
    }
    final JC_WorkflowLinkEndPointNode lep = (JC_WorkflowLinkEndPointNode)n.morphToNodeType();
    if (pRelativeFrom==null) {
      lep.setProperty( "jecars:endpoint", pTask );
    } else {
      final String relp  = n.getPath().substring( pRelativeFrom.getPath().length()+1 );
      final String reltp = pTask.getPath().substring( pRelativeFrom.getPath().length()+1 );
      lep.setProperty( "jecars:endpoint", JC_Utils.toRelative( "/"+relp ) + reltp );
    }
    return lep;
  }

  public JC_WorkflowTaskNode getToTask() throws JC_Exception {
    final JC_WorkflowLinkEndPointNode lep = (JC_WorkflowLinkEndPointNode)getNode( "to" ).morphToNodeType();
    return lep.getTask();
  }
  
  /** setExpireContextAfterMinutes
   * 
   * @param pMin
   * @throws JC_Exception 
   */
  public void setExpireContextAfterMinutes( final long pMin ) throws JC_Exception {
    setProperty( "jecars:ExpireContextAfterMinutes", pMin );
    return;
  }

}
