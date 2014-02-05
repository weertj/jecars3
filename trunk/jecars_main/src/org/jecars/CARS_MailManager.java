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

package org.jecars;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.commons.mail.SimpleEmail;
import org.jecars.jaas.CARS_Credentials;
import org.jecars.tools.CARS_DefaultToolInterface;

/**
 *
 * @author weert
 */
public class CARS_MailManager extends CARS_DefaultToolInterface {

  private static final Object LOCK = new Object();

  private transient volatile boolean          mMailToBeSend = false;
  private transient Session                   mSession = null;
  private transient long                      mCheckPathLastModified = 0L;
  private transient final Map<Node, String>   mCheckPaths = new ConcurrentHashMap<Node, String>();

  static private final long  CHECKEVERY                 = 22000L; // **** 22 seconds

  /** getCheckPathNode
   *
   * @param pValue
   * @return
   */
  private List<Node> getCheckPathNode( final String pValue ) {
    List<Node> res = new ArrayList<Node>();
    Iterator<Node> it = mCheckPaths.keySet().iterator();
    while( it.hasNext() ) {
      Node n = it.next();
      if (mCheckPaths.get( n ).equals( pValue )) {
        res.add( n );
      }
    }
    return res;
  }

  /** onEvent_Mails
   *
   */
  private class onEvent_Mails implements EventListener {

    /** onEvent
     *
     * @param pEI
     */
    @Override
    public void onEvent( final EventIterator pEI ) {
      if (mSession!=null) {
        Event lastEvent = null;
        try {
          while( pEI.hasNext() ) {
            lastEvent = pEI.nextEvent();
            final String path = lastEvent.getPath();
//            System.out.println("MAIL: EVENT ADDED = " + path );
            final Map<String, String> handled = new HashMap<String, String>();
            final Collection<String> keys = mCheckPaths.values();
            for( final String key : keys) {
//            System.out.println(" EVENT KEY " + key );
              if ((!handled.containsKey( key )) && (path.startsWith( key ))) {
                handled.put( key, key );
//            System.out.println("CREATE MAIL: EVENT ADDED = " + path );
                synchronized(LOCK) {
                  List<Node> linkNodes = getCheckPathNode( key );
                  for( final Node linkNode : linkNodes) {
                    final Node eventNode = mSession.getNode( path );
                    boolean ok = true;
                    if (linkNode.hasProperty( "jecars:EventType" )) {
                      ok = lastEvent.getType()==linkNode.getProperty( "jecars:EventType" ).getLong();
                    }
                    if ((ok) && (linkNode.hasProperty( "jecars:NodeType" ))) {
                      ok = eventNode.isNodeType( linkNode.getProperty( "jecars:NodeType" ).getString() );
                    }
                    if ((ok) && (linkNode.hasProperty( "jecars:User" ))) {
                      final String princ = linkNode.getProperty( "jecars:User" ).getValue().getString();
                      try {
                        final Node princN = mSession.getNode( princ );
                        if (princN.hasProperty( "jecars:Email" ) && princN.hasNode( "jecars:Prefs" )) {
                          final Node prefs = princN.getNode( "jecars:Prefs" );
                          if (!prefs.hasNode( "MailBox" )) {
                            prefs.addNode( "MailBox", "jecars:MailBox" );
                            prefs.save();
                          }
                          final Node mailBox   = prefs.getNode( "MailBox" );
                          final Node sendQueue = mailBox.getNode( "SendQueue" );
                          final UUID uuid = UUID.randomUUID();
                          final Node mail = sendQueue.addNode( "Mail_" + uuid, "jecars:Mail" );
                          mail.setProperty( "jecars:InSendQueue", true );
                          if (eventNode.hasProperty( "jecars:Title" )) {
                            mail.setProperty( "jecars:Title", "JeCARS: " + eventNode.getProperty( "jecars:Title" ).getString() );
                          } else {
                            mail.setProperty( "jecars:Title", "JeCARS: " + eventNode.getPath() );
                          }
                          String body;
                          if (eventNode.hasProperty( "jecars:Body" )) {
                            body = eventNode.getProperty( "jecars:Body" ).getString();
                          } else {
                            body = lastEvent.toString();
                          }
                          body += "\n\n----------\nThis mail was automatically generated\n";
                          mail.setProperty( "jecars:Body", body );
                          CARS_Utils.setExpireDate( mail, (int)mailBox.getProperty( "jecars:NewMailExpire" ).getLong() );
                          CARS_Utils.addMultiProperty( mail, "jecars:To",
                                          princN.getProperty( "jecars:Email" ).getValue().getString(), false );
                          sendQueue.save();
                          mMailToBeSend = true;
                        }
                      } catch( PathNotFoundException e ) {
                        System.out.println("Cannot find user " + princ );
                      } catch( Exception pe ) {
                        pe.printStackTrace();
                        // **** No action, because otherwise it would generate an event
                      }
                    }
                  }
                }
              }
            }
          }
        } catch( Exception e ) {
          if (lastEvent==null) {
            LOG.log( Level.SEVERE, "onEvent", e );
          } else {
            LOG.log( Level.SEVERE, "onEvent:" + lastEvent.toString(), e );
          }
        }
      }
      return;
    }

  }

  /** CARS_MailManager
   *
   * @throws RepositoryException
   */
  public CARS_MailManager() throws RepositoryException {
    super();
    final ObservationManager om = CARS_Factory.getObservationSession().getWorkspace().getObservationManager();
    om.addEventListener( new onEvent_Mails(), Event.NODE_ADDED, "/JeCARS", true, null, null, false );
    return;
  }

