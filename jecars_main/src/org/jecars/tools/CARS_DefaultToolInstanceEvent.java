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
package org.jecars.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * @version $Id: CARS_DefaultToolInstanceEvent.java,v 1.6 2009/02/25 13:36:17 weertj Exp $
 */
public class CARS_DefaultToolInstanceEvent implements CARS_ToolInstanceEvent {
  
  private CARS_ToolInterface mInstance    = null;
  private int                mEventType   = EVENTTYPE_UNKNOWN;
  private String             mState       = CARS_ToolInterface.STATE_UNKNOWN;
  private Throwable          mException   = null;
  private String             mStringValue = null;
  private double             mValue       = -1;
  private Level              mLevel       = Level.ALL;
  private boolean            mBlocking    = false;
//  private Node               mEventNode   = null;
  private String             mEventNodePath   = null;
  
  /** Creates a new instance of LPF_DefaultToolInstanceEvent
   *
   * @param pInstance
   * @param pEventType
   */
  private CARS_DefaultToolInstanceEvent( CARS_ToolInterface pInstance, int pEventType ) {
    mInstance  = pInstance;
    mEventType = pEventType;
    return;
  }
  
  /** Factory method to create a event
   *
   * @param pInstance
   * @param pLevel
   * @param pMessage
   * @param pBlocking
   * @return
   */
  static public CARS_ToolInstanceEvent createEventMessage( CARS_ToolInterface pInstance, Level pLevel, String pMessage, boolean pBlocking ) {
    CARS_DefaultToolInstanceEvent dte = new CARS_DefaultToolInstanceEvent( pInstance, EVENTTYPE_TOOLMESSAGE );
    dte.mStringValue = pMessage;
    dte.mLevel       = pLevel;
    dte.mBlocking    = pBlocking;
    return dte;
  }

  /** Factory method to create a event
   *
   * @param pInstance
   * @param pEventType
   * @param pValue
   * @return
   */
  static public CARS_ToolInstanceEvent createEvent( CARS_ToolInterface pInstance, int pEventType, String pValue ) {
    CARS_DefaultToolInstanceEvent dte = new CARS_DefaultToolInstanceEvent( pInstance, pEventType );
    dte.mStringValue = pValue;
    return dte;
  }

  /** createEvent
   * 
   * @param pInstance
   * @param pEventType
   * @param pValue
   * @return
   */
  static public CARS_ToolInstanceEvent createEvent( CARS_ToolInterface pInstance, int pEventType, double pValue ) {
    CARS_DefaultToolInstanceEvent dte = new CARS_DefaultToolInstanceEvent( pInstance, pEventType );
    dte.mValue = pValue;
    return dte;
  }

  /** Factory method to create a event
   *
   * @param pInstance
   * @param pEventType
   * @return
   */
  static public CARS_ToolInstanceEvent createEvent( CARS_ToolInterface pInstance, int pEventType ) {
    return new CARS_DefaultToolInstanceEvent( pInstance, pEventType );
  }

  /** Factory method to create a event for a exception type
   *
   * @param pInstance
   * @param pException
   * @param pLevel
   * @return
   */
  static public CARS_ToolInstanceEvent createEvent( CARS_ToolInterface pInstance, Throwable pException, Level pLevel ) {
    final CARS_DefaultToolInstanceEvent e;
    if (pException instanceof CARS_ToolInstanceException) {
      e = new CARS_DefaultToolInstanceEvent( pInstance, EVENTTYPE_GENERALEXCEPTION );
      e.mException = pException;
      
    } else {
      e = new CARS_DefaultToolInstanceEvent( pInstance, EVENTTYPE_GENERALEXCEPTION );
      e.mException = pException;
    }
    e.mLevel = pLevel;
    return e;
  }

  /** Factory method to create a event for a change state type
   *
   * @param pInstance
   * @param pState
   * @return
   */
  static public CARS_ToolInstanceEvent createEventState( CARS_ToolInterface pInstance, String pState ) {
    final CARS_DefaultToolInstanceEvent e = new CARS_DefaultToolInstanceEvent( pInstance, EVENTTYPE_STATECHANGED );
    e.mState = pState;
    return e;
  }

  /** Get the tool instance which generated this event
   * @return The tool instance
   */
  @Override
  public CARS_ToolInterface getToolInstance() {
    return mInstance;
  }
   
  /** Get the state of the instance at the time this event was created
   * @return the STATE_*
   */
  @Override
  public String getEventState() {
    return mState;
  }

  /** Get the exception of the tool instance, when event type is EVENTTYPE_GENERALEXCEPTION
   */
  @Override
  public Throwable getEventException() {
    return mException;
  }
  
  /** Get the type of this event
   * @return The type can be;
   *          EVENTTYPE_UNKNOWN               = Unknown event type
   *          EVENTTYPE_STATECHANGED          = The state of the tool instance is changed,
   *                                            call getEventState() for the new state
   *          EVENTTYPE_GENERALEXCEPTION      = A general exception occured within the tool instance.
   *                                            call getEventException() for the thrown exception.
   *          EVENTTYPE_TOOLINSTANCEEXCEPTION = A general exception occured within the tool instance external execution.
   *                                            call getEventException() for the thrown exception.
   *          EVENTTYPE_TOOLOUTPUTREPORT      = Output is generated by the tool getEventStringValue()
   */
  @Override
  public int getEventType() {
    return mEventType;
  }
  
  /** Get event string value
   */
  @Override
  public String getEventStringValue() {
    if (mStringValue!=null) {
      return mStringValue;
    } else if (mException!=null) {
      return mException.getMessage();
    }
    return null;
  }

  /** Get event value
   *
   * @return
   */
  @Override
  public double getEventValue() {
    return mValue;
  }


  /** Get the event level, eq. java.util.Logger.Level
   */
  @Override
  public Level getEventLevel() {
    return mLevel;
  }
  
  /** Is this a blocking event?
   */
  @Override
  public boolean getEventBlocking() {
    return mBlocking;
  }

  @Override
  public boolean hasEventNode() {
    return mEventNodePath!=null;
  }

  
  @Override
  public void setEventNode( Node pNode ) throws RepositoryException {
    mEventNodePath = pNode.getPath();
    return;
  }
  
  @Override
  public Node getEventNode( final Session pSession ) throws RepositoryException {
    if (mEventNodePath!=null) {
      return pSession.getNode( mEventNodePath );
    }
    return null;
  }
  
  /** toString
   *
   * @return
   */
  @Override
  public String toString() {
    String event = "Eventtype=" + mEventType + ", Level=" + mLevel + ", State=" + mState + ", SValue=" + mStringValue + ", Val=" + mValue;
    if (mException!=null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      mException.printStackTrace(pw);
      event += "\nException\n" + sw.getBuffer().toString();
    }
    return event;
  }

}
