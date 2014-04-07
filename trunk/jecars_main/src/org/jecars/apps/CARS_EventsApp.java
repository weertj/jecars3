/*
 * Copyright 2010-2014 NLR - National Aerospace Laboratory
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

import java.io.UnsupportedEncodingException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Event;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.ICARS_Event;
import org.jecars.ICARS_EventService;

/**
 * CARS_EventsApp

 */
public class CARS_EventsApp extends CARS_DefaultInterface {
    
  /** Creates a new instance of CARS_EventsApp
   */
  public CARS_EventsApp() {
    super();
  }
 
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": " + CARS_Definitions.PRODUCTNAME + " version=" + CARS_Definitions.VERSION_ID + " CARS_EventsApp";
  }

  /** getNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws RepositoryException 
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws RepositoryException {
    if (pParentNode.isNodeType( "jecars:CARS_Interface" )) {
      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {
        try {
          final Node parentNode = appSession.getNode( pParentNode.getPath() );
          if (!parentNode.isNodeType( "jecars:mixin_unstructured" )) {
            parentNode.addMixin( "jecars:mixin_unstructured" );
          }
          final ICARS_EventService es = CARS_Factory.getEventService();
          parentNode.setProperty( "jecars:numberOfEventsWritten", es.numberOfEventsWritten() );
          parentNode.setProperty( "jecars:eventsInQueue", es.eventsInQueue() );
          parentNode.setProperty( "jecars:topEventsInQueue", es.topEventsInQueue() );
        } finally {
          appSession.save();
        }
      }
    }
    return;
  }

  
  /** Add a node to the repository
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pParentNode the node under which the object must be added
   * @param pName the node name
   * @param pPrimType the node type
   * @param pParams list of parameters
   * @return 
   * @throws javax.jcr.RepositoryException
   * @throws java.io.UnsupportedEncodingException
   */
  @Override
  public Node addNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws RepositoryException, UnsupportedEncodingException {
    Node realEventNode = null;
    if ("jecars:Event".equals( pPrimType )) {
      JD_Taglist paramsTL = pMain.getContext().getQueryPartsAsTaglist();
      paramsTL = pMain.getContext().getParameterMapAsTaglist( paramsTL );
      final String eventPath = (String)paramsTL.getData( "jecars:X-EventPath" );
      if (eventPath==null) {
        throw new ConstraintViolationException( "X-EventPath not given" );
      } else {
//        final CARS_ActionContext ac = pMain.getContext();
        
        final ICARS_EventService es = CARS_Factory.getEventService();
        
//        final CARS_EventManager eventManager = CARS_Factory.getEventManager();
        ICARS_Event event = null;
        final Session appSession = CARS_Factory.getSystemApplicationSession();
        synchronized( appSession ) {
          try {
            Node eventPathFolder = null;
            try {
              eventPathFolder = appSession.getNode( eventPath );
            } catch( PathNotFoundException pe ) {
              // **** Check we must create the event folder
              final int ix = eventPath.lastIndexOf( '/' );
              if (ix>-1) {
                final String ep = eventPath.substring( 0, ix );
                String type = eventPath.substring( ix+1 );
                final Node parentFolder = appSession.getNode( ep );
                if (type.startsWith( "jecars:Events" ) && parentFolder.isNodeType( "jecars:EventsFolder" )) {
                  eventPathFolder = parentFolder.addNode( type, "jecars:EventsFolder" );
                  eventPathFolder.setProperty( "jecars:StoreEventsPer",
                          parentFolder.getProperty( "jecars:StoreEventsPer" ).getString() );
                  eventPathFolder.setProperty( "jecars:ExpireHour" + type.substring( "jecars:Events".length() ),
                          parentFolder.getProperty( "jecars:ExpireHour" + type.substring( "jecars:Events".length() ) ).getLong() );
                }
              }
            }
            appSession.save();
//            final Node loginUserAsSystem = appSession.getNode( pMain.getLoginUser().getPath() );
//            final Node parentNode        = appSession.getNode( pParentNode.getPath() );
            // **** An event object will be created

            event = new CARS_Event( 
                    pMain,
                    eventPathFolder.getPath(),
                    null,
                    (String)paramsTL.getData( "jecars:Category" ),
                    (String)paramsTL.getData( "jecars:Type" ),
                    null,
                    (String)paramsTL.getData( "jecars:Title" ),
                    pPrimType ).
                    body( (String)paramsTL.getData( "jecars:Body" ) ).
                    waitForEventNode( true );
          } finally {
            appSession.save();
          }
        }
        // **** Offer event to the service
        if (event!=null) {
          es.offer( event );
          if (event.eventNode()!=null) {
            synchronized( appSession ) {
              try {
                // **** Event node path should be available
                realEventNode = appSession.getNode( event.eventNode() );
              } finally {
                appSession.save();
              }
            }
          }
        }
            
//            realEventNode = eventManager.addEvent( eventPathFolder, ac.getMain(),
//                      loginUserAsSystem, null,
//                      eventPath,
//                      (String)paramsTL.getData( "jecars:Category" ),
//                      (String)paramsTL.getData( "jecars:Type" ),
//                      (String)paramsTL.getData( "jecars:Title" ),
//                      pPrimType,
//                      (String)paramsTL.getData( "jecars:Body" ),
//                      parentNode.getPath() );
//        }
        pParentNode.save();
      }
    } else {
      throw new ConstraintViolationException( "Nodetype " + pPrimType + " isn't allowed\n" +
              "Parameters;\n" +
              "(Mandatory) jcr:primaryType=jecars:Event\n" +
              "(Mandatory) X-EventPath={the event path where the event will be created}\n"
              );
    }
    return realEventNode;
  }


  
    
}
