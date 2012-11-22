/*
 * Copyright 2007-2012 NLR - National Aerospace Laboratory
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
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_TextEntries
 * 
 * A very simple output generator
 *
 * @version $Id: CARS_OutputGenerator_TextEntries.java,v 1.3 2008/08/01 15:22:37 weertj Exp $
 */
public class CARS_OutputGenerator_TextEntries extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
   
  static final public String LF = "\n";
    
  /** Creates a new instance of CARS_OutputGenerator_TextEntries
   */
  public CARS_OutputGenerator_TextEntries() {
  }
  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pContext.setContentType( "text/plain" );
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

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   */   
  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {
    pMessage.append( pChildNode.getName() ).append( LF ); 
    return;
  }
  
  /** Is this output a RSS/Atom feed type
   * @return true for yes
   */   
  @Override
  public boolean isFeed() {
    return false;
  }

  
}
