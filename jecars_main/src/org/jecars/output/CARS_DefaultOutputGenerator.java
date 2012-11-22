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
 * CARS_DefaultOutputGenerator
 *
 * @version $Id: CARS_DefaultOutputGenerator.java,v 1.4 2009/06/21 20:59:41 weertj Exp $
 */
public class CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
    
  /** Creates a new instance of CARS_DefaultOutputGenerator
   */
  public CARS_DefaultOutputGenerator() {
  }
  
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    return;
  }
  
  @Override
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    return;
  }

  @Override
  public void addThisNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pThisNode,
                                long pFromNode, long pToNode ) throws RepositoryException, Exception {    
    return;
  }

  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {
    return;
  }
  
  /** Is this output a RSS/Atom feed type
   * @return true for yes
   */   
  @Override
  public boolean isFeed() {
    return true;
  }

  
}
