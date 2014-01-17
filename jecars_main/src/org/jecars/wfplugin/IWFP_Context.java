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
import javax.jcr.Node;
import org.jecars.CARS_Main;
import org.jecars.tools.workflow.IWF_Context;

/**
 *
 * @author weert
 */
public interface IWFP_Context {

  CARS_Main getMain();
    
  Node getContextNode();

  void copyInput( final IWFP_Node pInput ) throws WFP_Exception;

  boolean    hasInput( final String pName ) throws WFP_Exception;
  IWFP_Input getInput( final String pName ) throws WFP_Exception;

  List<IWFP_Input>  getInputs() throws WFP_Exception;
  Object            getTransientInput( final String pName );
  
  IWF_Context getContext();
  boolean     hasOutput( final String pName ) throws WFP_Exception;
  IWFP_Input  addInput(  final String pName ) throws WFP_Exception;
  IWFP_Output addOutput( final String pName ) throws WFP_Exception;
  IWFP_Output addOutput( final String pName, final String pNodeType ) throws WFP_Exception;
  IWFP_Output getOutput( final String pName ) throws WFP_Exception;
  void        addTransientObject( final String pName, final Object pData );

  Node    getParameterNodeValue(   final String pName, final IWFP_Tool pTool ) throws WFP_Exception;
  String  getParameterStringValue( final String pName ) throws WFP_Exception;

  IWFP_ContextParameter getParameter( final String pName ) throws WFP_Exception;
  IWFP_ContextParameter addParameter( final String pName ) throws WFP_Exception;
  
  void save() throws WFP_Exception;
  
}
