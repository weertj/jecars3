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
package org.jecars.output;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_ActionContext;
import org.jecars.binary.CB_Message;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_Binary
 *
 */
public class CARS_OutputGenerator_Binary extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {

  /** Creates a new instance of CARS_OutputGenerator_Binary
   */
  public CARS_OutputGenerator_Binary() {
  }

  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pContext.setContentType( "application/x-jecarsbinary" );
    return;
  }
  
 /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    return;
  }

  /** Add node entry, the current (this) node
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pThisNode the this node
   * @param pFromNode from node number
   * @param pToNode to node number
   */   
  @Override
  public void addThisNodeEntry( final CARS_ActionContext pContext, final CARS_Buffer pMessage, final Node pThisNode,
                                final long pFromNode, final long pToNode ) throws RepositoryException, Exception {

     
//    pMessage.append( pre ).append( "NAME=" ).append( pChildNode.getName() ).append( LF ); 
//    pMessage.append( pre ).append( "NT=" ).append( pChildNode.getPrimaryNodeType().getName() ).append( LF );

      
    return;
  }

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   */   
  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {
    return;
  }

  
}
