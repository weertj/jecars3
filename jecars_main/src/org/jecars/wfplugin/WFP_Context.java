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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Main;
import org.jecars.tools.workflow.IWF_Context;

/** WFP_Context
 *
 * @author weert
 */
public class WFP_Context implements IWFP_Context {
 
  private final transient CARS_Main             mMain;  
  private final transient IWF_Context           mContext;
  private final transient List<IWFP_Input>      mInputs = new ArrayList<IWFP_Input>();
  private final transient Map<String, Object>   mTransientInputs = new HashMap<String, Object>();

  /** WFP_Context
   * 
   * @param pContext 
   */
  public WFP_Context( final IWF_Context pContext, final CARS_Main pMain ) throws RepositoryException {
    mMain    = pMain;
    mContext = pContext;
    if (mContext!=null) {
      for( final Node dn : mContext.getDataNodes() ) {
        addInput( new WFP_Input( dn ) );
      }
    }
    return;
  }

  /** getMain
   * 
   * @return 
   */
  @Override
  public CARS_Main getMain() {
    return mMain;
  }
 
  
  
  /** copyTransientInputs
   * 
   * @param pContext 
   */
  public void copyTransientInputs( WFP_Context pContext ) {
    mTransientInputs.putAll( pContext.mTransientInputs );
    return;
  }
  
  /** addInput
   * 
   * @param pInput 
   */
  private void addInput( final IWFP_Input pInput ) {
    mInputs.add( pInput );
    return;
  }
  
  /** getContextNode
   * 
   * @return 
   */
  @Override
  public Node getContextNode() {
    return mContext.getNode();
  }
  
  /** copyInput
   * 
   * @param pInput
   * @throws WFP_Exception 
   */
  @Override
  public void copyInput( final IWFP_Input pInput ) throws WFP_Exception {
    try {
      Session ses = mContext.getNode().getSession();
      ses.getWorkspace().copy( pInput.getPath(), mContext.getNode().getPath() + "/" + pInput.getName()  );
    } catch( RepositoryException re ) {
      throw new WFP_Exception(re);
    }
    return;
  }
  
  /** getInputs
   * 
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public List<IWFP_Input> getInputs() throws WFP_Exception {
    return mInputs;
  }

  /** addOutput
   * 
   * @param pName
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Output addOutput( final String pName ) throws WFP_Exception {
    try {
      final Node n = mContext.getNode().addNode( pName, "jecars:inputresource" );
      n.setProperty( "jcr:mimeType", "text/plain" );
      return new WFP_Output( mContext, n );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  /** hasInput
   * 
   * @param pName
   * @return 
   */
  @Override
  public boolean hasInput( final String pName ) throws WFP_Exception {
    return getInput( pName )!=null;
  }

  /** getInput
   * 
   * @param pName
   * @return 
   */
  @Override
  public IWFP_Input getInput( final String pName ) throws WFP_Exception {
    for( final IWFP_Input input : mInputs ) {
      if (input.getName().equals( pName )) {
        return input;
      }
    }
    return null;    
  }

  /** getTransientInput
   * 
   * @param pName
   * @return 
   */
  @Override
  public Object getTransientInput( final String pName ) {
    return mTransientInputs.get( pName );
  }

  /** addTransientObject
   * 
   * @param pName
   * @param pData 
   */
  @Override
  public void addTransientObject( final String pName, final Object pData ) {
    mTransientInputs.put( pName, pData );
    return;
  }
    
}
