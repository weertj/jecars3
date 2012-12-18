/*
 * Copyright 2007-2011 NLR - National Aerospace Laboratory
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import nl.msd.jdots.JD_Taglist;
import org.jecars.*;
import org.jecars.jaas.CARS_Credentials;

/**
 *  CARS_DefaultToolInterface
 *
    [jecars:Tool] > jecars:dataresource
    - jecars:ToolClass      (String)
    - jecars:StateRequest   (String) < '(start|suspend|stop)'
    - jecars:State          (String)='open.notrunning' mandatory autocreated < '(open.notrunning|open.notrunning.suspended|open.running|closed.completed|closed.abnormalCompleted|closed.abnormalCompleted.terminated|closed.abnormalCompleted.aborted)'
    - jecars:PercCompleted  (Double)='0'
    + *                     (jecars:parameterresource) multiple
    + *                     (jecars:inputresource)     multiple
    + *                     (jecars:outputresource)    multiple
 
 * 
 * @version $Id: CARS_DefaultToolInterface.java,v 1.32 2009/07/02 07:43:44 weertj Exp $
 */
public class CARS_DefaultToolInterface implements CARS_ToolInterface, CARS_ToolInstanceListener, CARS_ToolSignalListener {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools" );

  static final protected String             LF = "\r\n";
  static final public    String NEWTOOL_FOLDER = "new";

  static public int SES_COREPOOLSIZE = 10;
//  static public int ES_COREPOOLSIZE  = 20;

  static final private ScheduledExecutorService gScheduledExecutorService;
  static final private ExecutorService          gSingleExecutorService;
  static final private ExecutorService          gExecutorService;
  
//  private ToolThread                        mToolThread = null;
  // **** Running tool instances
  private transient ScheduledFuture                   mScheduledFuture = null;
  private transient Future                            mFuture          = null;

  private transient String                            mUsername   = null;
  private transient char[]                            mPassword   = null;
  private transient String                            mAuth       = null;
  private transient String                            mPauseAtState     = STATE_NONE;
  private transient String                            mInterruptAtState = STATE_NONE;
  private transient List<CARS_ToolInstanceListener>   mListeners    = null;
  private transient String                            mUUID         = null;
  private transient Node                              mConfigNode   = null;
  private transient Map<String,String>                mToolArgs     = null;
  private transient boolean                           mStoreEvents   = false;
  private transient boolean                           mReplaceEvents = false;
  private transient boolean                           mIsScheduled   = false;
  private transient long                              mDelayInSecs   = -1L;
  private transient boolean                           mIsSingle      = false;
  private transient int                               mSendOutputsLinesAsBatch = 1;
  private transient int                               mNoOfCachedOutputLines = 0;
  private transient StringBuilder                     mCachedOutputLines = new StringBuilder();
  private transient Map<String, String>               mStringParams = null;
  private transient Map<String, Double>               mDoubleParams = null;
  private transient Map<String, Long>                 mLongParams   = null;
  private transient int                               mRunningExpireMinutes = 60;
  private transient int                               mClosedExpireMinutes = 60;
  private transient String                            mToolPath = "";
  private transient boolean                           mRebuildToolSession = true;
  private transient String                            mCurrentState = "";
  private transient int                               mThreadPriority = Thread.MIN_PRIORITY;

  private transient CARS_Main mMain     = null;
  private transient Node      mToolNode = null;

  static final public String CONFIGNODE_RUNNINGEXPIREMINUTES = "jecars:RunningExpireMinutes";
  static final public String CONFIGNODE_CLOSEDEXPIREMINUTES  = "jecars:ClosedExpireMinutes";
  
  static protected Locale gToolLocale = Locale.getDefault();

  /** The tool resource bundles
   */
  static final protected Collection<ResourceBundle> gToolResourceBundles     = new ArrayList<ResourceBundle>();
  static final protected Collection<String>         gToolResourceBundleNames = new ArrayList<String>();

  
  static {
    String bundle = "JeCARS_ToolBundle";
    if (!gToolResourceBundleNames.contains( bundle )) {
      gToolResourceBundleNames.add( bundle );
    }
    gScheduledExecutorService = Executors.newScheduledThreadPool( SES_COREPOOLSIZE );
//    gExecutorService          = Executors.newFixedThreadPool( ES_COREPOOLSIZE, new CARS_ThreadFactory( "CARS_ToolFixed", Thread.MIN_PRIORITY ));
    gExecutorService          = Executors.newCachedThreadPool( new CARS_ThreadFactory( "CARS_ToolFixed", Thread.MIN_PRIORITY ));
    gSingleExecutorService    = Executors.newSingleThreadExecutor();
  }

  /** destroy
   *
   */
  static public final void destroy() {
    System.out.println( "CARS_DefaultToolInterface: shutdown" );
    gSingleExecutorService.shutdownNow();
    gScheduledExecutorService.shutdownNow();
    gExecutorService.shutdownNow();
    return;
  }

  /** initToolFolder
   * @param pFolder
   * @throws java.lang.Exception
   */
  static public void initToolFolder( Node pFolder ) throws Exception {
    pFolder.addNode( NEWTOOL_FOLDER, "jecars:Tools" );
//    pFolder.addNode( "open", "jecars:Tools" );
//    pFolder.addNode( "closed", "jecars:Tools" );
//    pFolder.addNode( "paused", "jecars:Tools" );
//    pFolder.addNode( "unknown", "jecars:Tools" );
    return;
  }

  @Override
  public void setThreadPriority( final int pP ) {
    mThreadPriority = pP;
    return;
  }

  @Override
  public int getThreadPriority() {
    return mThreadPriority;
  }

  /** Thread which handles the tool execution
   */
//  protected class ToolRunnable implements Callable {
  protected class ToolRunnable implements Runnable {

    public CARS_DefaultToolInterface getToolInterface() {
      return CARS_DefaultToolInterface.this;
    }
      
