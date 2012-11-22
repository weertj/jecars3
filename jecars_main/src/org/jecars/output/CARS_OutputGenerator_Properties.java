/*
 * Copyright 2008-2012 NLR - National Aerospace Laboratory
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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_Properties
 * 
 * A properties output generator
 *
 * @version $Id: CARS_OutputGenerator_Properties.java,v 1.4 2009/03/05 16:03:01 weertj Exp $
 */
public class CARS_OutputGenerator_Properties extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
   
  static final public String LF = "\n";
    
  /** Creates a new instance of CARS_OutputGenerator_TextEntries
   */
  public CARS_OutputGenerator_Properties() {
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
    String pre = pNodeNo + ".";
    pMessage.append( pre ).append( "NAME=" ).append( pChildNode.getName() ).append( LF ); 
    pMessage.append( pre ).append( "NT=" ).append( pChildNode.getPrimaryNodeType().getName() ).append( LF );
    JD_Taglist tags = pContext.getQueryPartsAsTaglist();
    String gap = (String)tags.getData( "jecars:getAllProperties" );
    if ((gap!=null) && (gap.equals( "true" ))) {
      PropertyIterator pi = pChildNode.getProperties();
      Property p;
      while (pi.hasNext()) {
        p = pi.nextProperty();
        if (p.getDefinition().isMultiple()==false) {
          pMessage.append( pre ).append( p.getName() ).append( '=' ).append( CARS_Utils.encode(p.getValue().getString()) ).append( LF );
        } else {
          // **** TODO: multiple properties not supported
        }
      }
    }
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
