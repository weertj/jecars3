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

import java.util.EnumSet;
import java.util.concurrent.Future;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Main;
import org.jecars.wfplugin.IWFP_InterfaceResult;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 */
public interface IWF_WorkflowRunner extends IWF_Default {
 
  IWF_Workflow getWorkflow() throws RepositoryException;

  IWF_Context getContext() throws RepositoryException;
  
  void restart( final boolean pReRun, final boolean pRestoreFromCurrentContext ) throws RepositoryException;
  WFP_InterfaceResult singleStep() throws Exception;
  
  boolean isMainRunner();
  
  CARS_Main getMain();

  void setCurrentTask( final String pCurrentTask ) throws RepositoryException;
  void setCurrentLink( final String pCurrentLink ) throws RepositoryException;
  void setProgress(    final double pProgress    ) throws RepositoryException;
  void setState(       final String pState       ) throws RepositoryException;
  
  IWF_WorkflowRunner           setFuture( Future<IWFP_InterfaceResult>pIR );
  Future<IWFP_InterfaceResult> getFuture();

  void cancel();
  
  void    addInstruction(    final EWF_RunnerInstruction pRI );
  boolean hasInstruction(    final EWF_RunnerInstruction pRI );
  void    removeInstruction( final EWF_RunnerInstruction pRI );
  EnumSet<EWF_RunnerInstruction> getInstructions();

}
