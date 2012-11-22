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

import javax.jcr.RepositoryException;
import org.jecars.CARS_Main;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 */
public interface IWF_WorkflowRunner {
 
  IWF_Workflow getWorkflow() throws RepositoryException;

  IWF_Context getContext() throws RepositoryException;
  
  void restart()    throws Exception;
  WFP_InterfaceResult singleStep() throws Exception;
  
  boolean isMainRunner();
  
  CARS_Main getMain();

  void setCurrentTask( final String pCurrentTask ) throws RepositoryException;
  void setCurrentLink( final String pCurrentLink ) throws RepositoryException;
  void setProgress(    final double pProgress    ) throws RepositoryException;
  void setState(       final String pState       ) throws RepositoryException;
  
  void   setThread( final Thread pT );
  Thread getThread();
  
}