  /** shutdown the expire manager
   */
  public void shutdown() {
    try {
      setStateRequest( STATEREQUEST_ABORT );
    } catch (Exception e) {
      LOG.log( Level.WARNING, null, e );
    }
    if (mSession!=null) {
      mSession.logout();
      mSession = null;
    }
    return;
  }


  /** toolRun
   *
   * @throws Exception
   */
  @Override
  protected void toolRun() throws Exception {
    super.toolRun();
    final Node n = getTool();
    try {
        if (mSession==null) {
          mSession = n.getSession().getRepository().login( new CARS_Credentials( CARS_Definitions.gSuperuserName, "".toCharArray(), null ));
        }
        
    //    final Node tool = mSession.getNode( getTool().getPath() );
        final Node mailLinks = mSession.getNode( getTool().getPath() + "/MailLinks" );
        if ((mCheckPathLastModified==0) || (mailLinks.hasProperty( "jecars:Modified" ))) {
          final long mod;
          if (mCheckPathLastModified==0) {
            mod = System.currentTimeMillis();
          } else {
            mod = mailLinks.getProperty( "jecars:Modified" ).getLong();
          }
    //    System.out.println("CHECK MAIL : " + mod);
          if (mCheckPathLastModified!=mod) {
    //    System.out.println(" REFRESH LINKS : " + mod);
            mCheckPathLastModified = mod;
            final NodeIterator links = mailLinks.getNodes();
            mCheckPaths.clear();
            while( links.hasNext() ) {
              final Node link = links.nextNode();
              mCheckPaths.put( link, link.getProperty( "jecars:ObservationPath" ).getValue().getString() );
            }
          }
        }

        // *************************************
        // **** Check for mails
        if (mMailToBeSend) {
          mMailToBeSend = false;
          try {
            synchronized(LOCK) {
              final Node dms = getTool().getNode( "DefaultMailServer" );
              final String smtp = dms.getProperty( "jecars:SMTPHost" ).getString();
              final NodeIterator ni = getMailNodes( mSession.getWorkspace().getQueryManager() );
              while( ni.hasNext() ) {
                final Node mailNode = ni.nextNode();
    //             System.out.println("READY to send == " + mailNode.getPath() );
                final SimpleEmail email = new SimpleEmail();
                email.setHostName( smtp );
                if (mailNode.hasProperty( "jecars:To" )) {
                  final Value[] tos = mailNode.getProperty( "jecars:To" ).getValues();
                  for (Value to : tos) {
                    email.addTo( to.getString() );
                  }
                  email.setFrom( mSession.getUserID() + "@jecars.org", mSession.getUserID() );
                  email.setSubject( mailNode.getProperty( "jecars:Title" ).getString() );
                  email.setMsg( mailNode.getProperty( "jecars:Body" ).getString() );
                  final String result = email.send();
                  final Node mailbox = mailNode.getParent().getParent();
            //        System.out.println("result = " + result );
                  mailNode.setProperty( "jecars:SendResult",  result );
                  mailNode.setProperty( "jecars:SendedAt",    Calendar.getInstance() );
                  mailNode.setProperty( "jecars:InSendQueue", false );
                  CARS_Utils.setCurrentModificationDate( mailNode );
                  CARS_Utils.setExpireDate( mailNode, (int)mailbox.getProperty( "jecars:SendedMailExpire" ).getLong() );
                  mailNode.save();
    //        System.out.println("result " + email.send() );
                  // **** Check if there is a Sended box
                  if (mailbox.isNodeType( "jecars:MailBox" )) {
                    mailbox.setProperty( "jecars:LastMailSend",   Calendar.getInstance() );
                    dms.setProperty(     "jecars:LastMailSend",   Calendar.getInstance() );
                    mailbox.setProperty( "jecars:TotalMailsSend", mailbox.getProperty( "jecars:TotalMailsSend" ).getLong()+1 );
                    dms.setProperty(     "jecars:TotalMailsSend", dms.getProperty( "jecars:TotalMailsSend" ).getLong()+1 );
                    CARS_Utils.setCurrentModificationDate( mailbox );
                    CARS_Utils.setCurrentModificationDate( dms );
                  }
                  if (mailbox.hasNode( "Sended" ) && (mailbox.getNode( "Sended" ).isNodeType( "jecars:Mails" ))) {
                    // **** Move the mail to the sended box
                    mSession.move( mailNode.getPath(), mailbox.getNode( "Sended" ).getPath() + '/' + mailNode.getName() );
                  } else {
                    mailNode.remove();
                  }
                  mSession.save();
                }
              }
              mSession.save();
            }
          } catch( Exception e ) {
            e.printStackTrace();
          } finally {
            mSession.save();
          }
        }
    } finally {
    }
    return;
  }


  /** getMailNodes
   * @param pQM
   * @param pTime
   * @return
   * @throws javax.jcr.query.InvalidQueryException
   * @throws javax.jcr.RepositoryException
   */
  public NodeIterator getMailNodes( final QueryManager pQM ) throws InvalidQueryException, RepositoryException {
    final String qu = "SELECT * FROM jecars:Mail WHERE jecars:InSendQueue='true'";
    final Query q = pQM.createQuery( qu, Query.SQL );
    final QueryResult qr = q.execute();
    return qr.getNodes();
  }


  /** isScheduledTool
   *
   * @return
   */
  @Override
  public boolean isScheduledTool() {
    return true;
  }

  /** getDelayInSecs
   *
   * @return
   */
  @Override
  public long getDelayInSecs() {
    return CHECKEVERY/1000;
  }

  /** getRunningExpireMinutes
   *
   * @return
   */
  @Override
  public int getRunningExpireMinutes() {
    return 60*24*365*25;
  }



}
