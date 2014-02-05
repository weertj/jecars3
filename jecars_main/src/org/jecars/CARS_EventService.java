/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import static org.jecars.CARS_EventManager.SEP_DAY;
import static org.jecars.CARS_EventManager.SEP_HOUR;
import static org.jecars.CARS_EventManager.SEP_MINUTE;
import static org.jecars.CARS_EventManager.SEP_MONTH;
import static org.jecars.CARS_EventManager.SEP_YEAR;
import org.jecars.tools.CARS_ToolInstanceEvent;

/**
 *
 * @author weert
 */
public class CARS_EventService implements ICARS_EventService, Runnable {

  private final transient Thread                      mServiceThread;
  private final transient Session                     mSession;
  private       transient long                        mNumberOfEventsWritten = 0;
  private       transient long                        mTopEventsInQueue = 0;
  private final transient BlockingQueue<ICARS_Event>  mEventsQueue = new LinkedBlockingQueue<>(64);

  /**
   * 
   * @param pSession 
   */
  public CARS_EventService(final Session pSession) {
    mSession = pSession;
    mServiceThread = new Thread(this);
    mServiceThread.setPriority(Thread.MIN_PRIORITY);
    mServiceThread.setName("CARS_EventService");
    mServiceThread.start();
    return;
  }

  /**
   * numberOfEventsWritten
   *
   * @return
   */
  @Override
  public long numberOfEventsWritten() {
    return mNumberOfEventsWritten;
  }

  @Override
  public long eventsInQueue() {
    return mEventsQueue.size();
  }

  @Override
  public long topEventsInQueue() {
    return mTopEventsInQueue;
  }

  /** offer
   * 
   * @param pEvent
   * @return 
   */
  @Override
  public boolean offer(final ICARS_Event pEvent) {
    boolean stored = false;
    while( !stored ) {
      try {
        stored = mEventsQueue.offer(pEvent, 2000, TimeUnit.MILLISECONDS);
        if (pEvent.toolInstanceEvent()!=null) {
          while( pEvent.toolInstanceEvent().getEventNode( mSession )==null ) {
            synchronized( pEvent ) {
              pEvent.wait( 250 );
            }
          }
        }
      } catch( RepositoryException | InterruptedException e ) {
        e.printStackTrace();
      }
    }
    return stored;
  }

  @Override
  public void run() {
    while (1 == 1) {
      try {
        if (mEventsQueue.size()>mTopEventsInQueue) {
          mTopEventsInQueue = mEventsQueue.size();
        }
        final ICARS_Event event = mEventsQueue.poll(5000, TimeUnit.MILLISECONDS);
        if (event != null) {
          try {
//            System.out.println("GOT event " + event);
            final Node node;
            if (event.folder()==null) {
              node = createEventNode( event, mSession.getNode("/JeCARS/default/Events") );
            } else {
              node = createEventNode( event, mSession.getNode( event.folder() ) );              
            }
            if (event.toolInstanceEvent()!=null) {
              _setEventProperties( event.toolInstanceEvent(), node );
            }
            synchronized( event ) {
              event.notifyAll();
            }
            mNumberOfEventsWritten++;
          } finally {
            mSession.save();
          }
        }
      } catch (Throwable e) {
        // **** Never give up, never surrender!
        e.printStackTrace();
      }
    }
  }

