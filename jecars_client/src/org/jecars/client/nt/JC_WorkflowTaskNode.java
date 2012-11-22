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
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;

/**
 *
 * @author weert
 */
public class JC_WorkflowTaskNode extends JC_DefaultNode {

  static final public JC_WorkflowTaskNode NULL = new JC_WorkflowTaskNode();
    
  public final static String TYPE_START     = "START";
  public final static String TYPE_END       = "END";
  public final static String TYPE_TASK      = "TASK";
  public final static String TYPE_WORKFLOW  = "WORKFLOW";
  public final static String TYPE_JAVATASK  = "JAVATASK";
  public final static String TYPE_CONSTANTS = "CONSTANTS";
    
  @Override
  public boolean isNull() {
    return this==NULL;
  }
  
  /** setTool
   * 
   * @param pTool
   * @throws JC_Exception 
   */
  public void setTool( final JC_ToolNode pTool ) throws JC_Exception {
    setProperty( "jecars:taskpath", pTool.getPath() );
    return;
  }
  
  public JC_WorkflowNode getWorkflow() throws JC_Exception {
    return (JC_WorkflowNode)getParent().getParent().morphToNodeType();
  }
  
  /** getType
   * 
   * @return
   * @throws JC_Exception 
   */
  public String getType() throws JC_Exception {
    return getProperty( "jecars:type" ).getValueString();
  }
    
  /** addOutputPort
   * 
   * @param pPropertyName
   * @param pNodeName
   * @param pSeqNumber
   * @return
   * @throws JC_Exception 
   * @deprecated 
   * Use {@link #addOutputPort(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long) }
   */
  @Deprecated
  public JC_WorkflowTaskPortNode addOutputPort( final String pPropertyName, final String pNodeName, final String pNodeType, final long pSeqNumber ) throws JC_Exception {
    final String portName = String.valueOf(pSeqNumber);
    return addOutputPort(portName, pPropertyName, pNodeName, pNodeType, pSeqNumber);
  }
  /** add output port
   * 
   * @param portName name of the input port to be created
   * @param pPropertyName
   * @param pNodeName
   * @param pNodeType
   * @param pSeqNumber sequence number if {pPropertyName, pNodeName, pNodeType} map to multiple values
   * @return
   * @throws JC_Exception 
   */
  @Deprecated
  public JC_WorkflowTaskPortNode addOutputPort( final String pPortName, final String pPropertyName, final String pNodeName, final String pNodeType, final long pSeqNumber ) throws JC_Exception {
    final JC_Nodeable n = getNode( "outputs" ).addNode( pPortName, "jecars:workflowtaskport" );
    final JC_WorkflowTaskPortNode tpn = (JC_WorkflowTaskPortNode)n.morphToNodeType();
    if (pPropertyName!=null) {
      tpn.setProperty( "jecars:propertyname", pPropertyName );
    }
    if (pNodeName!=null) {
      tpn.setProperty( "jecars:nodename", pNodeName );
    }
    tpn.setProperty( "jecars:nodetype", pNodeType );
//    tpn.setProperty( "jecars:sequencenumber", pSeqNumber );
    tpn.save();
    return tpn;
  }

  public JC_WorkflowTaskPortNode addOutputPort( final String pPortName, final String pPropertyName, final String pNodeName, final String pNodeType ) throws JC_Exception {
    final JC_Nodeable n = getNode( "outputs" ).addNode( pPortName, "jecars:workflowtaskport" );
    final JC_WorkflowTaskPortNode tpn = (JC_WorkflowTaskPortNode)n.morphToNodeType();
    if (pPropertyName!=null) {
      tpn.setProperty( "jecars:propertyname", pPropertyName );
    }
    if (pNodeName!=null) {
      tpn.setProperty( "jecars:nodename", pNodeName );
    }
    tpn.setProperty( "jecars:nodetype", pNodeType );
    tpn.save();
    return tpn;
  }

