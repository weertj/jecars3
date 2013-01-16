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

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** IWF_Context
 *
 */
public interface IWF_Context extends IWF_Default {

  void       setParameterNode( String pName, String pValue ) throws RepositoryException;
  Node       getParameterNode( String pName ) throws RepositoryException;
  List<Node> getParameterNodes() throws RepositoryException;
  List<Node> getDataNodes() throws RepositoryException;
  
  void setUsedLink( IWF_Link pLink ) throws RepositoryException;
  void setUsedTask( IWF_Task pTask ) throws RepositoryException;
  
  void copyFrom( final IWF_Context pContext ) throws RepositoryException;
  
  void restore( final int pStepNumber ) throws RepositoryException;
  
  void filter( final IWF_WorkflowRunner pRunner, final List<IWF_LinkEndPoint>pEndPoints ) throws RepositoryException;

  void linkFunctions( final IWF_WorkflowRunner pRunner,
            final List<IWF_LinkEndPoint>pFromPoints, final List<IWF_LinkEndPoint>pToPoints
             ) throws RepositoryException;

  void convertTo( final List<IWF_LinkEndPoint>pEndPoints ) throws RepositoryException;
  
  void clear() throws RepositoryException;
  
}
