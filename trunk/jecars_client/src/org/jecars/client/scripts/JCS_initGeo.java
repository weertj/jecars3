/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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
public class JCS_initGeo {

  public String mJeCARSServer = "http://localhost:8080/cars";
  public String mUsername = "Administrator";
  public String mPassword = "admin";

  /** startInit
   * 
   */
  public void startInit() throws Exception {
      
    JC_Clientable client = JC_Factory.createClient( mJeCARSServer );
    client.setCredentials( mUsername, mPassword.toCharArray() );          

    createGeo( client );
    
    return;
  }
  
  /** createIndexer
   * 
   * @param pClient
   * @throws Exception
   */
  public void createGeo( final JC_Clientable pClient ) throws Exception {
    final JC_Nodeable data = pClient.getNode( "/JeCARS/default/Data" );
    if (!data.hasNode( "geo" )) {
      data.addNode( "geo", "jecars:datafolder" );
      data.save();
    }
    final JC_Nodeable geo = data.getNode( "geo" );
    if (!geo.hasNode( "osmimport" )) {
      JC_Nodeable oimp = geo.addNode( "osmimport", "jecars:CARS_Interface" );
      oimp.setProperty( "jecars:InterfaceClass", "org.jecars.osm.app.CARS_OSMImporterApp" );
      oimp.save();
    }

    return;
  }
  
    
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      JCS_initGeo initJ = new JCS_initGeo();
      initJ.mJeCARSServer = "http://localhost:8080/cars";
      initJ.startInit();
    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }
    
}
