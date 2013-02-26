/*
 * Copyright 2011-2012 NLR - National Aerospace Laboratory
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.security.auth.login.CredentialExpiredException;
import org.jecars.tools.CARS_DefaultToolInterface;
import org.jecars.wfplugin.IWFP_Task;
import org.jecars.wfplugin.WFP_Task;
import org.jecars.wfplugin.WFP_Tool;

/** WF_Workflow
 *
 */
public class WF_Workflow extends WF_Default implements IWF_Workflow {

  static final public IWF_Workflow NULL = new WF_Workflow( null );

  static final private AtomicLong RUNNERNO = new AtomicLong(1);
  
  private final CARS_DefaultToolInterface mToolI = new CARS_DefaultToolInterface();
  
  /** WF_Workflow
   * 
   * @param pNode 
   */
  public WF_Workflow( final Node pNode ) {
    super(pNode);
    mToolI.setTool( null, pNode );
    try {
      mToolI.toolInitSettings();
    } catch( Exception e ) {
      mToolI.reportMessage( Level.WARNING, e.getMessage(), false );
    }
    return;
  }

  @Override
  public Node getNode() {
    return super.getNode();
  }
  
  
  
  /** getToolInterface
   * 
   * @return 
   */
  @Override
  public CARS_DefaultToolInterface getToolInterface() {
    return mToolI;
  }
  
  /** getTaskByType
   * 
   * @param pTT
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IWF_Task> getTaskByType( final EWF_TaskType pTT ) throws RepositoryException {
    final List<IWF_Task> nodes = new ArrayList<IWF_Task>();
    Node tasks = getNode().getNode( "tasks" );
    NodeIterator ni = tasks.getNodes();
    while( ni.hasNext() ) {
       Node n = ni.nextNode();
       if (pTT.name().equals( n.getProperty( "jecars:type" ).getString() )) {
         nodes.add( new WF_Task( n ) );
       }
    }
    return nodes;
  }

  /** getTaskWithoutInputs
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IWF_Task> getTaskWithoutInputs() throws RepositoryException {
    final List<IWF_Task> nodes = new ArrayList<IWF_Task>();
    Node tasks = getNode().getNode( "tasks" );
    NodeIterator ni = tasks.getNodes();
    while( ni.hasNext() ) {
       Node n = ni.nextNode();
       if (!n.getNode( "inputs" ).hasNodes()) {
         nodes.add( new WF_Task( n ) );
       }
    }
    return nodes;
  }

  /** getTaskByName
   * 
   * @param pName
   * @return 
   */
  @Override
  public IWFP_Task getTaskByName( final String pName ) {
    try {
      return new WFP_Task( getNode().getNode( "tasks" ).getNode( pName ) );
    } catch( RepositoryException re ) {
      return null;
    }
  }

  
  /** getFromLinkByTask
   * 
   * @param pTask
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IWF_Link> getFromLinkByTask( final IWF_Task pTask ) throws RepositoryException {
    final List<IWF_Link> links = new ArrayList<IWF_Link>();
    Node tasks = getNode().getNode( "links" );
    NodeIterator ni = tasks.getNodes();
    while( ni.hasNext() ) {
       Node n = ni.nextNode();
       IWF_Link link = new WF_Link( n );
       if (link.getFromEndPoint().getEndPoint().equals( pTask )) {
         links.add( new WF_Link( n ) );
       }
    }    
    return links;
  }

  /** createRunner
   * 
   * @param pMaster
   * @param pStartTask
   * @return
   * @throws RepositoryException 
   */
  @Override
  public IWF_WorkflowRunner createRunner( final IWF_WorkflowRunner pMaster, final IWF_Task pStartTask ) throws RepositoryException, AccessDeniedException, CredentialExpiredException, CloneNotSupportedException {
    final Node runners = getNode().getNode( "runners" );
    long no = RUNNERNO.getAndIncrement();
    final Node newRunner;
    if (runners.hasNode( pStartTask.getNode().getName() + "_" + no )) {
      final UUID uuid = UUID.randomUUID();
      newRunner = runners.addNode( pStartTask.getNode().getName() + "_" + uuid.toString(), "jecars:WorkflowRunner" );
    } else {
      newRunner = runners.addNode( pStartTask.getNode().getName() + "_" + no, "jecars:WorkflowRunner" );      
    }
    newRunner.addMixin( "jecars:interfaceclass" );
    newRunner.addMixin( "jecars:mixin_unstructured" );
    newRunner.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowRunnerInterfaceApp" );    
    newRunner.setProperty( "jecars:SingleStep", 0 );
    IWF_WorkflowRunner wr = new WF_WorkflowRunner( pMaster.getMain(), newRunner );
    wr.setProgress( 0 );
    save();
    return wr;
  }

  /** isNULL
   * 
   * @return 
   */
  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  /** copyTo
   * 
   * @param pParentNode
   * @param pName
   * @return
   * @throws RepositoryException 
   */
  @Override
  public WF_Workflow copyTo( final Node pParentNode, final String pName  ) throws RepositoryException {

    pParentNode.getSession().getWorkspace().copy( getNode().getPath(), pParentNode.getPath() + "/" + pName );
    final Node wf = pParentNode.getNode( pName );
    wf.setProperty( "jecars:ToolTemplate", getNode().getPath() );
    wf.addMixin( "jecars:interfaceclass" );
    wf.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowsInterfaceApp" );
    save();
   
    return new WF_Workflow(  wf );
  }

  /** getParameter
   * 
   * @param pParameterName
   * @param pDefault
   * @return 
   */
  @Override
  public String getParameter( final String pParameterName, final String pDefault ) {
    return WFP_Tool.getParameter( getNode(), pParameterName, pDefault );
  }
  
    
}
