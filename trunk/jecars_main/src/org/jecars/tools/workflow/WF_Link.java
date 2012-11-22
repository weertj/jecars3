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
public class WF_Link extends WF_Default implements IWF_Link {

  static final public IWF_Link NULL = new WF_Link( null );

    
  public WF_Link( final Node pNode ) {
    super(pNode);
  }
  
  @Override
  public IWF_LinkEndPoint getFromEndPoint() throws RepositoryException {
    return new WF_LinkEndPoint( getNode().getNode( "from" ) );
  }

  @Override
  public IWF_LinkEndPoint getToEndPoint() throws RepositoryException {
    return new WF_LinkEndPoint( getNode().getNode( "to" ) );
  }


  @Override
  public boolean isNULL() {
    return this==NULL;
  }

    
}
