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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Utils;

/**
 *
 * @author weert
 */
public class JC_WorkflowLinkEndPointNode extends JC_DefaultNode {

  private JC_WorkflowTaskNode mCacheEndPoint = null;
    
  /** addPort
   * 
   * @param pTPN
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskPortRef addPort( final JC_WorkflowTaskPortNode pTPN, final JC_Nodeable pRelativeFrom ) throws JC_Exception {    
    final JC_WorkflowTaskPortRef tpr = (JC_WorkflowTaskPortRef)addNode( pTPN.getName(), "jecars:workflowtaskportref" ).morphToNodeType();
    if (pRelativeFrom==null) {
      tpr.setProperty( "jecars:portref",   pTPN.getPath() );
    } else {
      final String relp  = tpr.getPath().substring( pRelativeFrom.getPath().length()+1 );
      final String reltp = pTPN.getPath().substring( pRelativeFrom.getPath().length()+1 );
//      final String relp = pTPN.getPath().substring( pRelativeFrom.getPath().length()+1 );
      tpr.setProperty( "jecars:portref", JC_Utils.toRelative( "/"+relp ) + reltp );
    }
//    tpr.setProperty( "jecars:portindex", pTPN.getSequenceNumber() );
    return tpr;
  }

  public List<JC_WorkflowTaskPortRef> getPorts() throws JC_Exception {
    final List<JC_WorkflowTaskPortRef> ports = new ArrayList<JC_WorkflowTaskPortRef>();
    for( final JC_Nodeable n : getNodeList() ) {
      if ("jecars:workflowtaskportref".equals( n.getNodeType() )) {
        ports.add( (JC_WorkflowTaskPortRef)n.morphToNodeType() );
      }
    }
    return ports;
  }
  
  public String getEndPoint() throws JC_Exception {
    return getProperty( "jecars:endpoint" ).getValueString();
  }
  
  public void setEndPoint( final String pEndPoint ) throws JC_Exception {
    setProperty( "jecars:endpoint", pEndPoint );
    return;
  }

  
  public JC_WorkflowTaskNode getTask() throws JC_Exception {
    if (mCacheEndPoint==null) {
      final String endpoint = getEndPoint();      
      mCacheEndPoint = (JC_WorkflowTaskNode)getClient().getNode( this, endpoint ).morphToNodeType();
//      mCacheEndPoint = (JC_WorkflowTaskNode)getClient().getNode( endpoint ).morphToNodeType();
    }
    return mCacheEndPoint;
  }

  
}
