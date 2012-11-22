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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.tools.ant.util.ReaderInputStream;
import org.jecars.tools.workflow.IWF_Context;

/**
 *
 * @author weert
 */
public class WFP_Output extends WFP_Node implements IWFP_Output {
  
  private final transient IWF_Context  mContext;
  
  /** WFP_Output
   * 
   * @param pContext
   * @param pNode 
   */
  protected WFP_Output( final IWF_Context pContext, final Node pNode ) {
    super( pNode );
    mContext    = pContext;
    return;
  }

  /** setContents
   * 
   * @param pContents
   * @throws WFP_Exception 
   */
  @Override
  public void setContents( final String pContents ) throws WFP_Exception {
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream( pContents.getBytes() );
      final Binary bin = getNode().getSession().getValueFactory().createBinary( bais );
      getNode().setProperty( "jcr:data", bin );      
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }    
    return;
  }

  /** setContents
   * 
   * @param pReader
   * @throws WFP_Exception 
   */
  @Override
  public void setContents( final Reader pReader ) throws WFP_Exception {
    try {
      final ReaderInputStream ris = new ReaderInputStream( pReader );
      final Binary bin = getNode().getSession().getValueFactory().createBinary( ris );
      getNode().setProperty( "jcr:data", bin );      
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return;
  }
  

  @Override
  public void setContents( final InputStream pInput ) throws WFP_Exception {
    try {
      final Binary bin = getNode().getSession().getValueFactory().createBinary( pInput );
      getNode().setProperty( "jcr:data", bin );      
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }    
    return;
  }
  

  /** setContents
   * 
   * @param pObject
   * @throws WFP_Exception 
   */
  @Override
  public void setContents( final Object pObject ) throws WFP_Exception {
//    try {
//      Binary bin = mOutput.getSession().getValueFactory().createBinary(  )
//      mOutput.setProperty( "jcr:data", bin );
//    } catch( RepositoryException re ) {
//      throw new WFP_Exception( re );
//    }
    return;
  }
  
  @Override
  public void closeStream() throws WFP_Exception {
    try {
      mContext.save();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return;
  }

}
