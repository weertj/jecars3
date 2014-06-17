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
package org.jecars.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_UsersTests
 *
 * @version $Id: JC_UsersTests.java,v 1.5 2009/07/22 09:02:16 weertj Exp $
 */
public class JC_UsersTests {

    private JC_Clientable mClient_Admin = null;

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
    }

    public JC_UsersTests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
      try {
        mClient_Admin = getClient();
        mClient_Admin.setCredentials( "UserManager", "jecars".toCharArray() );
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
    }

  @Test
  public void adminPermissionTest() throws Exception {
    JC_Clientable noaccess = getClient();
    noaccess.setCredentials( "Administrator", "adminNOT".toCharArray() );
    JC_Nodeable root = noaccess.getRootNode();
    try {
      JC_Nodeable jecars = root.getNode( "JeCARS" );
    } catch( JC_HttpException je ) {
      assertEquals( HttpURLConnection.HTTP_UNAUTHORIZED, je.getHttpErrorCode().getErrorCode() );
    }
    assertFalse( noaccess.validCredentials() );
    noaccess = getClient();
    noaccess.setCredentials( "Administrator", "admin".toCharArray() );
    root = noaccess.getRootNode();
    JC_Nodeable jecars = root.getNode( "JeCARS" );
    assertTrue( noaccess.validCredentials() );

    return;
  }

  @Test
  public void gdataAuthTest1() throws Exception {
    final JC_Clientable c = getClient();
    c.setCredentials( "UserManager", "jecars".toCharArray() );
    JC_GDataAuth auth = c.retrieveGDataAuth();
    System.out.println(auth.getAuth());
    auth = c.retrieveGDataAuth();
    System.out.println(auth.getAuth());
    c.setCredentials( auth );
    final JC_Nodeable root = c.getRootNode();
    root.getNode( "JeCARS/default/Users" );
    return;
  }

  @Test
  public void gdataAuthTest2() throws Exception {
    JC_Clientable c = getClient();
    c.setCredentials( "UserManager", "jecars".toCharArray() );
    JC_GDataAuth auth = c.retrieveGDataAuth();
    System.out.println(auth.getAuth());
    c.setCredentials( null, null );
    c.setCredentials( auth );
    JC_Nodeable root = c.getRootNode();
    JC_Nodeable jecars = root.getNode( "JeCARS/default/Users" );
    return;
  }

  @Test
  public void gdataAuthTest3() throws Exception {
    JC_Clientable c = getClient();
    c.setCredentials( "UserManager", "jecars".toCharArray() );
    JC_GDataAuth auth = c.retrieveGDataAuth();
    String authS = auth.getAuth();
    c = getClient();
    c.setCredentials( JC_GDataAuth.create( authS ) );
    JC_Nodeable root = c.getRootNode();
    JC_Nodeable jecars = root.getNode( "JeCARS/default/Users" );
    return;
  }

  @Test
  public void getGroupTest() throws Exception {
    JC_Nodeable     rootNode = mClient_Admin.getRootNode();
    JC_GroupsNode groupsNode = (JC_GroupsNode)rootNode.getNode( "JeCARS/default/Groups" ).morphToNodeType();
    try {
      JC_GroupNode bn = groupsNode.getGroup( "" );
    } catch( JC_Exception e ) {
      assertEquals( "", "Cannot find path .", e.getMessage() );
    }
    return;
  }

  @Test
  public void listUsersTest1() throws Exception {
    final JC_Clientable c = getClient();
    c.setCredentials( "UserManager", "jecars".toCharArray() );
    final JC_UsersNode un = c.getUsersNode();
    System.out.println("Users = " + un.getPath() + " = " + un.getName() );
    final Collection<JC_UserNode> users = un.getUsers();
    for (JC_UserNode user : users) {
      System.out.println("User = " + user.getPath() + " = " + user.getFullname() );
    }
    return;
  }


  @Test
  public void userDataCreateTest() throws Exception {
    JC_Nodeable     rootNode = mClient_Admin.getRootNode();
    JC_Nodeable   eventsNode = rootNode.getNode( "JeCARS/default/Data" );
    JC_UsersNode   usersNode = (JC_UsersNode)rootNode.getNode( "JeCARS/default/Users" ).morphToNodeType();
    JC_GroupsNode groupsNode = (JC_GroupsNode)rootNode.getNode( "JeCARS/default/Groups" ).morphToNodeType();
    JC_UserNode eventUser = null;
    JC_GroupNode eventGroup = null;
    JC_PermissionNode eventGroupPerm = null;
    if (usersNode.hasUser( "testUser" )==true) eventUser = usersNode.getUser( "testUser" );
    if (groupsNode.hasGroup( "testGroup" )==true) eventGroup = groupsNode.getGroup( "testGroup" );
    if (eventsNode.hasNode( "P_testRights" )==true) {
      eventGroupPerm = (JC_PermissionNode)eventsNode.getNode( "P_testRights" ).morphToNodeType();
      eventGroupPerm.removeNode();
      eventGroupPerm.save();
      eventGroupPerm = null;
    }
    if (eventGroup==null) {
      // **** Create test group
      eventGroup = groupsNode.addGroup( "testGroup" );
      groupsNode.save();
      JC_GroupNode drg = groupsNode.getGroup( "DefaultReadGroup" );
      drg.addGroup( eventGroup );
      drg.save();
    }
    if (eventUser==null) {
      // **** Create event user
      eventUser = usersNode.addUser( "testUser", "Test User", "testUser".toCharArray(), JC_PermissionNode.RS_ALLREADACCESS );
      usersNode.save();
    }
    try {

      // **** login as testUser
      JC_Clientable client = getClient();
      client.setCredentials( "testUser", "testUser".toCharArray() );
      assertFalse( client.getRootNode().hasNode( "JeCARS" ) );

      if (eventGroupPerm==null) {
//      eventGroupPerm = (JC_PermissionNode)eventsNode.addNode( "P_testRights", "jecars:Permission" ).morphToNodeType();
//      eventGroupPerm.save();
//      eventGroupPerm.addRights( eventGroup, JC_PermissionNode.RS_ALLREADACCESS );
//      eventGroupPerm.save();
      }
      eventGroup.addUser( eventUser );
      eventGroup.save();
      assertTrue( client.getRootNode().hasNode( "JeCARS" ) );
      assertTrue( eventUser.getPath().equals( "/JeCARS/default/Users/testUser" ));
    } finally {
      usersNode.removeUser( eventUser );
      groupsNode.removeGroup( eventGroup );
//      eventGroupPerm.removeNode();
//      eventGroupPerm.save();
    }
    return;
  }

/*
  @Test
  public void readNodes() throws JC_Exception {
    JC_Clientable c = JC_Factory.createClient( "http://davn174u:8080/cars" );
    c.setCredentials( "Administrator", "admin".toCharArray() );
    JC_Nodeable n = c.getRootNode();
    n = n.getNode( "JeCARS/default/Data" );
    JC_Params p = c.createParams( JC_RESTComm.GET );
  p.setOutputFormat( JC_Defs.OUTPUTTYPE_PROPERTIES );
    Collection<JC_Nodeable> nodes = n.getNodes( p, null, null );
//    Collection<JC_Nodeable> nodes = n.getNodes();
    for (JC_Nodeable node : nodes) {
        System.out.println("nanna " + node.getPath() );
    }
      System.out.println("size " + nodes.size() );
    return;
  }
*/
  
}