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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 *
 */
public class WF_Task extends WF_Default implements IWF_Task {

  static final public IWF_Task NULL = new WF_Task( null );
    
  public WF_Task( final Node pNode ) {
    super( pNode );
  }

  @Override
  public boolean isNULL() {
    return this==NULL;
  }
  
  @Override
  public Node getToolTemplateNode() throws RepositoryException {
    final String taskPath = getNode().getProperty( "jecars:taskpath" ).getString();
    return getNode().getSession().getNode( taskPath );
  }
  
  /** getType
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public EWF_TaskType getType() throws RepositoryException {
    final String type = getNode().getProperty( "jecars:type" ).getString();
    return EWF_TaskType.valueOf( type );
  }

  @Override
  public List<IWF_TaskPort>getInputs() {
    final List<IWF_TaskPort> tps = new ArrayList<IWF_TaskPort>();
    try {
      final NodeIterator ni = getNode().getNode( "inputs" ).getNodes();
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (n.isNodeType( "jecars:workflowtaskport" )) {
          tps.add( new WF_TaskPort( n ) );
        }
      }
    } catch( RepositoryException re ) {        
    }
    return tps;
  }

  @Override
  public List<IWF_TaskPort>getOutputs() {
    final List<IWF_TaskPort> tps = new ArrayList<IWF_TaskPort>();
    try {
      final NodeIterator ni = getNode().getNode( "outputs" ).getNodes();
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (n.isNodeType( "jecars:workflowtaskport" )) {
          tps.add( new WF_TaskPort( n ) );
        }
      }
    } catch( RepositoryException re ) {        
    }
    return tps;
  }
  
//  @Override
//  public void startTask() throws RepositoryException {
//    
//    final String taskPath = getNode().getProperty( "jecars:taskpath" ).getString();
//    
//    
//    return;
//  }

  /** getModifiers
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public EnumSet<EWF_TaskModifier> getModifiers() throws RepositoryException {
    final EnumSet<EWF_TaskModifier> es = EnumSet.noneOf(EWF_TaskModifier.class);
    if (getNode().hasProperty( "jecars:Modifiers" )) {
      String mods = getNode().getProperty( "jecars:Modifiers" ).getString();
      String[] mda = mods.split(",");
      for( String md : mda ) {
        es.add( EWF_TaskModifier.valueOf(md) );
      }
    }
    return es;
  }
  
    
}
