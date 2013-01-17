/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

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

/**
 *
 * @author weert
 */
public class JC_PermissionsTest {

    private JC_Clientable mClient_Admin = null;

    public JC_PermissionsTest() {
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
        mClient_Admin = JC_ClientTarget.getClient();
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void getRightsTest() throws Exception {
      final JC_Clientable client = JC_ClientTarget.getClient();
      client.setCredentials( "UserManager", "jecars".toCharArray() );

      final JC_Rights rights = client.getRights( "/JeCARS" );
      assertTrue(  rights.hasRight( JC_Rights.R_READ ) );
      assertFalse( rights.hasRight( JC_Rights.R_ADDNODE ) );
      return;
    }


    @Test
    public void cacheDenyPermissionTest1() throws Exception {
      
      final JC_Nodeable adminData = mClient_Admin.getRootNode().getNode( "/JeCARS/default/Data" );
      final JC_UsersNode  users  = mClient_Admin.getUsersNode();
      final JC_GroupsNode groups = mClient_Admin.getGroupsNode();
      final JC_UserNode testUser;
      if (users.hasUser( "test" )) {
        testUser = users.getUser( "test" );
      } else {
        testUser = users.addUser( "test", "test", "test".toCharArray(), JC_PermissionNode.RS_ALLRIGHTS );
        final JC_GroupNode drg = groups.getGroup( "DefaultReadGroup" );
        drg.addUser( testUser );
        drg.save();
      }
      try {
        final JC_Nodeable cdpt;
        if (adminData.hasNode( "cacheDenyPermissionTest1" )) {
          adminData.getNode( "cacheDenyPermissionTest1" ).removeNode();
          adminData.save();
          cdpt = adminData.addNode( "cacheDenyPermissionTest1", "jecars:datafolder" );
          adminData.save();
        } else {
          cdpt = adminData.addNode( "cacheDenyPermissionTest1", "jecars:datafolder" );
          adminData.save();
        }

        final JC_Clientable testUserClient = JC_ClientTarget.getClient();
        testUserClient.setCredentials( "test", "test".toCharArray() );
        final JC_Nodeable df = null;
        try {
          // **** Read without permissions
          testUserClient.getRootNode().getNode( "/JeCARS/default/Data/cacheDenyPermissionTest1" );
        } catch( JC_Exception e ) {
        }
        assertEquals( null, df );

        // **** Add permission
        cdpt.addPermissionNode( "P_test", testUser, JC_PermissionNode.RS_READACCESS );
        cdpt.save();

        // **** Read with permissions
        testUserClient.getRootNode().getNode( "/JeCARS/default/Data/cacheDenyPermissionTest1" );
      } finally {
        testUser.removeNodeForced();
        testUser.save();
        adminData.getNode( "cacheDenyPermissionTest1" ).removeNode();
        adminData.save();
      }
      return;
    }


    @Test
    public void getPropertyRight1() throws Exception {

      final JC_Nodeable adminData = mClient_Admin.getRootNode().getNode( "/JeCARS/default/Data" );
      final JC_UsersNode  users  = mClient_Admin.getUsersNode();
      final JC_GroupsNode groups = mClient_Admin.getGroupsNode();
      final JC_UserNode testUser;
      if (users.hasUser( "test" )) {
        testUser = users.getUser( "test" );
      } else {
        testUser = users.addUser( "test", "test", "test".toCharArray(), JC_PermissionNode.RS_ALLRIGHTS );
        final JC_GroupNode drg = groups.getGroup( "DefaultReadGroup" );
        drg.addUser( testUser );
        drg.save();
      }
      try {
        final JC_Nodeable cdpt;
        if (adminData.hasNode( "getPropertyRight1" )) {
          adminData.getNode( "getPropertyRight1" ).removeNode();
          adminData.save();
          cdpt = adminData.addNode( "getPropertyRight1", "jecars:datafolder" );
          adminData.save();
        } else {
          cdpt = adminData.addNode( "getPropertyRight1", "jecars:datafolder" );
          adminData.save();
        }
        cdpt.addMixin( "jecars:mixin_unstructured" );
        cdpt.save();
        cdpt.setProperty( "jecars:GETPROPERTYTEST", "CanRead" );
        cdpt.save();
        cdpt.removeMixin( "jecars:mixin_unstructured" );
        cdpt.save();
        cdpt.refresh();
        assertFalse( cdpt.hasProperty( "jecars:GETPROPERTYTEST" ) );
        cdpt.save();
        cdpt.addMixin( "jecars:mixin_unstructured" );
        cdpt.save();
        cdpt.setProperty( "jecars:GETPROPERTYTEST", "CanRead" );
        cdpt.save();

        // **** Add permission
        cdpt.addPermissionNode( "P_test", testUser, JC_PermissionNode.RS_READACCESS );
        cdpt.save();

        final JC_Clientable testUserClient = JC_ClientTarget.getClient();
        testUserClient.setCredentials( "test", "test".toCharArray() );

        // **** Read with READ and GETPROPERTY permission
        JC_Nodeable n = testUserClient.getRootNode().getNode( "/JeCARS/default/Data/getPropertyRight1" );
        assertTrue( n.hasProperty( "jecars:GETPROPERTYTEST" ) );
        assertEquals( "CanRead", n.getProperty( "jecars:GETPROPERTYTEST" ).getValueString() );

        // **** Add permission
        final JC_PermissionNode pn = (JC_PermissionNode)cdpt.getNode( "P_test" ).morphToNodeType();
        pn.setRights( JC_PermissionNode.RS_FOLDERACCESS );
        pn.save();

        try {
          // **** Read with READ permission
          n = testUserClient.getRootNode().getNode( "/JeCARS/default/Data/getPropertyRight1" );
          assertFalse( n.hasProperty( "jecars:GETPROPERTYTEST" ) );
          assertEquals( "CanRead", n.getProperty( "jecars:GETPROPERTYTEST" ).getValueString() );
          assertTrue( "Shouldn't be able to read GETPROPERTYTEST", false );
        } catch( JC_Exception e ) {
        }

        // **** Read with permissions
        testUserClient.getRootNode().getNode( "/JeCARS/default/Data/getPropertyRight1" );
      } finally {
//        testUser.removeNodeForced();
//        testUser.save();
//        adminData.getNode( "getPropertyRight1" ).removeNode();
//        adminData.save();
      }
      return;
    }



}