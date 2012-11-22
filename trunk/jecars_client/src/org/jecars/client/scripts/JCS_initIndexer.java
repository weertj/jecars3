/*
 * Copyright 2009 NLR - National Aerospace Laboratory
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

package org.jecars.client.scripts;

import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_Nodeable;

/** JCS_initIndexer
 *
 * @version  $Id$
 */
public class JCS_initIndexer {

  public String mJeCARSServer = "http://localhost:8084/cars";
  public String mUsername = "Administrator";
  public String mPassword = "admin";

  /** startInit
   * 
   */
  public void startInit() throws Exception {
      
    JC_Clientable client = JC_Factory.createClient( mJeCARSServer );
    client.setCredentials( mUsername, mPassword.toCharArray() );          

    createIndexer( client );
    
    return;
  }
  
  /** createIndexer
   * 
   * @param pClient
   * @throws Exception
   */
  public void createIndexer( final JC_Clientable pClient ) throws Exception {
    JC_Nodeable tools      = pClient.getNode( "/JeCARS/default/jecars:Tools" );
    if (!tools.hasNode( "Indexer" )) {
      JC_Nodeable indexer = tools.addNode( "Indexer", "jecars:datafolder" );
      tools.save();
    }
    return;
  }
  
    
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      JCS_initIndexer initJ = new JCS_initIndexer();
      initJ.mJeCARSServer = "http://localhost:8080/cars";
      initJ.startInit();
    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }
    
}
