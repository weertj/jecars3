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
public class WF_TaskPort extends WF_Default implements IWF_TaskPort {

  static final public WF_TaskPort NULL = new WF_TaskPort( null );
    
  public WF_TaskPort( final Node pNode ) {
    super( pNode );
  }

  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  @Override
  public String getNodeName() throws RepositoryException {
    if (getNode().hasProperty( "jecars:nodename" )) {
      return getNode().getProperty( "jecars:nodename" ).getString();
    }
    return "";
  }

  @Override
  public String getNodeType() throws RepositoryException {
    if (getNode().hasProperty( "jecars:nodetype" )) {
      return getNode().getProperty( "jecars:nodetype" ).getString();
    }
    return "";
  }

  @Override
  public String getPropertyName() throws RepositoryException {
    if (getNode().hasProperty( "jecars:propertyname" )) {
      return getNode().getProperty( "jecars:propertyname" ).getString();
    }
    return "";
  }

  @Override
  public int getSequenceNumber() throws RepositoryException {
    if (getNode().hasProperty( "jecars:sequencenumber" )) {
      return (int)getNode().getProperty( "jecars:sequencenumber" ).getLong();
    }
    return -1;
  }    

  
}
