/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.client.observation;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_HttpException;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Path;
import org.jecars.client.JC_RootNode;
import org.jecars.client.observation.JC_Event.TYPE;

/**
 *
 * @author weert
 */
public class JC_DefaultObservationManager implements JC_ObservationManager {

  static final protected Logger gLog = Logger.getLogger( "org.jecars.client.observation" );

  static final public int MINIMUM_TIMESLOT_IN_SECS = 5;

  static private volatile long EVENTID = 1L;

  private final JC_Clientable            mClient;
  private final List<eventListener>      mListeners;
  private final manager                  mManage;
  private final ScheduledExecutorService mSES;
  private       JC_RootNode              mRootNode = null;
  private       ScheduledFuture          mScheduledRunnable = null;

  /** manager
   *
   */
  private class manager implements Runnable {

    /** run
     *
     */
    @Override
    public void run() {
      try {
        synchronized( mManage ) {

          if (mRootNode==null) {
            mRootNode = (JC_RootNode)mClient.getRootNode();
          }

          final List<eventListener> tbr = new ArrayList<eventListener>();
          for (eventListener list : mListeners) {
            List<JC_Event>events = new ArrayList<JC_Event>();
            try {
              final JC_Nodeable node = mRootNode.getResolvedNode( list.getPathRelToRoot(), null );
              final EnumSet<TYPE> eventTypes = list.getEventTypes();
              if (node.getUpdateAsHead()) {
                // **** The node is updated
                if (eventTypes.contains( JC_Event.TYPE.NODE_ADDED )) {
                  events.add( new JC_DefaultEvent( Calendar.getInstance(), String.valueOf(EVENTID++), node.getPath_JC(), JC_Event.TYPE.NODE_ADDED ) );
                }
              }

              // **** Send the events, is not empty
              if (!events.isEmpty()) {
                events = Collections.unmodifiableList( events );
                list.getListener().onEvent( events );
              }

            } catch( JC_HttpException e ) {
              if (e.getHttpErrorCode().getErrorCode()==HttpURLConnection.HTTP_NOT_FOUND) {
                if (list.removeWhenInvalid()) {
                  // **** Remove the event listener, the object is gone
                  tbr.add( list );
                }
              }
            }
          }
          // **** Update the eventlisteners
          for ( eventListener listener : tbr) {
            removeEventListener( listener );
          }
        }
      } catch(JC_Exception je ) {
       gLog.log( Level.WARNING, je.getServerMessage(), je );
      }
    }

  }

  private class eventListener {
    
    private final JC_EventListener mListener;
    private final EnumSet<TYPE>    mEventTypes;
    private final String           mPath;
    private final boolean          mIsDeep;
    private final int              mCheckEverySlot;
    private       boolean          mRemoveWhenInvalid = true;

    /** eventListener
     * 
     * @param pListener
     * @param pEventTypes
     * @param pAbsPath
     * @param pIsDeep
     * @param pCheckEverySlot
     */
    private eventListener( final JC_EventListener pListener, final EnumSet<TYPE> pEventTypes, final JC_Path pAbsPath, final boolean pIsDeep, final int pCheckEverySlot ) {
      if (pIsDeep) {
        throw new UnsupportedOperationException( "IsDeep = true is not supported" );
      }
      mListener         = pListener;
      mEventTypes       = pEventTypes;
      mPath             = pAbsPath.getPathRelToRoot();
      mIsDeep           = pIsDeep;
      mCheckEverySlot   = pCheckEverySlot;
      return;      
    }

    /** setRemoveWhenInvalid
     * 
     * @param pRemove if true then the listener will automatically being remove when
     *                a retrieve error occurs, e.g. the path is removed.
     */
    public void setRemoveWhenInvalid( final boolean pRemove ) {
      mRemoveWhenInvalid = pRemove;
      return;
    }

    /** removeWhenInvalid
     *
     * @return
     */
    public boolean removeWhenInvalid() {
      return mRemoveWhenInvalid;
    }

    /** getListener
     *
     * @return
     */
    private JC_EventListener getListener() {
      return mListener;
    }

    /** getEventTypes
     *
     * @return
     */
    private EnumSet<TYPE> getEventTypes() {
      return mEventTypes;
    }

    /** getPathRelToRoot
     *
     * @return
     */
    private String getPathRelToRoot() {
      return mPath;
    }

    /** isDeep
     *
     * @return
     */
    private boolean isDeep() {
      return mIsDeep;
    }

    /** checkEverySecs
     *
     * @return
     */
    private int checkEverySlot() {
      return mCheckEverySlot;
    }

  }


  /** JC_DefaultObservationManager
   *
   * @param pClient
   */
  public JC_DefaultObservationManager( final JC_Clientable pClient ) throws JC_Exception {
    mClient    = pClient;
    mListeners = new ArrayList<eventListener>();
    mManage    = new manager();
    mSES       = Executors.newScheduledThreadPool( 4 );
    return;
  }



  /** addEventListener
   * 
   * @param pListener
   * @param pEventTypes
   * @param pAbsPath
   * @param pIsDeep
   * @param pCheckEverySecs
   */
  @Override
  public void addEventListener( final JC_EventListener pListener, final EnumSet<TYPE> pEventTypes, final JC_Path pAbsPath,
                                final boolean pIsDeep, final int pCheckEverySecs, final boolean pRemoveWhenInvalid ) {
    synchronized( mManage ) {
      eventListener el = new eventListener( pListener, pEventTypes, pAbsPath, pIsDeep, pCheckEverySecs );
      el.setRemoveWhenInvalid( pRemoveWhenInvalid );
      mListeners.add( el );
      if (mScheduledRunnable==null) {
        mScheduledRunnable = mSES.scheduleAtFixedRate( mManage, 1, MINIMUM_TIMESLOT_IN_SECS, TimeUnit.SECONDS );
      }
    }
    return;
  }

  /** removeEventListener
   *
   * @param pListener
   */
  @Override
  public void removeEventListener( JC_EventListener pListener ) {
    synchronized( mManage ) {
      List<eventListener> tbr = new ArrayList<eventListener>();
      for( eventListener listener : mListeners ) {
        if (listener.getListener().equals( pListener )) {
          tbr.add( listener );
        }
      }
      for( eventListener listener : tbr ) {
        removeEventListener( listener );
      }
    }
    return;
  }

  /** removeEventListener
   * 
   * @param pListener
   */
  private final void removeEventListener( final eventListener pListener ) {
    mListeners.remove( pListener );
    if (mListeners.isEmpty()) {
      if (mScheduledRunnable!=null) {
        mScheduledRunnable.cancel( true );
        mScheduledRunnable = null;
      }
    }
    return;
  }

}
