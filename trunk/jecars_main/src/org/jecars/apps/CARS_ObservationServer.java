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
package org.jecars.apps;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import org.jecars.CARS_Factory;

/**
 *
 * @author weert
 */
final public class CARS_ObservationServer {

  static final private Map<String, OServer> SERVERS = new HashMap<String, OServer>();


  /** OServer
   *
   */
  static private class OServer implements EventListener {
    final private transient String  mAddress;
    final private transient int     mPort;
    final private transient String  mPath;
    final private transient boolean mIsDeep;

    private InetAddress    mInetAddress    = null;
    private DatagramSocket mDatagramSocket = null;

    /** OServer
     *
     * @param pPort
     * @param pPath
     * @param pIsDeep
     */
    public OServer( final String pAddress, final int pPort, final String pPath, final boolean pIsDeep ) {
      mAddress = pAddress;
      mPort    = pPort;
      mPath    = pPath;
      mIsDeep  = pIsDeep;
      return;
    }

    /** start
     *
     * @param pWorkspace
     * @throws RepositoryException
     */
    public void start( final Workspace pWorkspace ) throws RepositoryException, UnknownHostException, SocketException {
      mInetAddress = InetAddress.getByName( mAddress );
      mDatagramSocket = new DatagramSocket();
      CARS_Factory.getSystemApplicationSession().getWorkspace().getObservationManager().addEventListener(
//      pWorkspace.getObservationManager().addEventListener(
                        this,
                        Event.NODE_ADDED|Event.NODE_MOVED|Event.NODE_REMOVED|Event.PROPERTY_ADDED|Event.PROPERTY_CHANGED|Event.PROPERTY_REMOVED,
                        mPath, mIsDeep, null, null, false );
      return;
    }

    /** onEvent
     *
     * @param ei
     */
    @Override
    public void onEvent( final EventIterator ei ) {
      if (ei!=null) {
        while( ei.hasNext() ) {
          try {
            final Event e = ei.nextEvent();
            final String event = e.toString();
            System.out.println("EVENT: = " + event );
            final byte[] data = event.getBytes();
            final DatagramPacket packet = new DatagramPacket( data, data.length, mInetAddress, mPort );
            mDatagramSocket.send( packet );
          } catch( IOException ie ) {
            ie.printStackTrace();
          }
        }
      }
      return;
    }

  }

  /** CARS_ObservationServer
   *
   */
  private CARS_ObservationServer() {
    return;
  }

  static public String startObservation( final Node pOBS ) throws RepositoryException, UnknownHostException, SocketException {
    String result = "";
    final NodeIterator ni = pOBS.getNodes();
    while( ni.hasNext() ) {
      final Node n = ni.nextNode();
      result += n.getPath() + '\n';
      final String obsPath = n.getProperty( "jecars:Obs_Path" ).getString();
      if (!SERVERS.containsKey( obsPath )) {
        final OServer os = new OServer(
                          n.getProperty( "jecars:Obs_Address" ).getString(),
                          (int)n.getProperty( "jecars:Obs_Port" ).getLong(),
                          obsPath,
                          n.getProperty( "jecars:Obs_IsDeep" ).getBoolean() );
        SERVERS.put( obsPath, os );
        final OServer oserver = SERVERS.get( obsPath );
        oserver.start( pOBS.getSession().getWorkspace() );
      }
    }
    return result;
  }

}