  /** createEventNode
   * 
   * @param pEvent
   * @return
   * @throws RepositoryException 
   */
  public Node createEventNode( final ICARS_Event pEvent, final Node pFolder ) throws RepositoryException {
    Node event;
    Node folder = pFolder;
//    Node folder = mSession.getRootNode().getNode("JeCARS/default/Events");

    if (pEvent.application() == null) {
      folder = folder.getNode("System/jecars:Events" + pEvent.type());
    } else {
      if (pEvent.application().startsWith("/")) {
        folder = mSession.getNode(pEvent.application());
      } else {
        folder = folder.getNode(pEvent.application());
      }
    }
    final Calendar now = pEvent.creationDate();
    final Calendar cal;
    final long expireValue = folder.getProperty(CARS_Definitions.DEFAULTNS + "ExpireHour" + pEvent.type()).getLong();
    if (expireValue < 0) {
      // **** No expire date
      cal = null;
    } else {
      cal = Calendar.getInstance();
      cal.setTimeInMillis(now.getTimeInMillis());
      cal.add(Calendar.HOUR, (int) expireValue);
    }
    final Node storeEvents = getEventStoreFolder(now, cal, folder);
    final long count = folder.getProperty(CARS_Definitions.DEFAULTNS + "EventsCount").getLong();
    if (pEvent.eventType() == null) {
      event = CARS_DefaultMain.addNode(storeEvents, pEvent.category() + "_" + pEvent.type() + "_" + count, CARS_Definitions.DEFAULTNS + "Event");
    } else {
      event = CARS_DefaultMain.addNode(storeEvents, pEvent.category() + "_" + pEvent.type() + "_" + count, pEvent.eventType());    // **** Tracker 2542920
    }
    folder.setProperty(CARS_Definitions.DEFAULTNS + "EventsCount", count + 1);
    event.setProperty(CARS_Definitions.DEFAULTNS + "Category", pEvent.category());
//        if (pSource!=null)     event.setProperty( CARS_Definitions.DEFAULTNS + "Source", pSource.getPath() );
//        if (pSourcePath!=null) event.setProperty( CARS_Definitions.DEFAULTNS + "Source", pSourcePath );
    if (pEvent.user() != null) {
      event.setProperty(CARS_Definitions.DEFAULTNS + "User", pEvent.user());
    }
    event.setProperty(CARS_Definitions.DEFAULTNS + "Type", pEvent.type());
    event.setProperty(CARS_Definitions.DEFAULTNS + "ExpireDate", cal);
    if (pEvent.message() != null) {
      event.setProperty(CARS_Definitions.DEFAULTNS + "Title", pEvent.message());
      event.setProperty(CARS_Definitions.DEFAULTNS + "Body", pEvent.message());
    }
    if (pEvent.body()!=null) {
      event.setProperty(CARS_Definitions.DEFAULTNS + "Body", pEvent.body());      
    }
    event.setProperty( CARS_Definitions.DEFAULTNS + "Code", pEvent.code() );
    return event;
  }

  /**
   * Retrieve and/or create the event store folder
   *
   * @param pWhen
   * @param pExpire
   * @param pFolder
   * @return
   * @throws javax.jcr.RepositoryException
   */
  private Node getEventStoreFolder(final Calendar pWhen, final Calendar pExpire, final Node pFolder) throws RepositoryException {
    Node storeEvents = pFolder;
    if (storeEvents.hasProperty("jecars:StoreEventsPer")) {
      final Calendar cal = Calendar.getInstance();
      final String storePer = storeEvents.getProperty("jecars:StoreEventsPer").getString();
      if (SEP_YEAR.equals(storePer) || SEP_MONTH.equals(storePer) || SEP_DAY.equals(storePer) || SEP_HOUR.equals(storePer) || SEP_MINUTE.equals(storePer)) {
        final String path = String.valueOf(pWhen.get(Calendar.YEAR));
        if (!storeEvents.hasNode(path)) {
          storeEvents.addNode(path, "jecars:EventsStoreFolder");
          storeEvents.setProperty(CARS_ActionContext.DEF_MODIFIED, cal);
        }
        storeEvents = storeEvents.getNode(path);
        if (pExpire != null) {
          if (!storeEvents.hasProperty(CARS_ActionContext.gDefExpireDate)) {
            storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
          } else {
            Calendar scal = storeEvents.getProperty(CARS_ActionContext.gDefExpireDate).getDate();
            if (scal.before(pExpire)) {
              storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
            }
          }
        }
      }
      if (SEP_MONTH.equals(storePer) || SEP_DAY.equals(storePer) || SEP_HOUR.equals(storePer) || SEP_MINUTE.equals(storePer)) {
        final String path = String.valueOf(pWhen.get(Calendar.MONTH) + 1);
        if (storeEvents.hasNode(path) == false) {
          storeEvents.addNode(path, "jecars:EventsStoreFolder");
          storeEvents.setProperty(CARS_ActionContext.DEF_MODIFIED, cal);
        }
        storeEvents = storeEvents.getNode(path);
        if (pExpire != null) {
          if (storeEvents.hasProperty(CARS_ActionContext.gDefExpireDate) == false) {
            storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
          } else {
            Calendar scal = storeEvents.getProperty(CARS_ActionContext.gDefExpireDate).getDate();
            if (scal.before(pExpire)) {
              storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
            }
          }
        }
      }
      if (SEP_DAY.equals(storePer) || SEP_HOUR.equals(storePer) || SEP_MINUTE.equals(storePer)) {
        final String path = String.valueOf(pWhen.get(Calendar.DAY_OF_MONTH));
        if (!storeEvents.hasNode(path)) {
          storeEvents.addNode(path, "jecars:EventsStoreFolder");
          storeEvents.setProperty(CARS_ActionContext.DEF_MODIFIED, cal);
        }
        storeEvents = storeEvents.getNode(path);
        if (pExpire != null) {
          if (storeEvents.hasProperty(CARS_ActionContext.gDefExpireDate) == false) {
            storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
          } else {
            final Calendar scal = storeEvents.getProperty(CARS_ActionContext.gDefExpireDate).getDate();
            if (scal.before(pExpire)) {
              storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
            }
          }
        }
      }
      if (SEP_HOUR.equals(storePer) || SEP_MINUTE.equals(storePer)) {
        final String path = String.valueOf(pWhen.get(Calendar.HOUR_OF_DAY));
        if (!storeEvents.hasNode(path)) {
          storeEvents.addNode(path, "jecars:EventsStoreFolder");
          storeEvents.setProperty(CARS_ActionContext.DEF_MODIFIED, cal);
        }
        storeEvents = storeEvents.getNode(path);
        if (pExpire != null) {
          if (storeEvents.hasProperty(CARS_ActionContext.gDefExpireDate) == false) {
            storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
          } else {
            final Calendar scal = storeEvents.getProperty(CARS_ActionContext.gDefExpireDate).getDate();
            if (scal.before(pExpire)) {
              storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
            }
          }
        }
      }
      if (SEP_MINUTE.equals(storePer)) {
        final String path = String.valueOf(pWhen.get(Calendar.MINUTE));
        if (!storeEvents.hasNode(path)) {
          storeEvents.addNode(path, "jecars:EventsStoreFolder");
          storeEvents.setProperty(CARS_ActionContext.DEF_MODIFIED, cal);
        }
        storeEvents = storeEvents.getNode(path);
        if (pExpire != null) {
          if (storeEvents.hasProperty(CARS_ActionContext.gDefExpireDate) == false) {
            storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
          } else {
            final Calendar scal = storeEvents.getProperty(CARS_ActionContext.gDefExpireDate).getDate();
            if (scal.before(pExpire)) {
              storeEvents.setProperty(CARS_ActionContext.gDefExpireDate, pExpire);
            }
          }
        }
      }
    }
    return storeEvents;
  }

