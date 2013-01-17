/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

import java.net.HttpURLConnection;
import org.jecars.client.nt.JC_EventNode;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author weert
 */
public class JC_EventsAppTest {

    public JC_EventsAppTest() {
      super();
    }

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
    }

    /**
     *
        http://localhost:8080/cars/JeCARS/ApplicationSources/EventsApp/blabla?X-HTTP-Method-Override=POST&jcr:primaryType=jecars:Event&X-EventPath=/JeCARS/default/Events/System/jecars:EventsFINE&Type=QUERY&Category=DEF&Title=Een URL Test&Body=met een body
     */
    @Test
    public void testEventApp1() throws JC_Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      final JC_UsersNode un = c.getUsersNode();
      final JC_UserNode teu;
      if (un.hasUser( "testEventUser" )) {
        teu = un.getUser( "testEventUser" );
      } else {
        teu = un.addUser( "testEventUser", "testEventApp1", "test".toCharArray(), JC_PermissionNode.RS_READACCESS );
      }
      un.save();
      final JC_GroupsNode gn = c.getGroupsNode();
      final JC_GroupNode drg = gn.getGroup( "DefaultReadGroup" );
      drg.addUser( teu );
      drg.save();

      try {
        final JC_Clientable testc = getClient();
        testc.setCredentials( "testEventUser", "test".toCharArray() );
        assertTrue( testc.isServerAvailable() );

        final JC_EventsApp eventApp = new JC_EventsApp( testc );
        try {
          eventApp.createEvent(
                "/JeCARS/default/Events/System/jecars:EventsFINE", "testEventApp1", "The body\nmessage", null, null, "QUERY", "DEF" );
          assertTrue( "Shouldn't have permission", false );
        } catch( JC_HttpException he ) {
          assertEquals( HttpURLConnection.HTTP_NOT_FOUND, he.getHttpErrorCode().getErrorCode() );
        }
        final JC_GroupNode group = gn.getGroup( "EventsAppUsers" );
        group.addUser( teu );
        group.save();
        for( int i=0; i<100; i++ ) {
          final JC_EventNode event = eventApp.createEvent(
                "/JeCARS/default/Events/System/jecars:EventsFINE", "testEventApp1", "The body\nmessage", null, null, "QUERY", "DEF" );
          System.out.println("event path = " + event.getPath() );
        }
      } finally {
        un.removeUser( teu );
      }
      return;
    }

}