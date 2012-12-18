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
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import org.jecars.CARS_Utils;

/** WF_Context
 *
 */
public class WF_Context extends WF_Default implements IWF_Context {

  static final public IWF_Context NULL = new WF_Context( null );

  public WF_Context( final Node pNode ) {
    super( pNode );
    return;
  }
 
  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  /** getDataNodes
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<Node> getDataNodes() throws RepositoryException {
    final List<Node> nl = new ArrayList<Node>();
    NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
      Node n = ni.nextNode();
      if ((n.isNodeType( "jecars:mix_inputresource" ) ||
           n.isNodeType( "jecars:datafile" ) ||
           n.isNodeType( "jecars:dataresource" ) ||
           n.isNodeType( "jecars:mix_link" )) ) {
        if (!n.isNodeType("jecars:parameterresource")) {
          nl.add( n );
        }
      }
    }
    return nl;
  }

  /** getParameterNodes
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<Node> getParameterNodes() throws RepositoryException {
    final List<Node> nl = new ArrayList<Node>();
    NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
      Node n = ni.nextNode();
      if (n.isNodeType("jecars:parameterresource")) {
        nl.add( n );
      }
    }
    return nl;
  }

  @Override
  public void setParameterNode( final String pName, final String pValue ) throws RepositoryException {
    Node n = getNode();
    if (n.hasNode( pName )) {
      n.getNode( pName ).remove();
    }
    Node para = n.addNode( pName, "jecars:parameterdata" );
    para.setProperty( "jecars:string", pValue );
    return;
  }

  
  /** restore
   * 
   * @param pStepNumber
   * @throws RepositoryException 
   */
  @Override
  public void restore( final int pStepNumber ) throws RepositoryException {
    Node pn = getNode().getParent();
    if (pn.hasNode( "context_" + pStepNumber )) {
      final String path = getNode().getPath();
      getNode().remove();
      save();
      pn.getSession().move( pn.getNode(  "context_" + pStepNumber ).getPath(), path );
      save();
    }
    return;
  }

  /** copyFrom
   * 
   * @param pContext
   * @throws RepositoryException 
   */
  @Override
  public void copyFrom( final IWF_Context pContext ) throws RepositoryException {
    final Workspace ws = getNode().getSession().getWorkspace();
//    final Session  ses = getNode().getSession();
//    for( Node n : pContext.getDataNodes() ) {
//      ws.copy( n.getPath(), getNode().getPath() + "/" + n.getName() );
//    }
    final NodeIterator ni = pContext.getNode().getNodes();
    while( ni.hasNext() ) {
      final Node n = ni.nextNode();
      ws.copy( n.getPath(), getNode().getPath() + "/" + n.getName() );
    }
    return;
  }

