/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;

/** JCS_initJeCARS
 *
 * @version  $Id: JCS_initJeCARS.java,v 1.12 2009/06/08 09:15:33 weertj Exp $
 */
public class JCS_initJeCARS {

  public String mJeCARSServer = "http://localhost:8888/cars";
  public String mUsername = "Administrator";
  public String mPassword = "admin";

  /** startInit
   * 
   */
  public void startInit() throws Exception {
    JC_Clientable client;
    if (JC_Factory.JECARSLOCAL.equals( mJeCARSServer )) {
      client = JC_Factory.createLocalClient();
    } else {
      client = JC_Factory.createClient( mJeCARSServer );
    }
    client.setCredentials( mUsername, mPassword.toCharArray() );          

    createGroups( client );
    
    return;
  }
  
  public void createGroups( JC_Clientable pClient ) throws Exception {
    // **** Create the standard groups        
    JC_Nodeable jecars      = pClient.getNode( "/JeCARS" );
    JC_GroupsNode groups    = (JC_GroupsNode)pClient.getNode( "/JeCARS/default/Groups" ).morphToNodeType();
    JC_Nodeable defaultNode = groups.getParent();
    JC_Nodeable users       = pClient.getNode( "/JeCARS/default/Users" );
    JC_Nodeable data        = pClient.getNode( "/JeCARS/default/Data" );
    JC_Nodeable apps        = pClient.getNode( "/JeCARS/ApplicationSources" );
    JC_Nodeable us          = pClient.getNode( "/JeCARS/UserSources" );
    JC_Nodeable gs          = pClient.getNode( "/JeCARS/GroupSources" );
    JC_Nodeable perm;
    
    // ***** /Groups
    JC_Nodeable group, user;
    if (!groups.hasNode( "DefaultReadGroup" )) {
       group = groups.addNode( "DefaultReadGroup", "jecars:Group" );
       group.setProperty( "jecars:Title", "Group which has read rights on default objects" );
       group.setProperty( "jecars:Fullname", "DefaultReadGroup" );
       group.setProperty( "jecars:Body", "Members of this group are allowed to read the ../default tree" );
       group.save();       
       perm = defaultNode.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       perm = users.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       perm = groups.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       perm = apps.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       perm = jecars.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       jecars = jecars.getParent();
       perm = jecars.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();       
       perm = data.addNode( "P_DefaultReadGroup", "jecars:Permission" );
       perm.setProperty( "jecars:Actions",  "+read" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();
       
       // **** Add DefaultReadGroup to the /accounts/... path
       perm = pClient.getNode( "/accounts/jecars:P_anonymous" );
       perm.setProperty( "jecars:Principal", "+" + group.getPath() );
       perm.save();

       
    }
        
    if (!groups.hasNode( "UserManagers" )) {
      group = groups.addNode( "UserManagers", "jecars:Group" );
      group.setProperty( "jecars:Fullname", "UserManagers" );
      group.setProperty( "jecars:Body", "Members of this group are allowed to create and change other users" );
      group.save();
    }
    if (!users.hasNode( "UserManager" )) {
      user = users.addNode( "UserManager", "jecars:User" );
      user.setProperty( "jecars:Fullname", "UserManager" );
      user.setProperty( "jecars:Password_crypt", "jecars" );
      user.save();
      group = groups.getNode( "UserManagers" );
      group.setProperty( "jecars:GroupMembers", "+" + user.getPath() );
      group.save();
    }

    if (!users.hasNode( "P_UserManagers" )) {
      perm = users.addNode( "P_UserManagers", "jecars:Permission" );
      perm.setProperty( "jecars:Actions", "+read,add_node,get_property,set_property,remove" );
      perm.setProperty( "jecars:Delegate", "true" );            
      perm.setProperty( "jecars:Principal", "+/JeCARS/default/Groups/UserManagers" );
      perm.save();
      // **** Groups
      perm = groups.addNode( "P_UserManagers", "jecars:Permission" );
      perm.setProperty( "jecars:Actions", "+read,add_node,get_property,set_property,remove" );
      perm.setProperty( "jecars:Delegate", "true" );
      perm.setProperty( "jecars:Principal", "+/JeCARS/default/Groups/UserManagers" );
      perm.save();

      // **** JeCARS/UserSources
      perm = us.addNode( "P_UserManagers", "jecars:Permission" );
      perm.setProperty( "jecars:Delegate", "true" );
      perm.setProperty( "jecars:Actions",  "+read" );
      perm.setProperty( "jecars:Principal", "+/JeCARS/default/Groups/UserManagers" );
      perm.save();
      // **** JeCARS/GroupSources
      perm = gs.addNode( "P_UserManagers", "jecars:Permission" );
      perm.setProperty( "jecars:Delegate", "true" );
      perm.setProperty( "jecars:Actions",  "+read" );
      perm.setProperty( "jecars:Principal", "+/JeCARS/default/Groups/UserManagers" );
      perm.save();
    }
    
        
    // **** Group/Group relations
    JC_Nodeable drg = groups.getNode( "DefaultReadGroup" );
    JC_Nodeable umg = groups.getNode( "UserManagers" );
    drg.setProperty( "jecars:GroupMembers", "+" + umg.getPath() );
    drg.save();                    

    // **** Set toolsapp rights
    JC_Nodeable toolsApp = apps.getNode( "ToolsApp" );
    if (!toolsApp.hasNode( "P_DefaultReadGroup" )) {
      perm = toolsApp.addNode( "P_DefaultReadGroup", "jecars:Permission" );
      perm.setProperty( "jecars:Actions",  "+read" );
      perm.setProperty( "jecars:Principal", "+" + drg.getPath() );
      perm.save();
    }

    // **** Add /JeCARS/apps container and credentials
/*
    if (!jecars.hasNode( "apps" )) {
      jecars.addNode( "apps", "jecars:datafolder" );
      jecars.save();
    }
    final JC_Nodeable userapps = jecars.getNode( "apps" );
    if (!groups.hasGroup( "AppUsers" )) {
      final JC_GroupNode appUsers = groups.addGroup( "AppUsers" );
      appUsers.save();
    }
    final JC_GroupNode appUsers = groups.getGroup( "AppUsers" );
    if (!userapps.hasNode( "P_AppUsers" )) {
      userapps.addPermissionNode( "P_AppUsers", appUsers, JC_PermissionNode.RS_ALLREADACCESS );
      userapps.save();
    }
 * 
 */


//    // **** Add standard Queries container
//    if (jecars.hasNode( "default/Queries" )==false) {
//      JC_Nodeable queries = pClient.getNode( "/JeCARS/default" ).addNode( "Queries", "jecars:Queries" );
//      queries.save();
//    }
    
    // **** Add standard storage application
//    if (apps.hasNode( "LocalStorage" )==false) {
//      JC_Nodeable newapp = apps.addNode( "LocalStorage", "jecars:localstorage" );
//      newapp.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_DirectoryApp" );
//      newapp.setProperty( "jecars:StorageDirectory", "C:/Dev/Data/cars/localstorage" );
//      newapp.save();
//      perm = newapp.addNode( "P_DefaultReadGroup", "jecars:Permission" );
//      perm.setProperty( "jecars:Actions",   "+read" );
//      perm.setProperty( "jecars:Principal", "+/JeCARS/default/Groups/DefaultReadGroup" );
//      perm.save();
//    }
    
    // **** Call to AdminApp
    apps.getNode( "AdminApp" );


    return;
  }
  
    
  /**
   * @param args the command line arguments
   */
  public static void main( final String[] args ) {
    try {
      final JCS_initJeCARS initJ = new JCS_initJeCARS();
//      initJ.mJeCARSServer = JC_LocalClient.JECARSLOCAL;
      initJ.startInit();
    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }
    
}
