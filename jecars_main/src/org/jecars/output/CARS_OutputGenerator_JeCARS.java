/*
 * Copyright 2008-2013 NLR - National Aerospace Laboratory
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

import java.io.ByteArrayOutputStream;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_ActionContext;
import org.jecars.backup.JB_ExportData;
import org.jecars.backup.JB_Options;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_JeCARS
 * 
 * A properties output generator
 *
 * @version $Id: CARS_OutputGenerator_Backup.java,v 1.1 2008/10/21 10:13:46 weertj Exp $
 */
public class CARS_OutputGenerator_JeCARS extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
   
  static final public String LF = "\n";
    
  /** Creates a new instance of CARS_OutputGenerator_Backup
   */
  public CARS_OutputGenerator_JeCARS() {
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
 
  /** addThisNodeEntry
   * 
   * @param pContext
   * @param pMessage
   * @param pThisNode
   * @param pFromNode
   * @param pToNode
   * @throws javax.jcr.RepositoryException
   * @throws java.lang.Exception
   */
  @Override
  public void addThisNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pThisNode,
                                long pFromNode, long pToNode ) throws RepositoryException, Exception {    
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();      
    final JB_ExportData export = new JB_ExportData();
    final JB_Options options = new JB_Options();
    options.setOnlyOneLevelChildren(true);
//    options.setOnlyOneLevel( true );
    export.exportToStreamAsJeCARS( pThisNode, baos, options );
    baos.close();
    pMessage.append( baos.toString() );    
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