    /** run
     */
    @Override
    public void run() {
//      String toolPath = "";
      Session newSession = null;
      try {
//        getTool().save();
        if (mRebuildToolSession) {
          newSession = createToolSession();
          mToolNode = newSession.getNode( mToolPath );
        }
        // **** TODO When the tools is renamed or rewritten there is a InvalidItemStateException.... reread the tool
        setExpireDateTool( getTool(), getRunningExpireMinutes() );
        moveToolTo( "open" );
//        _toolInit();
        _initEventFolder();
        setState( CARS_ToolInterface.STATE_OPEN_RUNNING_INIT );
        toolInit();
        setState( CARS_ToolInterface.STATE_OPEN_RUNNING_PARAMETERS );
        toolParameters();
        setState( CARS_ToolInterface.STATE_OPEN_RUNNING_INPUT );
        toolInput();
        if (pauseCheck()) {
          inPauseState();
          return;
        }
        setState( CARS_ToolInterface.STATE_OPEN_RUNNING );
        toolRun();
        if (STATE_OPEN_ABORTING.equals( getState() )) {
          // **** Tool is aborted
          setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
        } else {
          // **** Tool is ready running
          setState( CARS_ToolInterface.STATE_OPEN_RUNNING_OUTPUT );
          toolOutput();
          if (isScheduledTool()) {
            setState( CARS_ToolInterface.STATE_CLOSED_COMPLETED_SCHEDULED );
          } else {
            setState( CARS_ToolInterface.STATE_CLOSED_COMPLETED );
          }
        }
        toolExit();
        if (!isScheduledTool()) moveToolTo( "closed" );
      } catch( InterruptedException ie ) {
        LOG.log( Level.WARNING, null, ie );
        reportException( ie, Level.WARNING );
        setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
        try {
          setExpireDateTool( getTool(), getClosedExpireMinutes() );
        } catch (Exception ee) {
          LOG.log( Level.WARNING, null, ee );
        }
      } catch (CARS_ToolException te) {
//        try {
        LOG.log( Level.WARNING, mToolPath, te );
        reportException( te, Level.WARNING );
//        } catch( Exception ee ) {
//          LOG.log( Level.WARNING, null, te );
//          reportException( ee, Level.WARNING );
//        }
        setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
      } catch (Exception e) {
        // **** Exception
//        try {
        LOG.log( Level.WARNING, mToolPath, e );
        reportException( e, Level.WARNING );
//        } catch( Exception ee ) {
//          LOG.log( Level.WARNING, null, ee );
//          reportException( ee, Level.WARNING );
//        }
        setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
        try {
          setExpireDateTool( getTool(), getClosedExpireMinutes() );
        } catch (Exception ee) {
          LOG.log( Level.WARNING, null, ee );
          reportException( ee, Level.WARNING );
        }
      } catch (Throwable e) {
        // **** Throwable
        try {
          LOG.log( Level.SEVERE, mToolPath, e );
          reportException( e, Level.SEVERE );
        } catch( Exception ee ) {
          LOG.log( Level.WARNING, null, ee );
          reportException( ee, Level.WARNING );
        }
        setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
        try {
          setExpireDateTool( getTool(), getClosedExpireMinutes() );
        } catch (Exception ee) {
          LOG.log( Level.WARNING, null, ee );
          reportException( ee, Level.WARNING );
        }
      } finally {
        toolFinally();
        try {
          if (!isScheduledTool()) {
            getTool().setProperty( "jecars:StateRequest", STATEREQUEST_STOP );
            getTool().save();
          }
          getTool().save();
        } catch (Exception e) {
          LOG.log( Level.WARNING, null, e );
          reportException( e, Level.WARNING );
          try {
            getTool().refresh(false);
          } catch (Exception ex) {
            LOG.log( Level.SEVERE, null, ex );
            reportException( ex, Level.SEVERE );
          }
        }
        try {
          if (!isScheduledTool()) {
            mMain.destroy();
            if (newSession!=null) {
              newSession.logout();
            }
          } else {
            mRebuildToolSession = false;
          }
        } catch( Exception e ) {
          LOG.log( Level.WARNING, null, e );
        }
//        if (newSession!=null) {
//          newSession.logout();
//        }
      }
      return;
    }

    protected void inPauseState() throws Exception {
      moveToolTo( "paused" );
      return;
    }

  }


  /** setRunningExpireMinutes
   *
   * @param pMin
   */
  public void setRunningExpireMinutes( final int pMin ) {
    mRunningExpireMinutes = pMin;
    return;
  }

  /** getRunningExpireMinutes
   * 
   * @return
   */
  public int getRunningExpireMinutes() {
    try {
      final Node cn = getConfigNode();
      mRunningExpireMinutes = (int)cn.getProperty( CONFIGNODE_RUNNINGEXPIREMINUTES ).getValue().getLong();
    } catch( Exception e ) {        
    }
    return mRunningExpireMinutes;
  }

  /** setRunningExpireMinutes
   *
   * @param pMin
   */
  public void setClosedExpireMinutes( final int pMin ) {
    mClosedExpireMinutes = pMin;
    return;
  }

  /** getRunningExpireMinutes
   *
   * @return
   */
  public int getClosedExpireMinutes() {
    try {
      final Node cn = getConfigNode();
      mClosedExpireMinutes = (int)cn.getProperty( CONFIGNODE_CLOSEDEXPIREMINUTES ).getValue().getLong();
    } catch( Exception e ) {        
    }
    return mClosedExpireMinutes;
  }
  
  /** Move the tool to another leaf
   * @param pLeaf
   * @return The moved node
   * @throws java.lang.Exception
   */
  protected Node moveToolTo( String pLeaf ) throws Exception {
    Node t = getTool(), newt = null;
    t.save();
    newt = t;
//    String uuid = t.getUUID();
//    String newPath = t.getParent().getParent().getPath() + "/" + pLeaf + "/" + t.getName();
//    t.getSession().getWorkspace().move( t.getPath(), newPath );
//    newt = t.getSession().getNodeByUUID( uuid );
    if (pLeaf.equals( "closed" )) {
      setExpireDateTool( newt, getClosedExpireMinutes() );
    }
    if (pLeaf.equals( "paused" )) {
      setExpireDateTool( newt, 10 );
    }
    return newt;
  }
 
