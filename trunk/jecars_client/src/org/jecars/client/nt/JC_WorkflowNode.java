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
import java.util.Calendar;
import java.util.List;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;

/** JC_WorkflowNode
 *
 */
public class JC_WorkflowNode extends JC_ToolNode {

  /** addStart
   * 
   * @param pName
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addStart( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_START );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }

  /** addEnd
   * 
   * @param pName
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addEnd( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_END );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }
 
  /** addTask
   * 
   * @param pName
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addTask( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_TASK );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }

  /** addWorkflow
   * 
   * @param pName
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addWorkflow( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_WORKFLOW );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }

  /** addJavaTask
   * 
   * @param pName
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addJavaTask( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_JAVATASK );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }

  /** addJavaTask
   * 
   * @param pName
   * @param pJavaClass
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowTaskNode addJavaTask( final String pName, final String pJavaClass ) throws JC_Exception {
    final JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_JAVATASK );
    n.setProperty( "jecars:javaclasspath", pJavaClass );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }

  public JC_WorkflowTaskNode addConstants( final String pName ) throws JC_Exception {
    final JC_Nodeable n = getNode( "tasks" ).addNode( pName, "jecars:workflowtask" );
    n.setProperty( "jecars:type", JC_WorkflowTaskNode.TYPE_CONSTANTS );
    n.addMixin( "jecars:mixin_unstructured" );
    return (JC_WorkflowTaskNode)n.morphToNodeType();
  }
  
  public JC_WorkflowLinkNode addLink( final String pName ) throws JC_Exception {
    JC_Nodeable n = getNode( "links" ).addNode( pName, "jecars:workflowlink" );
    return (JC_WorkflowLinkNode)n.morphToNodeType();
  }

  /** addLink
   * 
   * @param pName
   * @param pFromTask
   * @param pFromTaskPort
   * @param pToTask
   * @param pToTaskPort
   * @param pExpireContextAfterMin if 0 then the context won't be expired
   * @return
   * @throws JC_Exception 
   */
  public JC_WorkflowLinkNode addLink( final String pName,
            final JC_WorkflowTaskNode       pFromTask,
            final JC_WorkflowTaskPortNode   pFromTaskPort,
            final JC_WorkflowTaskNode       pToTask,
            final JC_WorkflowTaskPortNode   pToTaskPort,
            final long                      pExpireContextAfterMin
            ) throws JC_Exception {
    JC_WorkflowLinkNode link = addLink( pName );
    JC_WorkflowLinkEndPointNode epn1 = link.setFrom( pFromTask, this );
    epn1.addPort( pFromTaskPort, this );
    JC_WorkflowLinkEndPointNode epn2 = link.setTo( pToTask, this );
    epn2.addPort( pToTaskPort, this );
    if (pExpireContextAfterMin>0) {
      link.setExpireContextAfterMinutes( pExpireContextAfterMin );
    }
    save();
    return link;
  }
  
  
  public JC_WorkflowRunnerNode getRunner( final String pName ) throws JC_Exception {
    return (JC_WorkflowRunnerNode)getNode( "runners" ).getNode( pName ).morphToNodeType();
  }

  /** getRunners
   * 
   * @return
   * @throws JC_Exception 
   */
  public List<JC_WorkflowRunnerNode> getRunners( final boolean pRefresh ) throws JC_Exception {
    final List<JC_WorkflowRunnerNode>runners = new ArrayList<JC_WorkflowRunnerNode>();
    if (pRefresh) {
      getNode( "runners" ).refresh();
    }
    for( final JC_Nodeable n : getNode( "runners" ).getNodeList() ) {
      if ("jecars:WorkflowRunner".equals( n.getNodeType() )) {
        runners.add( (JC_WorkflowRunnerNode)n.morphToNodeType() );
      }
    }
    return runners;
  }
  
  public JC_WorkflowRunnerNode addRunner( final String pName) throws JC_Exception {
    JC_Nodeable n = getNode( "runners" ).addNode( pName, "jecars:WorkflowRunner" );
    n.save();
    n.addMixin( "jecars:interfaceclass" );
    n.save();
//    n.addMixin( "jecars:mixin_unstructured" );   // **** TODO,  Temporary
//    n.save();
    n.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowRunnerInterfaceApp" );
//    n.setProperty( "jecars:currentLink", "" );
//    n.setProperty( "jecars:currentTask", "" );
    n.save();
    return (JC_WorkflowRunnerNode)n.morphToNodeType();    
  }
  
  /** getTasks
   * 
   * @return
   * @throws JC_Exception 
   */
  public List<JC_WorkflowTaskNode>getTasks() throws JC_Exception {
    final List<JC_WorkflowTaskNode> tns = new ArrayList<JC_WorkflowTaskNode>();
    for( final JC_Nodeable n : getNode( "tasks" ).getNodeList() ) {
      if ("jecars:workflowtask".equals(n.getNodeType())) {
        tns.add( (JC_WorkflowTaskNode)n.morphToNodeType() );
      }
    }
    return tns;
  }
  
