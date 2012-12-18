/*
 * Copyright 2012-2013 NLR - National Aerospace Laboratory
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

package org.jecars.sandy;

import java.io.IOException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.jecars.servlets.CARS_DefaultLongPolling;

/**
 *
 * @author weert
 */
public class SDY_LongPolling extends CARS_DefaultLongPolling {

  /** endHandleLongPolling
   * 
   * @param pRequest
   * @param pResponse 
   */
  @Override
  public void endHandleLongPolling( final String pNodePath, final HttpServletRequest pRequest, final HttpServletResponse pResponse, final Object pUserData ) throws RepositoryException {
    
    final Continuation cont = ContinuationSupport.getContinuation( pRequest );
    if (cont.isExpired()) {
        System.out.println("timeout??");
    } else {
      cont.suspend( pResponse );
      super.endHandleLongPolling( pNodePath, pRequest, pResponse, cont );
    }
    return;
  }

  /** wake
   * 
   * @param pPD 
   */
  @Override
  protected void wake( final PollData pPD ) {
    Continuation ac = (Continuation)pPD.getUserData();
    try {
      pPD.getResponse().setContentType( "text/plain" );
      final StringBuilder mes = new StringBuilder();
      int ix = 0;
      for( final Event ev : pPD.getEvents() ) {
        mes.append( "Event.Path." ).append( ix ).append( '=' ).append( ev.getPath() ).append( '\n' );
        mes.append( "Event.Date." ).append( ix ).append( '=' ).append( ev.getDate() ).append( '\n' );
        mes.append( "Event.Identifier." ).append( ix ).append( '=' ).append( ev.getIdentifier() ).append( '\n' );
        mes.append( "Event.Type." ).append( ix ).append( '=' ).append( ev.getType() ).append( '\n' );
        ix++;
      }
      pPD.getResponse().getOutputStream().println( mes.toString() );
    } catch( RepositoryException e ) {
      e.printStackTrace();
    } catch( IOException e ) {
      e.printStackTrace();
    }
    ac.complete();
    return;
  }
  
}
