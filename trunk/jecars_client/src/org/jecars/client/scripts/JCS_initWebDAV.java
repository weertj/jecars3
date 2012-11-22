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

/** JCS_initWebDAV
 *
 * @version  $Id: JCS_initWebDAV.java,v 1.1 2009/05/14 12:29:22 weertj Exp $
 */
public class JCS_initWebDAV {

  public String mJeCARSServer = "http://localhost:8084/cars";
  public String mUsername = "Administrator";
  public String mPassword = "admin";

  /** startInit
   * 
   */
  public void startInit() throws Exception {
      
    JC_Clientable client = JC_Factory.createClient( mJeCARSServer );
    client.setCredentials( mUsername, mPassword.toCharArray() );          

    createGroups( client );
    
    return;
  }
  
  public void createGroups( JC_Clientable pClient ) throws Exception {
    // **** Create the standard groups        
    JC_Nodeable jecars      = pClient.getNode( "/JeCARS" );
    JC_Nodeable groups      = pClient.getNode( "/JeCARS/default/Groups" );
    JC_Nodeable defaultNode = groups.getParent();
    JC_Nodeable users       = pClient.getNode( "/JeCARS/default/Users" );
    JC_Nodeable data        = pClient.getNode( "/JeCARS/default/Data" );
    JC_Nodeable apps        = pClient.getNode( "/JeCARS/ApplicationSources" );
    JC_Nodeable us          = pClient.getNode( "/JeCARS/UserSources" );
    JC_Nodeable gs          = pClient.getNode( "/JeCARS/GroupSources" );
    JC_Nodeable perm;

    users.Dav_enable();
    users.Dav_setDefaultFolderType( "jecars:User" );
    users.save();
    
    return;
  }
  
    
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      JCS_initWebDAV initJ = new JCS_initWebDAV();
      initJ.mJeCARSServer = "http://localhost:8080/cars";
      initJ.startInit();
    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }
    
}
