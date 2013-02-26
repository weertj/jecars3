/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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
 */package org.jecars.wfplugin;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import org.jecars.CARS_Utils;

/**
 *
 * @author weert
 */
public class WFP_ContextParameter extends WFP_Node implements IWFP_ContextParameter{

  private Node mLinkedNode;
    
  public WFP_ContextParameter( Node pNode ) throws RepositoryException {
    super(pNode);
    mLinkedNode = CARS_Utils.getLinkedNode( pNode );
  }

  @Override
  protected Node getNode() {
    return mLinkedNode;
  }  
  
  @Override
  public String getStringValue() throws WFP_Exception {
    try {
      final Value[] values = getNode().getProperty( "jecars:string" ).getValues();
      if (values.length>0) {
        return values[0].getString();
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return null;
  }    

  @Override
  public IWFP_ContextParameter setValue( final String pValue ) throws WFP_Exception {
    try {
      mLinkedNode.setProperty( "jecars:string", (Value[])null );
      CARS_Utils.addMultiProperty( mLinkedNode, "jecars:string", pValue, true );
    } catch( Exception e ) {
      throw new WFP_Exception( e );
    }
    return this;
  }  
  
}
