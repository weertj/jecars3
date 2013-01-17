/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

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
public class JC_IdentityTest {

    private JC_Clientable mClient_Admin = null;

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
    }


    public JC_IdentityTest() {
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
    public void testIdentity1() throws JC_Exception {
      JC_Clientable client = getClient();
      client.setCredentials( "Administrator", "admin".toCharArray() );
      JC_Nodeable userSources = client.getNode( "/JeCARS/UserSources" );
      if (userSources.hasNode( "jecars" )) {
        userSources.getNode( "jecars" ).removeNode();
        userSources.save();
      }
      JC_Nodeable userSource = userSources.addNode( "jecars", "jecars:UserSource" );
      userSource.setProperty( "jecars:RepositoryClass", "JeCARS_Araza,http://localhost:8080/helena/cars/" );
      userSource.setProperty( "jecars:LoginName", "UserManager" );
      userSource.setProperty( "jecars:LoginPassword", "jecars" );
      userSource.save();

      // **** Create user with destination Helena
      final JC_UsersNode users = client.getUsersNode();
      if (users.hasUser( "multiuser" )) {
        users.removeUser( "multiuser" );
      }
      final JC_UserNode user = users.addUser( "multiuser", "MultiUser", "multir".toCharArray(), null );
      user.addMixin( "jecars:principalexport" );
      user.save();
      user.setProperty( "jecars:Dest", "/JeCARS/UserSources/jecars" );
      user.save();
      return;
    }

}