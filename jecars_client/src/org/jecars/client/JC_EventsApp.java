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

package org.jecars.client;

import org.jecars.client.nt.JC_EventNode;

/** JC_EventApp
 *
 * @author weert
 */
public class JC_EventsApp {

  private final JC_Clientable mClient;

  /** JC_EventApp
   *
   * @param pClient
   */
  public JC_EventsApp( final JC_Clientable pClient ) {
    mClient = pClient;
    return;
  }



  /** createEvent
   *
   * @return
   * @throws JC_Exception
   */
  public JC_EventNode createEvent(
                final String pEventTargetFolder,
                final String pMessage, final String pBody,
                final String pEventCollectionID,
                final String pSource, final String pType, final String pCategory ) throws JC_Exception {
    final JC_Nodeable eventApp = mClient.getRootNode().getNode( "JeCARS/ApplicationSources/EventsApp" );
    final JC_EventNode event = (JC_EventNode)eventApp.addNode( "testEvent", "jecars:Event" );
    event.setProperty( "jecars:X-EventPath", pEventTargetFolder );
    if (pMessage!=null) {
      event.setProperty( JC_DefaultNode.PROP_TITLE, pMessage );
    }
    if (pBody!=null) {
      event.setProperty( JC_DefaultNode.PROP_BODY,  pBody );
    }
    if (pSource!=null) {
      event.setProperty( JC_EventNode.PROP_SOURCE,  pSource );
    }
    if (pType!=null) {
      event.setProperty( JC_EventNode.PROP_TYPE,  pType );
    }
    if (pCategory!=null) {
      event.setProperty( JC_EventNode.PROP_CATEGORY,  pCategory );
    }
    event.save();
    return event;
  }

}
