/*
 * Copyright 2007-2009 NLR - National Aerospace Laboratory
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

import java.util.logging.*;
import javax.jcr.Node;
import javax.jcr.Session;

/**
 * CARS_LogHandler
 *
 * @version $Id: CARS_LogHandler.java,v 1.3 2009/03/19 15:55:10 weertj Exp $
 */
public class CARS_LogHandler extends Handler {
  
  private Node mSystemEventsFolder = null;

  /** createLogRecord
   *
   * @param pUser
   * @param pSourceNode
   * @param pLevel
   * @param pCategory
   * @param pType
   * @param pMessage
   * @param pThrow
   * @return
   */
  static public LogRecord createLogRecord( Node pUser, Node pSourceNode, Level pLevel, String pCategory, String pType, String pMessage, Throwable pThrow ) {
    if (pMessage==null) {
      if (pThrow!=null) {
        pMessage = pThrow.getMessage();
      }
    }
    LogRecord lr = new LogRecord( pLevel, pMessage );
    lr.setMillis( System.currentTimeMillis() );
    lr.setThrown( pThrow );
    if (pUser!=null) {
      try {
        lr.setLoggerName( pUser.getPath() );
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
    if (pSourceNode!=null) {
      try {
        lr.setSourceClassName( pSourceNode.getPath() );
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
    String[] params = {pCategory,pType};
    lr.setParameters( params );
    return lr;
  }
  
  /** Creates a new instance of CARS_LogHandler
   */
  public CARS_LogHandler() {
    if (mSystemEventsFolder==null) {
      try {
//        mSystemEventsFolder = CARS_Factory.gSystemCarsSession.getRootNode().getNode( "JeCARS/default/Events/System" );
        final Session appSession = CARS_Factory.getSystemApplicationSession();
        synchronized( appSession ) {
          mSystemEventsFolder = appSession.getRootNode().getNode( "JeCARS/default/Events/System" );
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /** publish
   *
   * @param pRecord
   */
  @Override
  public void publish( final LogRecord pRecord ) {
    if (mSystemEventsFolder!=null) {
      if (pRecord.getSourceClassName().startsWith( "org.jecars")) {
          try {
            Session ses = mSystemEventsFolder.getSession();
            Node user = null;        
            if (pRecord.getLoggerName()!=null) {
              try {
                user = ses.getRootNode().getNode( pRecord.getLoggerName().substring(1) );
              } catch( Exception e ) {
              }
            }
            Node source = null;
            if (pRecord.getSourceClassName()!=null) {
              try {
                source = ses.getRootNode().getNode( pRecord.getSourceClassName().substring(1) );
              } catch (Exception e) {           
              }
            }
            if (pRecord.getParameters()!=null) {
              String[] params = (String[])pRecord.getParameters();
//              CARS_Factory.gEventManager.addException( null, user, source, null, params[0], params[1], pRecord.getThrown(), pRecord.getMessage() );
              CARS_Factory.getEventService().offer( new CARS_Event( user, source, null, params[0], params[1], pRecord ));
            } else {
//              CARS_Factory.gEventManager.addException( null, user, source, null, "SYS", pRecord.getLevel().getName(), pRecord.getThrown(), pRecord.getMessage() );
              CARS_Factory.getEventService().offer( new CARS_Event( user, source, null, "SYS", pRecord.getLevel().getName(), pRecord ));
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
      }
    }
    return;
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
  }
  
}
