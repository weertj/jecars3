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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Utils;

/**
 *
 * @author weert
 */
public class WFP_Input extends WFP_Node implements IWFP_Input {

  private final transient String    mName;
//  private final transient Node      mInput;
  private       transient Binary    mCurrentBinary;

  /** WFP_Input
   * 
   * @param pNode
   * @throws RepositoryException 
   */
  public WFP_Input( final Node pNode ) throws RepositoryException {
    super( pNode );
//    mInput = pNode;
    mName  = pNode.getName();
    return;
  }

  /** getName
   * 
   * @return 
   */
  @Override
  public String getName() {
    return mName;
  }



  /** setContents
   * 
   * @param pIS
   * @throws WFP_Exception 
   */
  @Override
  public void setContents( final InputStream pIS ) throws WFP_Exception {
    try {
      final Node input = CARS_Utils.getLinkedNode( getNode() );
      final Binary bin = input.getSession().getValueFactory().createBinary( pIS );
      input.setProperty( "jcr:data", bin );
      input.save();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );        
    }    
    return;
  }
  
  /** getContentsAsString
   * 
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public String getContentsAsString() throws WFP_Exception {
    try {
      InputStream is = openStream();
      return CARS_Utils.readAsString( is );
    } catch( IOException ie ) {
      throw new WFP_Exception( ie );
    } finally {
      closeStream();
    }
    
  }

  
  /** openStream
   * 
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public InputStream openStream() throws WFP_Exception {
    try {
      if (mCurrentBinary==null) {
        final Node input = CARS_Utils.getLinkedNode( getNode() );
        if (input.hasProperty( "jecars:PathToFile" )) {
          String ptf = input.getProperty( "jecars:PathToFile" ).getString();
          final FileInputStream fis = new FileInputStream( ptf );
          mCurrentBinary = input.getSession().getValueFactory().createBinary( fis );
        } else {
          mCurrentBinary = input.getProperty( "jcr:data" ).getBinary();
        }
      }
      return mCurrentBinary.getStream();
    } catch( FileNotFoundException fn ) {
      throw new WFP_Exception( fn );        
    } catch( RepositoryException ex) {
      throw new WFP_Exception( ex );
    }
  }

  /** closeStream
   * 
   * @throws WFP_Exception 
   */
  @Override
  public void closeStream() throws WFP_Exception {
    if (mCurrentBinary!=null) {
      mCurrentBinary.dispose();
      mCurrentBinary = null;
    }
  }

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Property setProperty( final String pName, final String pValue ) throws WFP_Exception {
    try {
      if (getNode().canAddMixin( "jecars:mixin_unstructured" )) {
        getNode().addMixin( "jecars:mixin_unstructured" );
      }
      return new WFP_Property( getNode().setProperty( pName, pValue ) );
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  
  /** save
   * 
   * @throws WFP_Exception 
   */
  @Override
  public void save() throws WFP_Exception {
    try {
      getNode().save();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }
    
}
