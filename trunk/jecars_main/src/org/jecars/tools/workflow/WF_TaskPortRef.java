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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
public class WF_TaskPortRef extends WF_Default implements IWF_TaskPortRef {
 
  static final public IWF_Task NULL = new WF_Task( null );
    
  public WF_TaskPortRef( final Node pNode ) {
    super( pNode );
  }

  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  @Override
  public IWF_TaskPort getTaskPort() throws RepositoryException {
//    return new WF_TaskPort( getNode().getSession().getNode( getNode().getProperty( "jecars:portref" ).getString()));
    return new WF_TaskPort( getNode().getProperty( "jecars:portref" ).getNode() );
  }

  
  
}