  /** addInputPort
   * 
   * @param pPropertyName
   * @param pNodeName
   * @param pNodeType
   * @param pSeqNumber
   * @return
   * @throws JC_Exception 
   * @deprecated
   * use {@link #addInputPort(java.lang.String, java.lang.String, java.lang.String, java.lang.String, long) }
   */
  @Deprecated
  public JC_WorkflowTaskPortNode addInputPort( final String pPropertyName, final String pNodeName, final String pNodeType, final long pSeqNumber ) throws JC_Exception {
    final String portName = String.valueOf(pSeqNumber);
    return addInputPort(portName, pPropertyName, pNodeName, pNodeType, pSeqNumber);
  }

  /** addInputPort
   * 
   * @param portName name of the input port to be created
   * @param pPropertyName
   * @param pNodeName
   * @param pNodeType
   * @param pSeqNumber sequence number if {pPropertyName, pNodeName, pNodeType} map to multiple values
   * @return
   * @throws JC_Exception 
   */
  @Deprecated
  public JC_WorkflowTaskPortNode addInputPort( final String pPortName, final String pPropertyName, final String pNodeName, final String pNodeType, final long pSeqNumber ) throws JC_Exception {
    final JC_Nodeable n = getNode( "inputs" ).addNode( pPortName, "jecars:workflowtaskport" );
    final JC_WorkflowTaskPortNode tpn = (JC_WorkflowTaskPortNode)n.morphToNodeType();
    if (pPropertyName!=null) {
      tpn.setProperty( "jecars:propertyname", pPropertyName );
    }
    if (pNodeName!=null) {
      tpn.setProperty( "jecars:nodename", pNodeName );
    }
    tpn.setProperty( "jecars:nodetype", pNodeType );
//    tpn.setProperty( "jecars:sequencenumber", pSeqNumber );
    tpn.save();
    return tpn;
  }

  public JC_WorkflowTaskPortNode addInputPort( final String pPortName, final String pPropertyName, final String pNodeName, final String pNodeType ) throws JC_Exception {
    final JC_Nodeable n = getNode( "inputs" ).addNode( pPortName, "jecars:workflowtaskport" );
    final JC_WorkflowTaskPortNode tpn = (JC_WorkflowTaskPortNode)n.morphToNodeType();
    if (pPropertyName!=null) {
      tpn.setProperty( "jecars:propertyname", pPropertyName );
    }
    if (pNodeName!=null) {
      tpn.setProperty( "jecars:nodename", pNodeName );
    }
    tpn.setProperty( "jecars:nodetype", pNodeType );
    tpn.save();
    return tpn;
  }
  
  public List<JC_WorkflowTaskPortNode>getInputPorts() throws JC_Exception {
    final List<JC_WorkflowTaskPortNode> ons = new ArrayList<JC_WorkflowTaskPortNode>();
    for( JC_Nodeable n : getNode( "inputs" ).getNodeList() ) {
      if (n instanceof JC_WorkflowTaskPortNode) {
        ons.add( (JC_WorkflowTaskPortNode)n );
      }
    }
    return ons;
  }

  public List<JC_WorkflowTaskPortNode>getOutputPorts() throws JC_Exception {
    final List<JC_WorkflowTaskPortNode> ons = new ArrayList<JC_WorkflowTaskPortNode>();
    for( JC_Nodeable n : getNode( "outputs" ).getNodeList() ) {
      if (n instanceof JC_WorkflowTaskPortNode) {
        ons.add( (JC_WorkflowTaskPortNode)n );
      }
    }
    return ons;
  }
  
  /** addParameterData
   *
   * @param pName
   * @return
   * @throws java.lang.Exception
   */
  public JC_ParameterDataNode addParameterData( final String pName ) throws JC_Exception {    
    final JC_Nodeable pn = addNode( pName, "jecars:parameterdata" );
    pn.save();
    return (JC_ParameterDataNode)pn.morphToNodeType();
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