  /** setExpireDateTool
   * @param pNode
   * @param pMinutes
   * @throws java.lang.Exception
   */
  static public void setExpireDateTool( final Node pNode, final int pMinutes ) throws Exception {
    if (pMinutes==-1) {
      // **** Remove the expire date
      pNode.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );
    } else {
      Calendar cal = Calendar.getInstance();
      cal.add( Calendar.MINUTE, pMinutes );
      pNode.setProperty( CARS_ActionContext.gDefExpireDate, cal );    
      pNode.setProperty( "jecars:Modified", Calendar.getInstance() );
    }
    return;
  }

  public void toolInitSettings() throws Exception {
    _toolInitSettings();
    return;
  }
  
  /** _toolInit
   *
   * @throws java.lang.Exception
   */
  private void _toolInitSettings() throws Exception {
    if (mToolNode!=null) {
      Property p = getResolvedToolProperty( mToolNode, "jecars:ReplaceEvents" );
      if (p!=null) mReplaceEvents = p.getBoolean();
      p = getResolvedToolProperty( mToolNode, "jecars:IsScheduled" );
      if (p!=null) mIsScheduled = p.getBoolean();
      p = getResolvedToolProperty( mToolNode, "jecars:StoreEvents" );
      if (p!=null) mStoreEvents = p.getBoolean();
      p = getResolvedToolProperty( mToolNode, "jecars:IsSingle" );
      if (p!=null) mIsSingle = p.getBoolean();
      p = getResolvedToolProperty( mToolNode, "jecars:DelayInSecs" );
      if (p!=null) mDelayInSecs = p.getLong();
    }
    return;
  }

  /** _initEventFolder
   *
   * @throws java.lang.Exception
   */
  private void _initEventFolder() throws Exception {
    if (replaceEvents()) {
      if (mToolNode.hasNode( "jecars:Events" )) {
        mToolNode.getNode( "jecars:Events" ).remove();
        mToolNode.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
        mToolNode.save();
      }
    }
    if (storeEvents()) {
        if (mToolNode.hasNode( "jecars:Events" )==false) {
          Node events = mToolNode.addNode( "jecars:Events", "jecars:ToolEvents" );
          mToolNode.save();
          Node ev = events.getNode( "jecars:EventsSEVERE" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourSEVERE", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsWARNING" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourWARNING", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsINFO" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourINFO", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsCONFIG" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourCONFIG", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsFINE" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourFINE", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsFINER" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourFINER", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsFINEST" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourFINEST", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsUNKNOWN" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourUNKNOWN", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsSTATE" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourSTATE", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsINSTANCE" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourINSTANCE", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsOUTPUT" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourOUTPUT", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsMESSAGE" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourMESSAGE", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsSTATUS" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourSTATUS", -1 );      // **** Events will be expired by the tool node
          ev.save();
          ev = events.getNode( "jecars:EventsPROGRESS" );
          ev.setProperty( "jecars:StoreEventsPer", "NONE" );        // **** Flat representation
          ev.setProperty( "jecars:ExpireHourPROGRESS", -1 );      // **** Events will be expired by the tool node
          ev.save();
        }
    }
    return;
  }

  /** Superclass must implement this method to actually init the tool
   */
  protected void toolInit() throws Exception {
    return;
  }
  
  /** Superclass must implement this method to actually get the parameters for the tool
   * @throws java.lang.Exception
   */
  protected void toolParameters() throws Exception {
    final List<Node> params = getParameters();
    for ( Node param : params) {
      if (param.isNodeType( "jecars:parameterdata" )) {
        if (param.hasProperty( "jecars:string" )) {
          if (mStringParams==null) {
            mStringParams = new HashMap<String, String>();
          }
          Value[] vals = param.getProperty( "jecars:string" ).getValues();
          if (vals!=null) {
            int i = 0;
            for( Value val : vals ) {
              mStringParams.put( param.getName() + ".string." + i++, val.getString() );
            }
          }
        }
        if (param.hasProperty( "jecars:double" )) {
          if (mDoubleParams==null) {
            mDoubleParams = new HashMap<String, Double>();
          }
          Value[] vals = param.getProperty( "jecars:double" ).getValues();
          if (vals!=null) {
            int i = 0;
            for( Value val : vals ) {
              mDoubleParams.put( param.getName() + ".double." + i++, val.getDouble() );
            }
          }
        }
        if (param.hasProperty( "jecars:long" )) {
          if (mLongParams==null) {
            mLongParams = new HashMap<String, Long>();
          }
          Value[] vals = param.getProperty( "jecars:long" ).getValues();
          if (vals!=null) {
            int i = 0;
            for( Value val : vals ) {
              mLongParams.put( param.getName() + ".long." + i++, val.getLong() );
            }
          }
        }
      }
    }
    return;
  }

  /** Superclass must implement this method to actually get the inputs for the tool
   * @throws java.lang.Exception
   */
  protected void toolInput() throws Exception {
    return;
  }

  /** Superclass must implement this method to actually start the tool
   */
  protected void toolRun() throws Exception {
    reportOutput( null );
    return;
  }

  /** Superclass must implement this method to actually process the outputs for the tool
   * @throws java.lang.Exception
   */
  protected void toolOutput() throws Exception {
    reportOutput( null );
    return;
  }

  /** Superclass must implement this method to actually finish the tool
   */
//  @OverridingMethodsMustInvokeSuper
  protected void toolExit() throws Exception {
    reportOutput( null );
    Node t = getTool();
    t.save();
    if (t.isLocked()) {
      t.unlock();
//      SessionImpl appSession = (SessionImpl)CARS_Factory.getSystemApplicationSession();
//      synchronized( appSession ) {
//        // **** Unlock as system
//        Node syst = appSession.getNode( t.getPath() );
//        syst.unlock();
//        syst.save();
//      }
    }
    t.save();
    return;
  }

  /** toolFinally, called when a tool is entered the finally {} part
   *
   */
  protected void toolFinally() {
//    if (mFuture!=null) {
//      mFuture.cancel( true );
//      mFuture = null;
//    }
    return;
  }
 
  
  /** Returns an unique identifier on this system.
   * @return the unique identifier. 
   */
  @Override
  public String getUUID() {
    return mUUID;
  }
  
  protected void setUUID( final String pUuid ) {
    mUUID = pUuid;
    return;
  }
  
  /** Set the tool node, when the tool has ended the CARS_Main context must be destroyed
   * @param pMain the CARS_Main context
   * @param pTool the tool
   * @throws Exception when an error occurs
   */  
  @Override
  public void   setTool( final CARS_Main pMain, final Node pTool ) {
    mMain     = pMain;
    mToolNode = pTool;
    return;
  }

  /** Get the tool node
   * @return the tool
   * @throws Exception when an error occurs
   */
  @Override
  public Node   getTool() {
    return mToolNode;
  }

  /** getToolTemplate
   * 
   * @param pTool
   * @return
   * @throws RepositoryException
   */
  public Node getToolTemplate( final Node pTool ) throws RepositoryException {
    final String path = getToolTemplatePath();
    if (path!=null) {
      return pTool.getSession().getRootNode().getNode( path.substring(1) );
    }
    return null;
  }

  /** getToolTemplatePath
   *
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public String getToolTemplatePath() throws RepositoryException {
    if (mToolNode.hasProperty( "jecars:ToolTemplate" )) {
      return mToolNode.getProperty( "jecars:ToolTemplate" ).getString();
    }
    return null;
  }


  /** getMain
   * @return
   */
  protected CARS_Main getMain() {
    return mMain;
  }
  
  /** Get the name of the tool
   * @return the name
   * @throws java.lang.Exception
   */
  @Override
  public String  getName() throws Exception {
    return getTool().getName();
  }

  /** Get the title of the tool (human readable)
   * @return the title
   * @throws java.lang.Exception
   */
  @Override
  public String getTitle() throws Exception {
    final Node n = getTool();
    if (n.hasProperty( CARS_ActionContext.gDefTitle )) {
      return n.getProperty( CARS_ActionContext.gDefTitle ).getString();
    }
    return getName();
  }

  /** Set the name of the tool
   * @param pName
   * @throws java.lang.Exception
   */
  @Override
  public void setName( String pName ) throws Exception {
    
  }

  /** Set authentication string
   * 
   * @param pAuth
   */
  @Override
  public void setCredentialAuth( String pAuth ) {
    mAuth = pAuth;
    return;
  }

  /** getCredentialAuth
   * 
   * @return
   */
  public String getCredentialAuth() {
    return mAuth;
  }
  
  /** Set the username and password for the instance
   * @param pUsername
   * @param pPassword
   */
  @Override
  public void setCredentials( String pUsername, char[] pPassword ) {
    mUsername = pUsername;
    mPassword = pPassword;
    return;
  }

  protected String getUsername() {
    return mUsername;
  }
  
  protected char[] getPassword() {
    return mPassword;
  }
  
 
  /** Get the tool thread object
   * @return
   */
//  @Override
//  public CARS_DefaultToolInterface.ToolThread getToolThread() {
//    return mToolThread;
//  }

  
  /** Set the current staterequest of the tool
   * @param pStateRequest STATEREQUEST_*
   * @return jecars:StateRequest property
   * @throws Exception when an error occurs
   */
  @Override
  public Property setStateRequest( final String pStateRequest ) throws Exception {
    final Property p = getTool().setProperty( "jecars:StateRequest", pStateRequest );
    getTool().save();
    mToolPath = getTool().getPath();

    _toolInitSettings();

    if (pStateRequest.equals(STATEREQUEST_START)) {
      if (isScheduledTool()) {
        LOG.info( "Running as scheduled executor: " + this );
        mScheduledFuture = gScheduledExecutorService.scheduleWithFixedDelay( new ToolRunnable(), getDelayInSecs(), getDelayInSecs(), TimeUnit.SECONDS );
      } else if (isSingleTool()) {
        LOG.info( "Running as single executor: " + this );
        mFuture = gSingleExecutorService.submit( new ToolRunnable() );
      } else {
        LOG.info( "Running as executor: " + this );
        mFuture = gExecutorService.submit( new ToolRunnable() );
//        Thread thread = new Thread( new ToolRunnable() );
//        thread.setPriority( getThreadPriority() );
//        thread.setName( getTool().getPath() );
//        thread.start();        
      }
    } else if (pStateRequest.equals(STATEREQUEST_PAUSE)) {
      // **** PAUSE REQUEST
      setState( STATE_PAUSED );
    } else if (pStateRequest.equals(STATEREQUEST_RESUME)) {
      // **** RESUME REQUEST
      setState( STATE_OPEN_RUNNING );
    } else if (pStateRequest.equals(STATEREQUEST_STOP)) {
      // **** STOP request
      if (STATEREQUEST_STOP.equals( getStateRequest() )) {
        // **** Two stop requests is abort(!)
        if (mScheduledFuture!=null) {
          mScheduledFuture.cancel( true );
        }
        if (mFuture!=null) {
          mFuture.cancel( true );
        }
        setState( STATE_OPEN_ABORTING );
      } else if (isScheduledTool()) {
        if (mScheduledFuture!=null) {
          if (!mScheduledFuture.cancel( false )) {
            throw new CARS_ToolException( getTool().getPath() + " cannot be cancelled" );
          }
        }
      }
    }
    return p;
  }

  /** Get the current staterequest of the tool
   * @return STATEREQUEST_*
   * @throws Exception when an error occurs
   */
  @Override
  public String getStateRequest() throws Exception {
    return getTool().getProperty( "jecars:StateRequest" ).getString();
  }

  /** Get the current state of the tool
   * @return STATE_*
   * @throws Exception when an error occurs
   */
  @Override
  public String getState() throws Exception {
    return getTool().getProperty( "jecars:State" ).getString();    
  }
  
  /** getCurrentState
   * 
   * @return
   */
  protected String getCurrentState() {
    return mCurrentState;
  }

  /** Internal set state
   * @param The state
   */
  public void setState( final String pState ) {
    try {
      if (!mCurrentState.equals(pState)) {
        mCurrentState = pState;
        getTool().setProperty( "jecars:State", pState );
        getTool().setProperty( "jecars:Modified", Calendar.getInstance() );
        getTool().save();
        reportToInstanceListeners( 
           CARS_DefaultToolInstanceEvent.createEventState( this, pState ));
      }
    } catch (Exception e) {
      LOG.log( Level.WARNING, null, e );
    }
    return;
  }
  
  /** Set the state at (after) which the tool execution must pause
   * @param pState Possible pause states which can be
   *               STATE_PAUSED        = Disable pause
   *               STATE_OPEN_RUNNING_INPUT = The tool is converting context data.
   */
  @Override
  public void setPauseAtState( String pState ) {
    mPauseAtState = pState;
    return;
  }

  /** Set the state at which the tool must be interupted, the tool implementation itself must detect this
   * @param pState
   */
  @Override
  public void setInterruptAtState( String pState ) {
    mInterruptAtState = pState;
    return;
  }

  /** getInterruptState
   * @return
   */
  protected String getInterruptState() {
    return mInterruptAtState;
  }
  
  /**
   * @return true when paused
   */
  protected boolean pauseCheck() throws Exception {
    if (getState().equals(mPauseAtState)) {
      setState( getState() + STATE_PAUSED );
//      reportToInstanceListeners( LPF_DefaultToolInstanceEvent.createEventState( this, getState() ));
      return true;
    }
    return false;
  }

  
  /** Get percentage completed for the tools
   * @return [0.0-1.0]
   * @throws Exception when an error occurs
   */
  @Override
  public double getPercCompleted() throws Exception {
    if (getTool().hasProperty( "jecars:PercCompleted" )==true) {
      return getTool().getProperty( "jecars:PercCompleted" ).getDouble();
    }
    return 0.0;
  }
  
  /** addToolArgument
   * 
   * @param pKey
   * @param pValue
   * @throws java.lang.Exception
   */
  @Override
  public void addToolArgument( final String pKey, final String pValue ) throws Exception {
    if (mToolArgs==null) mToolArgs = new HashMap<String, String>();
    mToolArgs.put( pKey, pValue );
    return;
  }
  
  /** getToolArgument
   * 
   * @param pKey
   * @return
   */


  @Override
  public String getToolArgument( String pKey ) {
    if (mToolArgs!=null) {
      String v = mToolArgs.get( pKey );
      return v;
    } else {
      return null;
    }
  }
  
  
  /** Add the node in which the configuration data for this tool is stored
   * @param pNode JCR Node
   * @throws Exception when an error occurs
   */
  @Override
  public void addConfigNode( final Node pNode ) {
    mConfigNode = pNode;
    return;
  }

  /** hasConfigNode
   * 
   * @return
   * @throws RepositoryException
   */
  public boolean hasConfigNode() throws RepositoryException {
    return getTool().hasNode( "jecars:Config" );
  }

  /** copyConfigNodeToTool
   *
   * @param pConfigNode
   * @throws RepositoryException
   */
  public void copyConfigNodeToTool( final Node pConfigNode ) throws RepositoryException {
    pConfigNode.getSession().getWorkspace().copy( pConfigNode.getPath(), getTool().getPath() + "/jecars:Config" );
    addConfigNode( null );
    return;
  }


  /** getConfigNode
   *
   * @return the JCR node in which the configuration data for the tool is stored
   * @throws javax.jcr.RepositoryException
   * @throws java.lang.Exception
   */
  @Override
  public Node getConfigNode() throws Exception {
    if (mConfigNode!=null) {
      return mConfigNode;
    }
    addConfigNode( getResolvedToolNode( getTool(), "jecars:Config" ));
    return mConfigNode;
  }

  
  /** Get an iterator with parameters nodes (jecars:Parameter.*)
   * @return the parameters
   * @throws Exception when an error occurs
   */
  @Override
  public List<Node> getParameters() throws Exception {

    final List<Node> nodes = new ArrayList<Node>();
    final List<Node> tools = getAllToolNodes( getTool() );
    for ( Node tool : tools ) {
      final NodeIterator ni = tool.getNodes();
      Node n;
      while( ni.hasNext() ) {
        n = ni.nextNode();
        if (n.isNodeType( "jecars:parameterresource" )) {
          nodes.add( n );
        }
      }
    }
    return nodes;
  }

  /** getParametersString
   *
   * @param pName
   * @param pIndex
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public String getParameterString( final String pName, final int pIndex ) throws Exception {
    if (mStringParams!=null) {
      return mStringParams.get( pName + ".string." + pIndex );
    }
    return null;
  }



  /** addInput
   * @param pInput
   * @throws java.lang.Exception
   */
  @Override
  public void addInput( InputStream pInput ) throws Exception {
    addInput( pInput, "text/plain" );
    return;
  }

  /** addInput
   * 
   * @param pInput
   * @param pMimeType
   * @throws java.lang.Exception
   */
  @Override
  public void addInput( InputStream pInput, String pMimeType ) throws Exception {
    Node inputN = getTool().addNode( "jecars:Input", "jecars:inputresource" );
    inputN.setProperty( "jcr:mimeType", pMimeType );
    inputN.setProperty( "jcr:data", pInput );
    Calendar c = Calendar.getInstance();
    inputN.setProperty( "jcr:lastModified", c );
    return;
  }
  
  /** addOutput
   * @param pOutput
   * @throws java.lang.Exception
   */
  protected void addOutput( final String pOutput ) throws Exception {
    final Node n = getTool();
    final Node output = n.addNode( "jecars:Output", "jecars:outputresource" );
    output.setProperty( "jcr:mimeType", "text/plain" );
    output.setProperty( "jcr:data", pOutput );
    final Calendar c = Calendar.getInstance();
    output.setProperty( "jcr:lastModified", c );
    return;
  }

  /** replaceOutput
   *
   * @param pNodeName
   * @param pOutput
   * @return
   * @throws RepositoryException
   */
  protected Node replaceOutput( final Node pTool, final String pNodeName, final String pOutput ) throws RepositoryException {
//    final Node n = getTool();
    if (!pTool.hasNode( pNodeName )) {
      pTool.addNode( pNodeName, "jecars:outputresource" );
    }
    final Node output = pTool.getNode( pNodeName );
    output.setProperty( "jcr:mimeType", "text/plain" );
    output.setProperty( "jcr:data", pOutput );
    output.setProperty( "jcr:lastModified", Calendar.getInstance() );
    output.setProperty( "jecars:Title", pNodeName );
    output.setProperty( "jecars:ContentLength", pOutput.length() );
    return output;
  }

  /** replaceOutput
   * 
   * @param pTool
   * @param pNodeName
   * @param pOutput
   * @return
   * @throws RepositoryException 
   */
  protected Node replaceOutput( final Node pTool, final String pNodeName, final InputStream pOutput ) throws RepositoryException {
    if (!pTool.hasNode( pNodeName )) {
      pTool.addNode( pNodeName, "jecars:outputresource" );
    }
    final Node output = pTool.getNode( pNodeName );
    output.setProperty( "jcr:mimeType", "text/plain" );
    final Binary bin = pTool.getSession().getValueFactory().createBinary( pOutput );
    output.setProperty( "jcr:data", bin );
    output.setProperty( "jcr:lastModified", Calendar.getInstance() );
    return output;
  }
  
  /** addOutput
   *
   * @param pOutput
   * @return
   * @throws RepositoryException
   */
  @Override
  public Node addOutput( InputStream pOutput ) throws RepositoryException {
    return addOutput( pOutput, "jecars:Output" );
  }
  
  /** addOutput
   *
   * @param pOutput
   * @param pOutputName
   * @return
   * @throws RepositoryException
   */
  @Override
  public Node addOutput( final InputStream pOutput, final String pOutputName ) throws RepositoryException {
    final Node n = getTool();
    if (!n.hasNode( pOutputName )) {
      n.addNode( pOutputName, "jecars:outputresource" );
    }
    final Node output = n.getNode( pOutputName );
    final boolean isLink;
    if (output.hasProperty( "jecars:IsLink" )) {
      isLink = output.getProperty( "jecars:IsLink" ).getBoolean();
    } else {
      isLink = false;
    }
    output.setProperty( "jcr:mimeType", "text/plain" );
    if (isLink || (pOutput==null)) {
      final ByteArrayInputStream bais = new ByteArrayInputStream( "".getBytes() );
      final Binary bin = output.getSession().getValueFactory().createBinary( bais );
      output.setProperty( "jcr:data", bin );
      output.setProperty( "jecars:Partial", true );
    } else {
      final Binary bin = output.getSession().getValueFactory().createBinary( pOutput );
      output.setProperty( "jcr:data", bin );
      output.setProperty( "jecars:Partial", false );
    }
    final Calendar c = Calendar.getInstance();
    output.setProperty( "jcr:lastModified", c );
    n.save();
    return output;
  }

  /** addOutputTransient
   *
   * @param pOutput
   * @param pOutputName
   * @return
   * @throws RepositoryException
   */
  protected Node addOutputTransient( final Node pTool, final InputStream pOutput, final String pOutputName ) throws RepositoryException {
//    final Node n = getTool();
    final Node n = pTool;
    final Node output;
    final boolean isLink, hasNode;
    if (hasNode=n.hasNode( pOutputName )) {
      output = n.getNode( pOutputName );
      if (output.hasProperty( "jecars:IsLink" )) {
        isLink = output.getProperty( "jecars:IsLink" ).getBoolean();
      } else {
        isLink = false;
      }
    } else {
      output = n.addNode( pOutputName, "jecars:outputresource" );
      isLink = false;
      output.setProperty( "jcr:mimeType", "text/plain" );
    }
    if (isLink || (pOutput==null)) {
      if (!hasNode) {
        final ByteArrayInputStream bais = new ByteArrayInputStream( "".getBytes() );
        final Binary bin = output.getSession().getValueFactory().createBinary( bais );
        output.setProperty( "jcr:data", bin );
      }
      output.setProperty( "jecars:Partial", true );
    } else {
      final Binary bin = output.getSession().getValueFactory().createBinary( pOutput );
      output.setProperty( "jcr:data", bin );
      output.setProperty( "jecars:Partial", false );
    }
    final Calendar c = Calendar.getInstance();
    output.setProperty( "jcr:lastModified", c );
    return output;
  }
  
  /** getFuture
   * 
   * @return
   */
  @Override
  public Future getFuture() {
    if (mFuture==null) {
      return mScheduledFuture;
    } else {
      return mFuture;
    }
  }

  /** Get an iterator with inputs nodes (jecars:Input.*)
   * Replaced by getMixInputs
   * @return the parameters
   * @throws Exception when an error occurs
   */
  @Deprecated
  @Override
  public List<Node> getInputs()     throws Exception {
    final List<Node> nodes = new ArrayList<Node>();
    final NodeIterator ni = getTool().getNodes();
    Node n;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      if (n.isNodeType( "jecars:inputresource" )) {
        nodes.add( n );
      }
    }
    return nodes;
//    return getTool().getNodes( "jecars:Parameter*" );
  }

  /** getMixInputs
   * 
   * @return 
   */
  @Override
  public List<Node> getMixInputs() throws RepositoryException {
    final List<Node> nodes = new ArrayList<Node>();
    final NodeIterator ni = getTool().getNodes();
    Node n;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      if (n.isNodeType( "jecars:mix_inputresource" )) {
        nodes.add( n );
      }
    }
    return nodes;
  }


  
  /** getInputResources
   * 
   * @param pTool
   * @return
   * @throws RepositoryException
   */
  public List<Node> getInputResources( final Node pTool ) throws RepositoryException {
    final List<Node> nodes = new ArrayList<Node>();
    final NodeIterator ni = pTool.getNodes();
    Node n;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      if (n.isNodeType( "jecars:inputresource" )) {
        nodes.add( n );
      }
    }
    return nodes;
  }


  /** Get the input as objects in the collection as pObjectClass type
   * @param pObjectClass the class type of the resulting inputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  @Override
  public Collection<?> getInputsAsObject( final Class pObjectClass, final JD_Taglist pParamsTL ) throws Exception {
    return getAsObject( "jecars:Input", pObjectClass, pParamsTL );
  }

  /** clearOutputs
   * @throws java.lang.Exception
   */
  protected void clearOutputs() throws Exception {
    Collection<Node> ni = getOutputs();
    for (Node node : ni) {
      node.getParent().setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
      node.remove();
    }
    return;
  }
  
  /** Get an iterator with outputs nodes (jecars:Output.*)
   * @return the parameters
   * @throws Exception when an error occurs
   */
  @Override
  public List<Node> getOutputs()     throws Exception {
    ArrayList<Node> nodes = new ArrayList<Node>();
    NodeIterator ni = getTool().getNodes();
    Node n;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      if (n.isNodeType( "jecars:outputresource" )==true) {
        nodes.add( n );
      }
    }
    return nodes;
  }

  /** Get the output as objects in the collection as pObjectClass type
   * @param pObjectClass the class type of the resulting outputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  @Override
  public Collection<?> getOutputsAsObject( Class pObjectClass, JD_Taglist pParamsTL ) throws Exception {
    
    return null;
  }

  /** Get nodename as objects in the collection as pObjectClass type
   * @param pNodename e.g. jecars:Input or jecars:Output
   * @param pObjectClass the class type of the resulting outputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  @Override
  public Collection<?> getAsObject( final String pNodename, final Class pObjectClass, final JD_Taglist pParamsTL ) throws Exception {
    final Collection<Object> col = new ArrayList<Object>();
    final NodeIterator ni = getTool().getNodes( pNodename + "*" );
    Node n;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      if (n.isNodeType( "jecars:Tool" )) {
        final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( mMain, n, null, false );
        col.addAll( ti.getOutputsAsObject( pObjectClass, pParamsTL ) );
      } else {
        
        final Object result = getMain().getContext().getResultObject( n );
        if (result!=null) {
          if (pObjectClass.equals( InputStream.class )) {
            if (result instanceof InputStream) {
              col.add( result );
            }
          } else if (pObjectClass.equals( Double.class )) {
            if (result instanceof InputStream) {
              col.add( Double.parseDouble( CARS_Utils.readAsString2( (InputStream)result ) ));
            }
          }
        }        
/*
        if (n.isNodeType( "jecars:urlresource")) {
          if (pObjectClass.equals( InputStream.class )) {
            // **** [jecars:urlresource] mixin
            // **** - jecars:URL            (String)
            // **** - jecars:QueryPart      (String)
            if (n.hasProperty( "jecars:URL" )) {
              LOG.info( getTool().getPath() + ": Reading URL = " + n.getProperty( "jecars:URL" ).getString() );
              final URL u = new URL( n.getProperty( "jecars:URL" ).getString() );
              col.add( u.openStream() );
            } else if (n.isNodeType( "nt:resource")) {
              // **** [nt:resource] > mix:referenceable
              // **** - jcr:encoding     (STRING)
              // **** - jcr:mimeType     (STRING) mandatory
              // **** - jcr:data         (BINARY) mandatory primary
              // **** - jcr:lastModified (DATE)   mandatory IGNORE
 //              if (pObjectClass.equals( InputStream.class )==true) {
              col.add( n.getProperty( "jcr:data" ).getStream() );
//              }
            }
          } else if (pObjectClass.equals( Double.class )) {
            // **** Expecting double (convertable)
            col.add( Double.parseDouble(n.getProperty( "jcr:data" ).getString()) );
          }
        } else if (n.isNodeType( "jecars:mix_datafilelink")) {
          if (pObjectClass.equals( InputStream.class )) {
            // **** A link to a jecars:datafile node
            if (n.hasProperty( "jecars:PathToDatafile" )) {
              final String path = n.getProperty( "jecars:PathToDatafile" ).getValue().getString();
              col.add( getTool().getSession().getNode( path ).getProperty( "jcr:data" ).getBinary().getStream() );
            }
          }
        } else if (n.isNodeType( "jecars:mix_link")) {
          final List<Node> recursioncheck = new ArrayList<Node>();
          Node runnode = n;
          while( runnode.hasProperty( "jecars:Link" ) ) {
            recursioncheck.add( runnode );
            runnode = runnode.getProperty( "jecars:Link" ).getNode();
            if ((recursioncheck.size()>100) || recursioncheck.contains( runnode )) {
              throw new IllegalArgumentException( "Recursion in input node detected (jecars:Link)" );
            }
          }
          col.add( n.getProperty( "jcr:data" ).getBinary().getStream() );
        }
        * 
        */
      }
    }
    return col;
  }
  
  
  /** Add a instance listener
   * @param pListener A new listener
   */
  @Override
  public void addInstanceListener( CARS_ToolInstanceListener pListener ) {
    if (mListeners==null) {
      mListeners = new ArrayList<CARS_ToolInstanceListener>();
    }
    mListeners.add( pListener );
    return;
  }
  
  /** Return an iterator containing the Listeners
   * @return The LPF_InstanceListener iterator
   */
  protected Collection<CARS_ToolInstanceListener> getInstanceListeners() {
    return mListeners;
  }

  /** _setEventProperties
   *
   * @param pEvent
   * @param pNode
   * @throws javax.jcr.RepositoryException
   * @throws java.io.IOException
   */
  private void _setEventProperties( CARS_ToolInstanceEvent pEvent, Node pNode ) throws RepositoryException, IOException {
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

  /** Report this event to the listeners
   * @param pEvent The to be reported event
   */
  synchronized protected void reportToInstanceListeners( CARS_ToolInstanceEvent pEvent ) {
    try {
      // **** Check for storing events
      final CARS_EventManager em = CARS_Factory.getEventManager();
      if (storeEvents() && (mToolNode.hasNode( "jecars:Events" ))) {
        /*
          final static public int EVENTTYPE_UNKNOWN               = 0x00;
          final static public int EVENTTYPE_STATECHANGED          = 0x01;
          final static public int EVENTTYPE_GENERALEXCEPTION      = 0x02;
          final static public int EVENTTYPE_TOOLINSTANCEEXCEPTION = 0x03;
          final static public int EVENTTYPE_TOOLOUTPUTREPORT      = 0x04;
          final static public int EVENTTYPE_TOOLMESSAGE           = 0x05;
          final static public int EVENTTYPE_STATUSMESSAGE         = 0x06;
          final static public int EVENTTYPE_PROGRESS              = 0x07;
        */
        switch( pEvent.getEventType() ) {
          case CARS_ToolInstanceEvent.EVENTTYPE_UNKNOWN: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsUNKNOWN" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "UNKNOWN", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_STATECHANGED: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsSTATE" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "STATE", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_GENERALEXCEPTION: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:Events" + pEvent.getEventLevel().toString().toUpperCase() );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL",
                            pEvent.getEventLevel().toString().toUpperCase(), null, "jecars:ToolEventException" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_TOOLINSTANCEEXCEPTION: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsINSTANCE" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "INSTANCE", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_TOOLOUTPUTREPORT: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsOUTPUT" );
//            Node ef = em.addEvent( getMain(), null, getTool(), events.getPath(), "TOOL", "OUTPUT", null, "jecars:ToolEvent" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "OUTPUT", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_TOOLMESSAGE: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsMESSAGE" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "MESSAGE", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_STATUSMESSAGE: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsSTATUS" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "STATUS", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
          case CARS_ToolInstanceEvent.EVENTTYPE_PROGRESS: {
            final Node events = mToolNode.getNode( "jecars:Events/jecars:EventsPROGRESS" );
            final Node ef = em.createEventNode( getMain(), getTool(), null, getTool(), events.getPath(), "TOOL", "PROGRESS", null, "jecars:ToolEvent" );
            _setEventProperties( pEvent, ef );
            break;
          }
        }
      }
    } catch( Exception e ) {
      LOG.log( Level.SEVERE, "", e );
    } finally {
      try {
        getTool().save();
      } catch (Exception ex) {
        LOG.log( Level.SEVERE, "", ex );
      }
    }

    final Collection<CARS_ToolInstanceListener> til = getInstanceListeners();
    if (til!=null) {
      for (CARS_ToolInstanceListener ti : til) {
        if (pEvent.getEventType()==pEvent.EVENTTYPE_STATECHANGED) {
          ti.stateChanged( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_GENERALEXCEPTION) {
          ti.generalException( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_TOOLINSTANCEEXCEPTION) {
          ti.toolInstanceException( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_TOOLOUTPUTREPORT) {
          ti.reports( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_TOOLMESSAGE) {
          ti.reportMessage( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_STATUSMESSAGE) {
          ti.reportMessage( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_PROGRESS) {
          ti.reports( pEvent );
        }
        if (pEvent.getEventType()==pEvent.EVENTTYPE_UNKNOWN) {
          ti.reports( pEvent );
        }
      }
    }
    return;
  }
  
  /** Remove a instance listener, if this listener isn't added the method returns quiet.
   * @param pListener A listener to be removed
   */
  @Override
  public void removeInstanceListener( final CARS_ToolInstanceListener pListener ) {
    if (mListeners!=null) {
      while( mListeners.remove( pListener ) ) {        
      }
    }
    return;
  } 

  /** Report progress
   *
   * @param pProgress [0.0-1.0]
   */
  @Override
  public void reportProgress( final double pProgress ) throws Exception {
    getTool().setProperty( "jecars:PercCompleted", 100.0*pProgress );
    getTool().setProperty( "jecars:Modified", Calendar.getInstance() );
    getTool().save();
    reportToInstanceListeners(
            CARS_DefaultToolInstanceEvent.createEvent( this, CARS_ToolInstanceEvent.EVENTTYPE_PROGRESS, pProgress) );
    return;
  }

  /** Report output text
   * @param pOutput Standard output text string, when null the outputs will be flushed
   */
  @Override
  public void reportOutput( final String pOutput ) {
    if (pOutput!=null) {
      if (mSendOutputsLinesAsBatch<=1) {
        reportToInstanceListeners(
           CARS_DefaultToolInstanceEvent.createEvent( this, CARS_ToolInstanceEvent.EVENTTYPE_TOOLOUTPUTREPORT, pOutput ));
      } else {
        mCachedOutputLines.append( pOutput );
        mNoOfCachedOutputLines++;
        if (mNoOfCachedOutputLines>=mSendOutputsLinesAsBatch) {
          mNoOfCachedOutputLines = 0;
          reportToInstanceListeners(
             CARS_DefaultToolInstanceEvent.createEvent( this, CARS_ToolInstanceEvent.EVENTTYPE_TOOLOUTPUTREPORT, mCachedOutputLines.toString() ));
          mCachedOutputLines = new StringBuilder();
        }
      }
    } else {
      // **** Flush
      if (mSendOutputsLinesAsBatch>1) {
        if (mNoOfCachedOutputLines>0) {
          mNoOfCachedOutputLines = 0;
          reportToInstanceListeners(
             CARS_DefaultToolInstanceEvent.createEvent( this, CARS_ToolInstanceEvent.EVENTTYPE_TOOLOUTPUTREPORT, mCachedOutputLines.toString() ));
          mCachedOutputLines = new StringBuilder();
        }
      }
    }
    return;
  }

  
  /** Listen to instance state changes
   * @param pEvent holds the event which caused the call
   */
  @Override
  public void stateChanged( final CARS_ToolInstanceEvent pEvent ) {
    return;
  }
  
 /** Listen to tool instance exceptions, tool instance exceptions are exception which
   * are generated by external tools.
   * @param pEvent holds the event which caused the call
   */
  @Override
  public void toolInstanceException( final CARS_ToolInstanceEvent pEvent ) {
    return;
  }

  
  /** Report a message to the user.
   * @param pLevel the "level" of the message;
   *        Level.FINEST
   *        Level.FINER
   *        Level.FINE
   *        Level.CONFIG
   *        Level.INFO
   *        Level.WARNING
   *        Level.SERVERE
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   *  @param pBlocking when true the program will continue after the user closes down the requester.
   *                   when false the program continues with waiting for the user feedback
   *                   (NOTE) currently only the true option is supported
   */
  @Override
  public void reportMessage( java.util.logging.Level pLevel, String pHtmlMessage, boolean pBlocking ) {
    reportToInstanceListeners( 
       CARS_DefaultToolInstanceEvent.createEventMessage( this, pLevel, pHtmlMessage, pBlocking ));
    return;
  }

  /** Report a message to the user.
   * @param pLevel the "level" of the message;
   *        Level.FINEST
   *        Level.FINER
   *        Level.FINE
   *        Level.CONFIG
   *        Level.INFO
   *        Level.WARNING
   *        Level.SERVERE
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   *  @param pBlocking when true the program will continue after the user closes down the requester.
   *                   when false the program continue
   *                   (NOTE) currently only the true option is supported
   *   @return the event
   */
  @Override
  public CARS_ToolInstanceEvent reportMessageEvent( java.util.logging.Level pLevel, String pHtmlMessage, boolean pBlocking ) {
    final CARS_ToolInstanceEvent event = CARS_DefaultToolInstanceEvent.createEventMessage( this, pLevel, pHtmlMessage, pBlocking );
    reportToInstanceListeners( event );
    return event;
  }

  /** reportException
   * use reportException( pThrow, pLevel )
   * @param pThrow
   */
  @Deprecated
  protected void reportException( Throwable pThrow ) {
    reportException( pThrow, Level.SEVERE );
  }

  /** reportException
   *
   * @param pThrow
   * @param pLevel
   */
  @Override
  public void reportException( final Throwable pThrow, final Level pLevel ) {
    final CARS_ToolInstanceEvent tie = CARS_DefaultToolInstanceEvent.createEvent( this, pThrow, pLevel );
    reportToInstanceListeners( tie );
    return;
  }

  /** reportExceptionEvent
   * 
   * @param pThrow
   * @param pLevel
   * @return 
   */
  @Override
  public CARS_ToolInstanceEvent reportExceptionEvent( final Throwable pThrow, final Level pLevel ) {
    final CARS_ToolInstanceEvent tie = CARS_DefaultToolInstanceEvent.createEvent( this, pThrow, pLevel );
    reportToInstanceListeners( tie );
    return tie;
  }
  
/** Listen to instance exceptions
   * @param pEvent holds the event which caused the call
   */
  @Override
  public void generalException( final CARS_ToolInstanceEvent pEvent ) {
    return;
  }

  /** Listen to instance reports
   * @param pEvent holds the event which caused the call
   */
  @Override
  public void reports( final CARS_ToolInstanceEvent pEvent ) {
    return;
  }
  
  /** Listen to message reports
   * @param pEvent holds the event which caused the call
   */
  @Override
  public void reportMessage( final CARS_ToolInstanceEvent pEvent ) {
    return;
  }
  

  /** Report a status to the user.
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   */
  @Override
  public void reportStatusMessage( final String pHtmlMessage ) {
    reportToInstanceListeners( 
       CARS_DefaultToolInstanceEvent.createEvent( this, CARS_ToolInstanceEvent.EVENTTYPE_STATUSMESSAGE, pHtmlMessage ));
    return;
  }

  
  /** Add an input node object
   * @param pNode
   * @throws java.lang.Exception
   */
  @Override
  public void addInput( Node pNode ) throws Exception {
    return;
  }
  
  /** getTranslatedString
   * Translate a string, is the string starts with "locale:" (without the "") (use CARS_ToolInterface.LOCALE)
   * the remaining part of the string will be used as key to be resolved against the
   * language resourcebundles
   * 
   * @param pString the to be translated string
   * @return resulting string
   */
  @Override
  public String getTranslatedString( String pString ) {
    if (pString!=null) {
      if (pString.startsWith( LOCALE )) {
        pString = getString( pString.substring( 7 ) );
      }
    }
    return pString;
  }
  
  /** getResolvedToolProperty
   * 
   * @param pTool
   * @param pPropName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  public Property getResolvedToolProperty( final Node pTool, final String pPropName ) throws RepositoryException {
    if (pTool.hasProperty( pPropName )) {
      return pTool.getProperty( pPropName );
    }
    if (pTool.hasProperty( "jecars:ToolTemplate" )) {
      final String path = pTool.getProperty( "jecars:ToolTemplate" ).getString();
      final Node pn = pTool.getSession().getNode( path );
      return getResolvedToolProperty( pn, pPropName );
    }
    return null;
  }

  /** getResolvedToolNode
   *
   * @param pTool
   * @param pChildNodeName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  public Node getResolvedToolNode( final Node pTool, final String pChildNodeName ) throws RepositoryException {
    if (pTool.hasNode( pChildNodeName )) {
      return pTool.getNode( pChildNodeName );
    }
    if (pTool.hasProperty( "jecars:ToolTemplate" )) {
      final String path = pTool.getProperty( "jecars:ToolTemplate" ).getString();
      final Node pn = pTool.getSession().getRootNode().getNode(path.substring(1) );
      return getResolvedToolNode( pn, pChildNodeName );
    }
    return null;
  }

  /** getResolvedToolNode
   *
   * @param pTool
   * @param pChildNodeName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  public List<Node> getAllToolNodes( final Node pTool ) throws RepositoryException {
    return _getAllToolNodes( new ArrayList<Node>(), pTool );
  }

  /** _getAllToolNodes
   *
   * @param pNodes
   * @param pTool
   * @return
   * @throws javax.jcr.RepositoryException
   */
  private List<Node> _getAllToolNodes( List<Node> pNodes, final Node pTool ) throws RepositoryException {
    if (pTool.hasProperty( "jecars:ToolTemplate" )==true) {
      final String path = pTool.getProperty( "jecars:ToolTemplate" ).getString();
      Node pn = pTool.getSession().getRootNode().getNode(path.substring(1) );
      _getAllToolNodes( pNodes, pn );
    }
    pNodes.add( pTool );
    return pNodes;
  }


  /** getResolvedToolNodes
   *
   * @param pTool
   * @param pChildNodeName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  public List<Node> getResolvedToolNodes( Node pTool, final String pChildNodeName ) throws RepositoryException {
    return _getResolvedToolNodes( new ArrayList<Node>(), pTool, pChildNodeName );
  }

  /** _getResolvedToolNodes
   *
   * @param pNodes
   * @param pTool
   * @param pChildNodeName
   * @return
   * @throws javax.jcr.RepositoryException
   */
  private List<Node> _getResolvedToolNodes( final List<Node>pNodes, final Node pTool, final String pChildNodeName ) throws RepositoryException {
    if (pTool.hasNode( pChildNodeName )==true) {
      pNodes.add( pTool.getNode( pChildNodeName ));
    }
    if (pTool.hasProperty( "jecars:ToolTemplate" )==true) {
      final String path = pTool.getProperty( "jecars:ToolTemplate" ).getString();
      final Node pn = pTool.getSession().getRootNode().getNode(path.substring(1) );
      return _getResolvedToolNodes( pNodes, pn, pChildNodeName );
    }
    return pNodes;
  }


  /** sendOutputsAsBatch
   *
   * @return
   */
  @Override
  public void sendOutputsAsBatch( final int pNoLines ) {
    mSendOutputsLinesAsBatch = pNoLines;
    return;
  }

  /** storeEvents
   *
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public boolean storeEvents() throws Exception {
    return mStoreEvents;
  }

  /** replaceEvents
   *
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public boolean replaceEvents() throws Exception {
    return mReplaceEvents;
  }


  /** isScheduledTool
   *
   * @return true is the tool must be call every x {time}
   */
  @Override
  public boolean isScheduledTool() throws Exception {
    return mIsScheduled;
  }

  /** isSingleTool
   *
   * @return
   */
  @Override
  public boolean isSingleTool() throws Exception {
    return mIsSingle;
  }


  /** getDelayInSecs
   *
   * @return
   */
  @Override
  public long getDelayInSecs() throws Exception {
    return mDelayInSecs;
  }


  /** getDefaultInstancePath
   *
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public String getDefaultInstancePath() throws Exception {
    final String ip;
    if (mToolNode.hasProperty( "jecars:DefaultInstancePath" )) {
      final Property p = mToolNode.getProperty( "jecars:DefaultInstancePath" );
      if (p!=null) {
        ip = p.getString();
      } else {
        ip = null;
      }
    } else {
      ip = null;
    }
    return ip;
  }

  /** getAutoRunWhenInput
   *
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public String getAutoRunWhenInput() throws Exception {
    final Property p = getResolvedToolProperty( mToolNode, "jecars:AutoRunWhenInput" );
    if (p!=null) return p.getString();
    return null;
  }


  /** createToolSession
   *
   * @return
   */
  @Override
  public Session createToolSession() throws RepositoryException {
    if (getUsername()==null) {
      // *** TODO Elderberry
      return CARS_Factory.getLastFactory().getSessionInterface().cloneSession( getTool().getSession() );
//      return (SessionImpl)((SessionImpl)getTool().getSession()).createSession( getTool().getSession().getWorkspace().getName() );
    } else {
      return getTool().getSession().getRepository().login( new CARS_Credentials( getUsername(), getPassword(), null ));
    }
  }


  /** Get language string from the tool resource bundles
   */
  static protected String getString( String pString ) {
    String s = null;
    if (gToolResourceBundles.isEmpty()) {
      // **** Retrieve the resource bundle, if empty
      for( String rs : gToolResourceBundleNames ) {
        ResourceBundle rb = null;
        try {
          rb = ResourceBundle.getBundle( rs, gToolLocale );
        } catch (Exception rbe) {
          LOG.log( Level.SEVERE, "Missing resource: " + rs + " locale:" + gToolLocale, rbe );
        }
        LOG.config( "Load bundle: " + rs + " locale:" + gToolLocale );
        gToolResourceBundles.add( rb );
      }    
    }
    // **** Get the localized string
    for( ResourceBundle rb : gToolResourceBundles ) {
      try {
        s = rb.getString( pString );
      } catch ( MissingResourceException mse ) {
      }
    }
    if (s==null) LOG.log( Level.WARNING, "Missing resource key: " + pString );
    return s;
  }


  /** signal
   * 
   * @param pSignal
   */
  @Override
  public void signal( final String pToolPath, final CARS_ToolSignal pSignal ) {
    return;
  }

  /** toString
   * 
   */
  @Override
  public String toString() {
    try {
      return mToolNode.getPath();
    } catch(RepositoryException re) {
      return super.toString();
    }
  }
  
  
}
