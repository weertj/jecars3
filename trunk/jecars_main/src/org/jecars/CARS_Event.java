/*
 * Copyright 2014 NLR - National Aerospace Laboratory
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
  private       String      mBody;
  private       boolean     mWaitForEventNode = false;
  private       String      mEventNodePath = null;
  
  private final String      mReferer;
  private final String      mUserAgent;
  private final String      mRemoteHost;
  
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
    mReferer = null;
    mRemoteHost = null;
    mUserAgent = null;
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
      mReferer = null;
      mRemoteHost = null;
      mUserAgent = null;
    } else {
      init( pMain.getLoginUser(), pMain.getCurrentViewNode() );
      final CARS_ActionContext ac = pMain.getContext();
      if (ac==null) {
        mReferer = null;
        mRemoteHost = null;
        mUserAgent = null;        
      } else {
        mReferer = ac.getReferer();
        mRemoteHost = ac.getRemoteHost();
        mUserAgent = ac.getUserAgent();
      }
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
    if (pContext==null) {
      mReferer = null;
      mRemoteHost = null;
      mUserAgent = null;        
    } else {
      mReferer = pContext.getReferer();
      mRemoteHost = pContext.getRemoteHost();
      mUserAgent = pContext.getUserAgent();
    }

    return;
  }

  /** CARS_Event
   * 
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pLog 
   */
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
    mReferer = null;
    mRemoteHost = null;
    mUserAgent = null;        
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
  
  @Override
  public ICARS_Event body( final String pBody ) {
    mBody = pBody;
    return this;
  }

  @Override
  public boolean waitForEventNode() {
    return mWaitForEventNode;
  }

  @Override
  public ICARS_Event waitForEventNode(boolean pW) {
    mWaitForEventNode = pW;
    return this;
  }

  @Override
  public String eventNode() {
    return mEventNodePath;
  }

  @Override
  public ICARS_Event eventNode(Node pNode) throws RepositoryException {
    mEventNodePath = pNode.getPath();
    return this;
  }

  @Override
  public String referer() {
    return mReferer;
  }

  @Override
  public String remoteHost() {
    return mRemoteHost;
  }

  @Override
  public String userAgent() {
    return mUserAgent;
  }

  
}