  /**
   * 
   * @param pEvent
   * @param pNode
   * @throws RepositoryException
   * @throws IOException 
   */
  private void _setEventProperties( final CARS_ToolInstanceEvent pEvent, final Node pNode ) throws RepositoryException, IOException {
//      System.out.println("SEt SESSION - " + pNode.getSession() );
    pNode.setProperty( "jecars:EventType", pEvent.getEventType() );
    pNode.setProperty( "jecars:State",     pEvent.getEventState() );
    pNode.setProperty( "jecars:Level",     pEvent.getEventLevel().intValue() );
    pNode.setProperty( "jecars:Value",     pEvent.getEventStringValue() );
    pNode.setProperty( "jecars:DValue",    pEvent.getEventValue() );
    pNode.setProperty( "jecars:Modified", Calendar.getInstance() );
    if (pEvent.getEventBlocking()) {
      pNode.setProperty( "jecars:Blocking", true );
    }
    if (pNode.isNodeType( "jecars:ToolEventException" )) {
      if (pEvent.getEventException()!=null) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pEvent.getEventException().printStackTrace(pw);
        pNode.setProperty( "jecars:Body", sw.getBuffer().toString() );
        final ByteArrayOutputStream   dos = new ByteArrayOutputStream();
        final ObjectOutputStream      oos = new ObjectOutputStream( dos );
        final Throwable                 t = pEvent.getEventException();
        if (t instanceof java.io.Serializable) {
            try {
                oos.writeObject( t );
            } catch (java.io.NotSerializableException e) {
                // strange - still happens mentioning com.sun.script.javascript.ExternalScriptable (so in the special case we are using a script tool)
                oos.writeObject( "Event was not serializable - writing message and stackTrace");
                oos.writeObject( t.toString());
                oos.writeObject( t.getStackTrace());
            }
            // no cause - cause could be again non Serializable ...
        }        oos.close();
        dos.close();
        final ByteArrayInputStream bais = new ByteArrayInputStream( dos.toByteArray() );
        pNode.setProperty( "jecars:Exception", bais );
        bais.close();
      }
    }
    pEvent.setEventNode( pNode );
//    pNode.save();
    return;
  }

  
  
}
