/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars;

import java.util.Calendar;
import java.util.logging.LogRecord;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.tools.CARS_ToolInstanceEvent;

/**
 *
 * @author weert
 */
public class CARS_Event implements ICARS_Event {

  private final Calendar    mCreationDate = Calendar.getInstance();
  private       String      mUser;
  private       String      mSource;
  private final String      mMessage;
  private final String      mApplication;
  private final String      mCategory;
  private final String      mFolder;
  private final String      mEventType;
  private final String      mType;
  private final Throwable   mThrowable;
  private final long        mCode;
  private final String      mBody;
  
  private CARS_ToolInstanceEvent mToolInstanceEvent = null;
  
  /** CARS_Event
   * 
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pMessage
   * @param pCode
   * @throws javax.jcr.RepositoryException 
   */
  public CARS_Event(
          final Node pUser,
          final Node pSource,
          final String pApplication,
          final String pCategory,
          final String pType,
          final String pMessage,
          final long   pCode ) {
    init( pUser, pSource );
    mMessage = pMessage;
    mApplication = pApplication;
    mCategory = pCategory;
    mType = pType;
    mFolder = null;
    mEventType = null;
    mThrowable = null;
    mCode = pCode;
    mBody = null;
    return;
  }
  
  /** CARS_Event
   * 
   * @param pMain
   * @param pFolder
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pThrow
   * @param pMessage 
   */
  public CARS_Event(
          final CARS_Main pMain,
          final String pFolder,
          final String pApplication,
          final String pCategory,
          final String pType,
          final Throwable pThrow,
          final String pMessage,
          final String pEventType ) {
    if (pMain==null) {
      init( null, null );
    } else {
      init( pMain.getLoginUser(), pMain.getCurrentViewNode() );
    }
    mMessage = pMessage;
    mApplication = pApplication;
    mCategory = pCategory;
    mType = pType;
    mFolder = pFolder;
    mEventType = pEventType;
    mThrowable = pThrow;
    mCode = 0;
    mBody = null;
    return;
  }

  /**
   * 
   * @param pMain
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pContext 
   */
  public CARS_Event(
          final CARS_Main pMain,
          final String pApplication,
          final String pCategory,
          final String pType,
          final CARS_ActionContext pContext,
          final String pMessage ) {
    if (pMain==null) {
      init( null, null );
    } else {
      init( pMain.getLoginUser(), pMain.getCurrentViewNode() );
    }
    mApplication = pApplication;
    mCategory = pCategory;
    mType = pType;
    mFolder = null;
    mEventType = null;
    if (pContext==null) {
      mThrowable = null;
      if (pMessage==null) {
        mMessage = null;
      } else {
        mMessage = pMessage;
      }
      mCode = 0;
    } else {
      mThrowable = pContext.getError();
      if (pMessage==null) {
        mMessage = pContext.getPathInfo();
      } else {
        mMessage = pMessage;
      }
      mCode = pContext.getErrorCode();
    }
    mBody = null;
    return;
  }

          
  public CARS_Event(
          final Node pUser,
          final Node pSource,
          final String pApplication,
          final String pCategory,
          final String pType,
          final LogRecord pLog ) {
    init( pUser, pSource );
    mApplication = pApplication;
    mCategory = pCategory;
    mType = pType;
    mFolder = null;
    mEventType = null;
    if (pLog==null) {
      mMessage = null;
      mCode = 0;
      mThrowable = null;
    } else {
      mCreationDate.setTimeInMillis( pLog.getMillis() );
      mMessage = pLog.getMessage();
      mCode = 0;
      mThrowable = pLog.getThrown();
    }
    mBody = null;
    return;    
  }

  
  /** init
   * 
   * @param pUser
   * @return 
   */
  private ICARS_Event init( final Node pUser, final Node pSource ) {
    if (pUser==null) {
      mUser = null;
    } else {
      try {
        mUser = pUser.getPath();
      } catch(RepositoryException e) {
        mUser = e.getMessage();
      }
    }
    if (pSource==null) {
      mSource = null;
    } else {
      try {
        mSource = pSource.getPath();
      } catch(RepositoryException e) {
        mSource = e.getMessage();
      }      
    }
    return this;
  }
  
  @Override
  public String message() {
    return mMessage;
  }

  @Override
  public String application() {
    return mApplication;
  }

  @Override
  public String category() {
    return mCategory;
  }

  @Override
  public String folder() {
    return mFolder;
  }

  @Override
  public String eventType() {
    return mEventType;
  }

  @Override
  public String type() {
    return mType;
  }
  
  @Override
  public String user() {
    return mUser;
  }
  
  @Override
  public Calendar creationDate() {
    return mCreationDate;
  }

  @Override
  public Throwable throwable() {
    return mThrowable;
  }

  @Override
  public String source() {
    return mSource;
  }

  @Override
  public long code() {
    return mCode;
  }

  @Override
  public ICARS_Event toolInstanceEvent(CARS_ToolInstanceEvent pE) {
    mToolInstanceEvent = pE;
    return this;
  }

  @Override
  public CARS_ToolInstanceEvent toolInstanceEvent() {
    return mToolInstanceEvent;
  }

  @Override
  public String body() {
    return mBody;
  }
  

  
}