  public JC_WorkflowTaskNode getTask( final String pName ) throws JC_Exception {
    if (getNode( "tasks" ).hasNode( pName )) {
      return (JC_WorkflowTaskNode)getNode( "tasks" ).getNode( pName ).morphToNodeType();
    }
    return JC_WorkflowTaskNode.NULL;
  }

  public JC_WorkflowLinkNode getLink( final String pName ) throws JC_Exception {
    if (getNode( "links" ).hasNode( pName )) {      
      return (JC_WorkflowLinkNode)getNode( "links" ).getNode( pName ).morphToNodeType();
    }
    return JC_WorkflowLinkNode.NULL;
  }
  
  /** getLinks
   * 
   * @return
   * @throws JC_Exception 
   */
  public List<JC_WorkflowLinkNode>getLinks() throws JC_Exception {
    final List<JC_WorkflowLinkNode> lns = new ArrayList<JC_WorkflowLinkNode>();
    for( final JC_Nodeable n : getNode( "links" ).getNodeList() ) {
      if ("jecars:workflowlink".equals(n.getNodeType())) {
        lns.add( (JC_WorkflowLinkNode)n.morphToNodeType() );
      }
    }
    return lns;
  }
   
  /** createTemplateWorkflow
   * 
   * @param pParentNode
   * @param pName
   * @param pServerClass
   * @param pStoreEvents
   * @return
   * @throws JC_Exception 
   */
  static public JC_WorkflowNode createTemplateWorkflow( JC_Nodeable pParentNode, final String pName, final String pServerClass, final boolean pStoreEvents ) throws JC_Exception {
    final JC_Nodeable tool = pParentNode.addNode( pName, "jecars:Workflow" );
    tool.setProperty( "jecars:ToolClass", pServerClass );
    tool.setProperty( "jecars:StoreEvents", pStoreEvents );
    tool.save();
    return (JC_WorkflowNode)tool.morphToNodeType();
  }
 
  /** createWorkflowInterface
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @param pExpireMinutes
   * @param pToolType
   * @return
   * @throws JC_Exception 
   */
  static public JC_WorkflowNode createWorkflowInterface( final JC_Nodeable pParentNode, final String pTemplateTool,
                    final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes,
                    final String pToolType ) throws JC_Exception {
    final JC_WorkflowNode tool = createWorkflow( pParentNode, pTemplateTool, pToolName, pToolUser, pExpireMinutes, pToolType );
    tool.addMixin( "jecars:interfaceclass" );
    tool.save();
    tool.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowsInterfaceApp" );
    tool.save();
    return tool;
  }
  
  /** createWorkflow
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @param pExpireMinutes
   * @param pToolType
   * @return
   * @throws JC_Exception 
   */
  static public JC_WorkflowNode createWorkflow( final JC_Nodeable pParentNode, final String pTemplateTool,
                    final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes,
                    final String pToolType ) throws JC_Exception {    
    if (pParentNode.hasNode( pToolName )) {
      throw new JC_Exception( "Workflow " + pToolName + " already exists" );
    }
    final JC_WorkflowNode tool = (JC_WorkflowNode)pParentNode.addNode( pToolName, pToolType, pTemplateTool ).morphToNodeType();
    tool.save();
    tool.setProperty( "jecars:ToolTemplate", pTemplateTool );
    if (pToolUser!=null) {
      tool.addMixin( "jecars:permissionable" );
      JC_PermissionNode.addRights( tool, pToolUser, JC_PermissionNode.RS_ALLRIGHTS );
    }
    tool.setProperty( "jecars:StoreEvents", true );
    tool.save();

    
    if (pExpireMinutes>0) {
      final Calendar c = Calendar.getInstance();
      c.add( Calendar.MINUTE, pExpireMinutes );
      tool.setExpireDate( c );
    }
    tool.save();
    if (pToolUser!=null) {
      final JC_PermissionNode perm = (JC_PermissionNode)tool.addNode( "P_" + pToolUser.getUsername(), "jecars:Permission" ).morphToNodeType();
      perm.addRights( pToolUser, JC_PermissionNode.RS_ALLRIGHTS );
      perm.save();
    }
    
    // **** Rebuild the links
/*    
    for( JC_WorkflowLinkNode link : tool.getLinks() ) {
      final JC_WorkflowLinkEndPointNode fromN = link.getFrom();
      if (fromN!=null) {
        String endP = fromN.getEndPoint();
        endP = endP.substring( pTemplateTool.length() );
        endP = tool.getPath() + endP;
        fromN.setEndPoint( endP );
        fromN.save();
      }
      final JC_WorkflowLinkEndPointNode toN = link.getTo();
      if (toN!=null) {
        String endP = toN.getEndPoint();
        endP = endP.substring( pTemplateTool.length() );
        endP = tool.getPath() + endP;
        toN.setEndPoint( endP );
        toN.save();
      }
    }
  */  
    return tool;
  }

  
}
