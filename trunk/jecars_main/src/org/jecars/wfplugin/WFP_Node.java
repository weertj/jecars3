/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import org.jecars.CARS_Utils;
import org.jecars.tools.workflow.WF_WorkflowRunner;

/**
 *
 * @author weert
 */
public class WFP_Node implements IWFP_Node {
  
  private       transient Node      mNode;

  /** WFP_Node
   * 
   * @param pTool
   * @param pNode 
   */
  public WFP_Node( final Node pNode ) {
    mNode = pNode;
    return;
  }
 
  protected Node getNode() {
    return mNode;
  }
  
  /** getNode
   * 
   * @return 
   */
  @Override
  public Node getJCRNode() {
    return mNode;
  }
  
  @Override
  public void remove() throws WFP_Exception {
    try {
      getNode().remove();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return;
  }

  /** getPath
   * 
   * @return 
   */
  @Override
  public String getPath() {
    try {
      return mNode.getPath();
    } catch( RepositoryException re ) {
      return null;
    }
  }
  /** getNodes
   * 
   * @return 
   */
  @Override
  public List<IWFP_Node> getNodes() throws WFP_Exception {
    final List<IWFP_Node> nodes = new ArrayList<IWFP_Node>();
    try {
      for( final Iterator<Node> it = mNode.getNodes(); it.hasNext(); ) {
        final Node node = it.next();
        nodes.add( new WFP_Node( node ));
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return nodes;
  }
  
  @Override
  public List<IWFP_Property> getProperties() throws WFP_Exception {
    final List<IWFP_Property> props = new ArrayList<IWFP_Property>();
    try {
      for( final Iterator<Property> it = mNode.getProperties(); it.hasNext(); ) {
        final Property prop = it.next();
        props.add( new WFP_Property( prop ));
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return props;
  }

  @Override
  public boolean hasProperty( final String pName ) {
    try {
      return mNode.hasProperty( pName );
    } catch( RepositoryException re ) {
      return false;
    }
  }

  /** getProperty
   * 
   * @param pName
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Property getProperty( final String pName ) throws WFP_Exception {
    try {
      final WFP_Property prop = new WFP_Property( mNode.getProperty( pName ) );
      return prop;
    } catch( RepositoryException re ) {
      throw new WFP_Exception(re);
    }
  }

  @Override
  public IWFP_Node getNode( final String pName) throws WFP_Exception {
    try {
      final WFP_Node node = new WFP_Node( mNode.getNode( pName ) );
      return node;
    } catch( RepositoryException re ) {
      throw new WFP_Exception(re);
    }
  }

  @Override
  public String getName() throws WFP_Exception {
    try {
      return mNode.getName();
    } catch( RepositoryException re ) {      
      throw new WFP_Exception(re);
    }    
  }

  /** getResolvedNode
   * 
   * @param pTool
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Node getResolvedNode() throws WFP_Exception {
    try {
      final Node n = CARS_Utils.getLinkedNode( mNode );
      return new WFP_Node( n );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  /** rename
   * 
   * @param pName
   * @throws WFP_Exception 
   */
  @Override
  public void rename( final String pName, final String pTitle ) throws WFP_Exception {
    try {
      Node parent = mNode.getParent();
      if (!parent.hasNode( pName )) {
        synchronized( WF_WorkflowRunner.WRITERACCESS ) {
          mNode.setProperty( "jecars:Title", pTitle );
          mNode.getSession().move( mNode.getPath(), parent.getPath() + "/" + pName );
          mNode.getSession().save();
          mNode = parent.getNode( pName );
        }
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return;
  }

  @Override
  public IWFP_Node addNode( String pName, String pNodeType ) throws WFP_Exception{
    try {
      return new WFP_Node(mNode.addNode( pName, pNodeType ));
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Property setProperty( String pName, String pValue ) throws WFP_Exception{
    try {
      if (mNode.hasProperty( pName )) {
        if (mNode.getProperty( pName ).isMultiple()) {
          String[] vals = {pValue};
          return new WFP_Property( mNode.setProperty( pName, vals ) );
        } else {
          return new WFP_Property( mNode.setProperty( pName, pValue ) );
        }
      } else {
        return new WFP_Property( mNode.setProperty( pName, pValue ) );        
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Property setProperty( String pName, long pValue ) throws WFP_Exception{
    try {
      if (mNode.hasProperty( pName )) {
        if (mNode.getProperty( pName ).isMultiple()) {
          Value[] vals = {mNode.getSession().getValueFactory().createValue( pValue )};
          return new WFP_Property( mNode.setProperty( pName, vals ) );
        } else {
          return new WFP_Property( mNode.setProperty( pName, pValue ) );
        }
      } else {
        return new WFP_Property( mNode.setProperty( pName, pValue ) );        
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  
  @Override
  public boolean hasNode(String pName) throws WFP_Exception {
    try {
      return mNode.hasNode( pName );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public void addMixin( final String pN ) throws WFP_Exception {
    try {
      mNode.addMixin( pN  );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }    
  }

  
  @Override
  public void save() throws WFP_Exception {
    try {
      mNode.save();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public Object getNodeObject()  throws WFP_Exception {
    try {
      return CARS_Utils.getLinkedNode( mNode );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public Object makeLocalCopy() throws WFP_Exception {
    mNode = makeLocalCopy( mNode );
    return mNode;
  }
  
  static protected Node makeLocalCopy( Node pNode ) throws WFP_Exception {
    try {
      if (pNode.hasProperty( "jecars:Link" )) {
        // **** Local copy will be made
        Node n = CARS_Utils.getLinkedNode( pNode );
        Session  ses = n.getSession();
        Workspace ws = ses.getWorkspace();
        String copyTo = pNode.getPath();
        pNode.remove();
        ses.save();
        ws.copy( n.getPath(), copyTo );
        ses.save();
        pNode = ses.getNode( copyTo );
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );      
    }
    return pNode;
  }

  
    
}