  /** getFilterNodeCandidates
   * 
   * @param pNode
   * @return
   * @throws RepositoryException 
   */
  private List<Node> getFilterNodeCandidates( final List<Node> pNodes, final Node pNode ) throws RepositoryException {
    final NodeIterator ni = pNode.getNodes();
    while( ni.hasNext() ) {
      final Node n = ni.nextNode();
      pNodes.add( n );
      getFilterNodeCandidates( pNodes, n );
    }
    
    return pNodes;
  }
  
  
  /** linkFunctions
   * 
   * @param pRunner
   * @param pFromPoints
   * @param pToPoints
   * @throws RepositoryException 
   */
  @Override
  public void linkFunctions( final IWF_WorkflowRunner pRunner,
                final List<IWF_LinkEndPoint> pFromPoints, final List<IWF_LinkEndPoint> pToPoints
                ) throws RepositoryException {
    for( final IWF_LinkEndPoint fromLink : pFromPoints ) {
      for( final IWF_LinkEndPoint toLink : pToPoints ) {
        for( final IWF_TaskPortRef tpr : fromLink.getTaskPortRefs() ) {
          final IWF_TaskPort tp = tpr.getTaskPort();
          final String propname = tp.getPropertyName();
          if (!"".equals(propname)) {
            final Property p = fromLink.getEndPoint().getNode().getProperty( propname );
            for( final IWF_TaskPortRef toref : toLink.getTaskPortRefs() ) {
              final Node wn = toLink.getEndPoint().getNode();
              synchronized( WF_WorkflowRunner.WRITERACCESS ) {                
                if (!wn.isNodeType( "jecars:mixin_unstructured")) {
                  wn.addMixin( "jecars:mixin_unstructured" );
                }
                toLink.getEndPoint().getNode().setProperty(
                        toref.getTaskPort().getPropertyName(), p.getValue() );
                wn.save();
              }
            }
          }
        }
      }
    }
  }  
    
  
  /** filter
   * 
   * @param pEndPoints
   * @throws RepositoryException 
   */
  @Override
  public void filter( final IWF_WorkflowRunner pRunner, final List<IWF_LinkEndPoint>pEndPoints ) throws RepositoryException {

//    final IWF_Workflow workflow = pRunner.getWorkflow();
//    final CARS_ToolInterface toolI = workflow.getToolInterface();
    
    final int pathPrefixSize = getNode().getPath().length()+1;
      
    final List<Node> nodeList = getFilterNodeCandidates( new ArrayList<Node>(), getNode() );
//    final NodeIterator ni = getNode().getNodes();
//    while( ni.hasNext() ) {
//      nodeList.add( ni.nextNode() );
//    }
    final List<Node> positiveNodes = new ArrayList<Node>();
    for( final IWF_LinkEndPoint lep : pEndPoints ) {
      for( final IWF_TaskPortRef tpr : lep.getTaskPortRefs() ) {
        final IWF_TaskPort tp = tpr.getTaskPort();
        final String nodetype = tp.getNodeType();        
        if (!"".equals(tp.getNodeName())) {
          Pattern nnp = Pattern.compile( tp.getNodeName() );
          Pattern ntp = Pattern.compile( nodetype );
          for( final Node n : nodeList ) {
            if (n.isNodeType( "jecars:parameterresource")) {
              positiveNodes.add( n );
            } else if ((nnp.matcher( n.getPath().substring(pathPrefixSize) ).find()) &&
                (ntp.matcher( n.getPrimaryNodeType().getName() ).find())) {              
              positiveNodes.add( n );
            }
          }
        }
      }
    }

    // **** Filter the context
    for( final Node n : nodeList ) {
      if (!positiveNodes.contains(n)) {
//        toolI.reportMessage( Level.FINE, "FILTER: " + n.getPath() + " removed", false );
        try {
          n.remove();
        } catch( RepositoryException re ) {            
        }
      }
    }
    save();
    return;
  }

  /** convertTo
   * 
   * @param pEndPoints
   * @throws RepositoryException 
   */
  @Override
  public void convertTo( final List<IWF_LinkEndPoint> pEndPoints ) throws RepositoryException {
    final Session session = getNode().getSession();
    for( IWF_LinkEndPoint lep : pEndPoints ) {
      final List<IWF_TaskPortRef> tprs = lep.getTaskPortRefs();
      for( final IWF_TaskPortRef tpr : tprs ) {
        final IWF_TaskPort tp = tpr.getTaskPort();
        final String newNodeType = tp.getNodeType();
        if (!"".equals(tp.getNodeName())) {
          final NodeIterator ni = getNode().getNodes();
          while( ni.hasNext() ) {
            final Node n = ni.nextNode();
            if ("".equals(newNodeType)) {
              // **** Only move the node
              session.move( n.getPath(), n.getParent().getPath() + "/" + tp.getNodeName() );
            } else {
              // **** Convert also the nodetype
              final Node copyNode = CARS_Utils.getLinkedNode(n);
              final Node newNode = getNode().addNode( tp.getNodeName(), newNodeType );

              // **** Add the mixin's
              NodeType[] mixins = copyNode.getMixinNodeTypes();
              if (mixins!=null) {
                 for( NodeType mixin : mixins ) {
                   newNode.addMixin( mixin.getName() );
                 }
              }
              
              final PropertyIterator pi = copyNode.getProperties();
              while( pi.hasNext() ) {
                final Property prop = pi.nextProperty();
                if (!prop.getDefinition().isProtected()) {
                  try {
                    newNode.setProperty( prop.getName(), prop.getValue() );
                  } catch( RepositoryException re ) {
//                    re.printStackTrace();
                  }
                }
              }
              n.remove();
            }
          }
        }
      }
    }
    save();
    return;
  }

  
  
  
  
  /** clear
   * 
   * @throws RepositoryException 
   */
  @Override
  public void clear() throws RepositoryException {
    NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
      Node n = ni.nextNode();
      n.remove();
    }
    save();
    return;
  }

  @Override
  public void setUsedLink( final IWF_Link pLink ) throws RepositoryException {
    getNode().setProperty( "jecars:UsedInTask", "" );
    getNode().setProperty( "jecars:UsedInLink", pLink.getNode().getPath() );
    return;
  }
  
  @Override
  public void setUsedTask( final IWF_Task pTask ) throws RepositoryException {
    getNode().setProperty( "jecars:UsedInTask", pTask.getNode().getPath() );
    getNode().setProperty( "jecars:UsedInLink", "" );
    return;
  }


}
