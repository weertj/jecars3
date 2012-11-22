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

package org.jecars.tools.workflow;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 *
 */
public class WF_LinkEndPoint extends WF_Default implements IWF_LinkEndPoint {

  static final public IWF_LinkEndPoint NULL = new WF_LinkEndPoint( null );
    
  public WF_LinkEndPoint( final Node pNode ) {
    super( pNode );
  }

  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  @Override
  public IWF_Task getEndPoint() throws RepositoryException {
    if (getNode().hasProperty( "jecars:endpoint" )) {
       return new WF_Task( getNode().getProperty( "jecars:endpoint" ).getNode() );
        
//      final String endpoint = getNode().getProperty( "jecars:endpoint" ).getString();
//      if (endpoint.startsWith( "/" )) {
//        return new WF_Task( getNode().getSession().getNode( endpoint ) );
//      } else {
//        return new WF_Task( getNode().getNode( endpoint ) );        
//      }
    } else {
      return WF_Task.NULL;
    }
  }
  
  @Override
  public List<IWF_TaskPortRef> getTaskPortRefs() throws RepositoryException {
    final List<IWF_TaskPortRef> tprs = new ArrayList<IWF_TaskPortRef>();
    final NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
       final Node n = ni.nextNode();
       final IWF_TaskPortRef tpr = new WF_TaskPortRef( n );
       tprs.add( tpr );
    }    
    
    return tprs;
  }

  
}
