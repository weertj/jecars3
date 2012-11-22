/*
 * Copyright 2011-2012 NLR - National Aerospace Laboratory
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
package org.jecars.servlets;

import java.util.*;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jecars.CARS_Factory;

/**
 *
 * @author weert
 */
public class CARS_DefaultLongPolling implements ICARS_LongPolling, EventListener {

  /** PollData
   * 
   */
  static public class PollData {
    private final String        mKey;
    private HttpServletRequest  mRequest;
    private HttpServletResponse mResponse;
    private Object              mUserData;
    private final List<Event>   mEvents = new ArrayList<Event>();

    public PollData( final String pKey ) {
      mKey = pKey;
      return;
    }
    
    public String getKey() {
      return mKey;
    }
    
    public Object getUserData() {
      return mUserData;
    }

    public HttpServletResponse getResponse() {
      return mResponse;
    }

    public HttpServletRequest getRequest() {
      return mRequest;
    }
    
    public void addEvent( final Event pEvent ) {
      mEvents.add( pEvent );
      return;
    }
    
    public List<Event> getEvents() {
      return mEvents;
    }
    
  }
    
  static private final Map<String, PollData> POLLERS = new HashMap<String, PollData>();

  static private boolean mIsEventAdded = false;
  
  static public Map<String, PollData>getPollers() {
    return POLLERS; 
  }
  
  /** endHandleLongPolling
   * 
   * @param pNodePath
   * @param pRequest
   * @param pResponse
   * @param pUserData
   * @throws RepositoryException 
   */
  @Override
  public void endHandleLongPolling( final String pNodePath, final HttpServletRequest pRequest, final HttpServletResponse pResponse, final Object pUserData ) throws RepositoryException {
    if (!mIsEventAdded) {
      CARS_Factory.getSystemApplicationSession().getWorkspace().getObservationManager().addEventListener(
                        this,
                        Event.NODE_ADDED|Event.NODE_MOVED|Event.NODE_REMOVED|Event.PROPERTY_ADDED|Event.PROPERTY_CHANGED|Event.PROPERTY_REMOVED,
                        "/", true, null, null, false );
      mIsEventAdded = true;
    }
    final PollData pd = new PollData( pNodePath );
    pd.mRequest  = pRequest;
    pd.mResponse = pResponse;
    pd.mUserData = pUserData;
    POLLERS.put( pNodePath, pd );
    return;
  }

  /** onEvent
   * 
   * @param ei 
   */
  @Override
  public void onEvent( final EventIterator pEI ) {
    try {
      while( pEI.hasNext() ) {
        final Event ev = pEI.nextEvent();
        for( final String n : POLLERS.keySet() ) {
          if (ev.getPath().startsWith(n)) {            
            PollData polldata = POLLERS.get( n );
            polldata.addEvent( ev );
          }
        }
      }
      
      // **** Wake up the polls with event data
      final Collection<PollData> pdcol = new ArrayList<PollData>(POLLERS.values());
      for( PollData pd : pdcol ) {
        if (!pd.getEvents().isEmpty()) {
          POLLERS.remove( pd.getKey() );
          wake( pd );
        }
      }

    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }

  protected void wake( PollData pPD ) {
    return;
  }
  
  
}
