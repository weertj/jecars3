/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.EnumSet;
import java.util.List;
import org.jecars.client.observation.JC_Event;
import org.jecars.client.observation.JC_EventListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_ObservationManagerTest
 *
 * @author weert
 */
public class JC_ObservationManagerTest implements JC_EventListener {

    private JC_Clientable mClient_Admin = null;


    public JC_ObservationManagerTest() {
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
        mClient_Admin.setCredentials( "Administrator", "admin".toCharArray() );
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
    }

//    @Test
    public void udpObservationTest() throws Exception {


      byte[] addr = new byte[4];
      addr[0] = (byte)255;
      InetAddress iad = Inet4Address.getByAddress( addr );
      SocketAddress sad = new InetSocketAddress( iad, 4444 );
      DatagramSocket dsocket = new DatagramSocket( sad );
      byte[] buffer = new byte[1024];

      DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
      int i = 5;
      while( i>0 ) {
        dsocket.receive( packet );

        String event = new String( buffer, 0, packet.getLength() );
        System.out.println( i + " EVENT: " + event );

        i--;
      }

      return;
    }

    @Test
    public void nodeAddedTest() throws Exception {
      JC_Nodeable node = mClient_Admin.getRootNode().getNode( "JeCARS/default/Data" );
      try {
        final JC_Nodeable testNode = node.addNode( "JC_ObservationManagerTest", "jecars:datafile" );
        testNode.save();
        mClient_Admin.getObservationManager().addEventListener( this, EnumSet.of(JC_Event.TYPE.NODE_ADDED), testNode.getPath_JC(), false, 1, true );
        Thread.sleep( 2000 );
        testNode.setExpireDate( 20 );
        testNode.save();
        testNode.refresh();
        Thread.sleep( 5000 );
      } finally {
        mClient_Admin.getObservationManager().removeEventListener( this );
        node.getNode( "JC_ObservationManagerTest" ).removeNode();
        node.save();
      }
      return;
    }


//    @Test
//    public void nodeChangedTest() throws Exception {
//      JC_Nodeable node = mClient_Admin.getRootNode().getNode( "JeCARS/tshare/projects/testProject1/pipelines/Pipeline_PrebucklingStiffness1" );
//      try {
//        JC_Nodeable obsNode = node.getNode( "graph_1.png" );
//        mClient_Admin.getObservationManager().addEventListener(
//                        this, EnumSet.of(JC_Event.TYPE.NODE_CHANGED), obsNode.getPath_JC(), false, 1, false );
//        Thread.sleep( 60000 );
//      } finally {
//        mClient_Admin.getObservationManager().removeEventListener( this );
//      }
//      return;
//    }

    @Override
    public void onEvent(List<JC_Event> pEvent) {
      for (JC_Event event : pEvent) {
        System.out.println( "event ID = " + event.getIdentifier() + " " + event.getPath() + " on " + event.getDate().getTime() + " what " + event.getType() );
      }
      return;
    }

}