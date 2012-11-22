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

import javax.jcr.Node;
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

//  @Override
//  public void startTask() throws RepositoryException {
//    
//    final String taskPath = getNode().getProperty( "jecars:taskpath" ).getString();
//    
//    
//    return;
//  }
  
    
}
