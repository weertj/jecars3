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

import java.util.List;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.security.auth.login.CredentialExpiredException;
import org.jecars.tools.CARS_DefaultToolInterface;
import org.jecars.wfplugin.IWFP_Task;

/** IWF_Workflow
 *
 */
public interface IWF_Workflow extends IWF_Default {

  CARS_DefaultToolInterface getToolInterface();
  
  Node getConfig() throws RepositoryException;
  
  WF_Workflow copyTo( final Node pParentNode, final String pName  ) throws RepositoryException;

  IWFP_Task      getTaskByName( final String pName );
  List<IWF_Task> getTaskWithoutInputs() throws RepositoryException;  
  List<IWF_Task> getTaskByType( final EWF_TaskType pTT ) throws RepositoryException;  
  List<IWF_Link> getFromLinkByTask( final IWF_Task pTask ) throws RepositoryException;
  
  IWF_WorkflowRunner createRunner( final IWF_WorkflowRunner pMaster, final IWF_Task pStartTask ) throws  AccessDeniedException, CredentialExpiredException, RepositoryException, CloneNotSupportedException;

  String        getParameter( final String pParameterName, final String pDefault );
  
}
