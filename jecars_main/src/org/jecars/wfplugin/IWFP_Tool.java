/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.wfplugin;

import java.util.List;
import java.util.logging.Level;
import org.jecars.tools.workflow.IWF_Workflow;

/**
 *
 * @author weert
 */
public interface IWFP_Tool extends IWFP_Node {
  
  Level         getWorstExceptionLevel();
  String        getTaskPath();
  IWF_Workflow  getWorkflow();
  IWFP_Node     getTaskAsNode();
  IWFP_Node     getNodeFromRoot( final String pPath ) throws WFP_Exception;
  void          reportProgress(  final float pProgress );
  void          reportException( final Level pLevel, final Throwable pT );
  void          reportMessage(   final Level pLevel, final String pMessage );
  void          reportMessage(   final Level pLevel, final String pMessage, final int pRemoveAfterMinutes );

  List<IWFP_ContextParameter>   getContextParameters( final IWFP_Context pContext, final String pRegex, final String pParameterName, final boolean pMakeLocalCopy ) throws WFP_Exception;
  IWFP_ContextParameter         getContextParameter(  final IWFP_Context pContext, final String pRegex, final String pParameterName, final boolean pMakeLocalCopy ) throws WFP_Exception;
  String                        getParameter( final String pParameterName, final String pDefault );
  IWFP_Parameter                getParameter( final String pParameterName ) throws WFP_Exception;
  
}
