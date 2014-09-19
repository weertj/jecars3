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
package org.jecars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.logging.*;
import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.jaas.CARS_PasswordService;
import org.jecars.apps.CARS_AccountsApp;
import org.jecars.apps.CARS_AdminApp;
import org.jecars.apps.CARS_Interface;
import org.jecars.jaas.CARS_Credentials;

/**
 * CARS_Factory
 *
 * @version $Id: CARS_Factory.java,v 1.37 2009/06/21 20:58:48 weertj Exp $
 */
abstract public class CARS_Factory {
  
  static final public int REPOSITORYTYPE_TRANSIENT = 1;

//  static private HashMap<String, Session> gSessionPool = new HashMap<String, Session>();
  static final public String JECARSPROP_ETAG = "PROP_ETAG";
  
  static final public String     JECARSPROPERTIESNAME = "jecars.properties";
  // **** If not null then gJecarsPropertiesPath overrules the properties location
  static       public String     gJecarsPropertiesPath = null;
  static final public Properties gJecarsProperties = new Properties();
  static public String        gConfigFile       = "";
  static public String        gRepHome          = "";
  static public String        gRepLogHome       = "";
  static public String        gRepNamespaces    = "";
  static public String        gRepCNDFiles      = "";
  static public Level         gLogLevel   = Level.FINE;
  static private CARS_Factory gLastFactory = null;

  static final protected Logger gLog = Logger.getLogger( "org.jecars" );
  
  static public    ICARS_AccessManager  gAccessManager            = null;
  static protected CARS_Credentials     gSysCreds                 = null;
  static protected Repository           gRepository               = null;
  static protected Session              gSystemCarsSession        = null;
  static protected Session              gSystemAccessSession      = null;
  static protected Session              gSystemLoginSession       = null;
  static protected Session              gSystemApplicationSession = null;
  static protected Session              gSystemToolSession        = null;
  static protected Session              gObservationSession       = null;
  @Deprecated
  static protected CARS_EventManager    gEventManager             = null;
  static protected ICARS_EventService   gEventService             = null;
  static private   Object               gServletContext           = null;
  static final private Calendar         gStartTime                = Calendar.getInstance();
  static private   boolean              gEnableFET                = true;
  static private   boolean              gEnableFETLogging         = false;
  
  static final public List<MultiJeCARS> gMultiJeCARS = new ArrayList<>();
  
  static public class MultiJeCARS {
    public String mServer = "";
    public String mJeCARSURL = "";
    public int    mNumberOfCores = 1;
  }
  
  /** Creates a new instance of CARS_Factory
   */
  @SuppressWarnings("LeakingThisInConstructor")
  public CARS_Factory() {
    gLastFactory   = this;
    return;
  }

  /** addMultiJeCARS
   * 
   * @param pServer
   * @param pJeCARSURL
   * @param pNumberOfCores
   */
  static public void addMultiJeCARS( final String pServer, final String pJeCARSURL, final int pNumberOfCores ) {
    MultiJeCARS mj = new MultiJeCARS();
    mj.mServer = pServer;
    mj.mJeCARSURL = pJeCARSURL;
    mj.mNumberOfCores = pNumberOfCores;
    gMultiJeCARS.add( mj );
    return;
  }
  
  /** setJecarsPropertiesPath
   * 
   * @param pPath
   */
  static public void setJecarsPropertiesPath( final String pPath ) {
    gJecarsPropertiesPath = pPath;
    return;
  }
  
  /** getJecarsProperties
   * 
   * @return 
   */
  static public Properties getJecarsProperties() {
    return gJecarsProperties;
  }

  /** setEnableFET
   * 
   * @param pEnable
   */
  static public void setEnableFET( final boolean pEnable ) {
    gEnableFET = pEnable;
    return;
  }

  /** getJeCARSStartTime
   * 
   * @return
   */
  static public Calendar getJeCARSStartTime() {
    return gStartTime;
  }

  /** setServletContext
   * 
   * @param pSC
   */
  static public void setServletContext( final Object pSC ) {
    gServletContext = pSC;
    return;
  }

  /** getServletContext
   *
   * @return
   */
  static public Object getServletContext() {
    return gServletContext;
  }

  /** getSessionPool
   * 
   * @return
   */
//  static HashMap<String, Session>getSessionPool() {
//    return gSessionPool;
//  }
 
  /** isInSessionPool
   * 
   * @param pSession
   * @return
   */
//  static boolean isInSessionPool( Session pSession ) {
//    return (gSessionPool.containsValue( pSession ));
//  }
  
  /** Get the last created factory
   */
  static public CARS_Factory getLastFactory() {
    return gLastFactory;
  }

  /** initJeCARSProperties
   *
   */
  static protected void initJeCARSProperties() {
    try {
        gJecarsProperties.put( JECARSPROP_ETAG, "false" );
        
//      if (gJecarsProperties.isEmpty()) {
        if (gJecarsPropertiesPath==null) {
          gLog.log( Level.INFO, "Trying to read jecars properties as system resource " + JECARSPROPERTIESNAME );
          final InputStream sis = ClassLoader.getSystemResourceAsStream( "/" + JECARSPROPERTIESNAME );
          if (sis!=null) {
            gJecarsProperties.load( sis );
            sis.close();
          } else {
            // **** Read jecars property file
            final File f = new File( JECARSPROPERTIESNAME );
            gLog.log( Level.INFO, "Trying to read file: " + f.getCanonicalPath() );
            if (f.exists()) {
              final FileInputStream fis = new FileInputStream(f);
              try {
                gJecarsProperties.load( fis );
              } finally {
                fis.close();
              }
            } else {
              gLog.log( Level.SEVERE, "Cannot find " + f.getCanonicalPath()  );
            }
          }
        } else {
          gLog.log( Level.INFO, "Trying to read jecars properties from path " + gJecarsPropertiesPath );
          final FileInputStream jfis = new FileInputStream( gJecarsPropertiesPath );
          try {
            gJecarsProperties.load( jfis );
          } finally {
            jfis.close();
          }
        }
//      }
      gLog.log( Level.INFO, "Config file = " + gJecarsProperties.getProperty( "jecars.ConfigFile", "<null>" ) );
      gConfigFile    = gJecarsProperties.getProperty( "jecars.ConfigFile", "<null>" );
      gRepHome       = gJecarsProperties.getProperty( "jecars.RepHome",    "<null>" );
      gLog.log( Level.INFO, "Repository home = " + gRepHome );
      gRepLogHome    = gJecarsProperties.getProperty( "jecars.RepLogHome", "<null>" );
      gLog.log( Level.INFO, "Log home = " + gRepLogHome );
      gRepNamespaces = gJecarsProperties.getProperty( "jecars.Namespaces", "jecars,http://jecars.org" );
      gLog.log( Level.INFO, "Namespaces = " + gRepNamespaces );
      gRepCNDFiles   = gJecarsProperties.getProperty( "jecars.CNDFiles",   "/org/jecars/jcr/nodetype/jecars.cnd,jecars.cnd" );
      gLog.log( Level.INFO, "CND files = " + gRepCNDFiles );
    } catch (IOException e) {
      gLog.log( Level.SEVERE, null, e );
    }      
  }

  /** getEventService
   * 
   * @return 
   */
  static public ICARS_EventService getEventService() {
    return gEventService;
  }
  
  @Deprecated
  static public CARS_EventManager getEventManager() {
    return gEventManager;
  }

  /** setRepository
   * 
   * @param pRep
   */
  static public void setRepository( Repository pRep ) {
    gRepository = pRep;
    return;
  }
  
  static public Repository getRepository() {
    return gRepository;
  }
  
  /** Add namespaces
   */
  protected void addNamespaces( NamespaceRegistry pNSR, String[] pNS ) throws RepositoryException {
    int i = 0;
    while( i<pNS.length ) {
      try {
        gLog.log( Level.INFO, "Add namespace: " + pNS[i] + " = " +  pNS[i+1] );
        CARS_ActionContext.addPublicNamespace( pNS[i] );
        pNSR.registerNamespace( pNS[i], pNS[i+1] );
      } catch (NamespaceException ne) {
        // **** Jackrabbit init ready
      }
      i += 2;
    }
    return;
  }
  
  /** Add nodetypes definitions
   * @param pSession
   * @param pCNDS
   * @throws java.io.IOException
   * @throws org.apache.jackrabbit.core.nodetype.compact.ParseException
   * @throws javax.jcr.RepositoryException
   */
  abstract protected void addNodeTypesDefinitions( Session pSession, String[] pCNDS ) throws IOException, RepositoryException;
  
  /** Init the JCR repository and register the nodetypes
   *
   * @param pCreds
   * @throws java.lang.Exception
   */
  abstract protected void initJCR( final CARS_Credentials pCreds ) throws Exception;

  /** Init the logging files, the directories
   */
  abstract public void initLogging() throws Exception;
  
  /** Init, the initial object framework will be created
   * 
   * @param pCreds
   * @param pReinit
   * @throws java.lang.Exception
   */
  abstract public void init( CARS_Credentials pCreds, final boolean pReinit ) throws Exception; 
  
  /** Init all Application sources (jecars:CARS_Interface)
   * @param pMain CARS_Main
   */
  public void initApplicationSources( final CARS_Main pMain ) throws RepositoryException {
    final String query = "SELECT * FROM jecars:interfaceclass WHERE jecars:InterfaceClass<>''";
    final Session ses = getSystemCarsSession();
    synchronized( ses ) {
      final Query q = ses.getWorkspace().getQueryManager().createQuery( query, Query.SQL );
      final QueryResult qr = q.execute();
      final NodeIterator ni = qr.getNodes();
      while( ni.hasNext() ) {
        final Node apps = ni.nextNode();
        try {
          final String clss = apps.getProperty( "jecars:InterfaceClass" ).getString();
          final CARS_Interface ic = (CARS_Interface)Class.forName( clss ).newInstance();
          ic.init( null, apps );
        } catch (Throwable e) {
          gLog.log( Level.SEVERE, "initApplicationSources", e );
        }
      }
    }
      
    return;
  }
  
  /** closeAllSessions
   * 
   */
  static public void closeAllSessions() {
//    getEventManager().sessionLogout();
    
    Session ses = gSystemCarsSession;
    if (ses!=null) {
      synchronized( ses ) {
        gSystemCarsSession = null;
        ses.logout();
      }
    }
    try {
      gSystemLoginSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemAccessSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemApplicationSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemToolSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    
    return;
  }

  /** shutdown
   *
   */
  @SuppressWarnings("empty-statement")
  static public void shutdown() {
    System.out.println( "CARS_Factory: shutdown" );
    gLastFactory = null;
    if (gObservationSession!=null) {
      try {
        gObservationSession.save();
        gObservationSession.refresh( false );
      } catch( RepositoryException re ) {};
      gObservationSession.logout();
      gObservationSession = null;
    }
    if (gSystemAccessSession!=null) {
      try {
        gSystemAccessSession.save();
        gSystemAccessSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemAccessSession.logout();
      gSystemAccessSession = null;
    }
    if (gSystemApplicationSession!=null) {
      try {
        gSystemApplicationSession.save();
        gSystemApplicationSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemApplicationSession.logout();
      gSystemApplicationSession = null;
    }
    if (gSystemLoginSession!=null) {
      try {
        gSystemLoginSession.save();
        gSystemLoginSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemLoginSession.logout();
      gSystemLoginSession = null;
    }
    if (gSystemToolSession!=null) {
      try {
        gSystemToolSession.save();
        gSystemToolSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemToolSession.logout();
      gSystemToolSession = null;
    }
    if (gSystemCarsSession!=null) {
      try {
        gSystemCarsSession.save();
        gSystemCarsSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemCarsSession.logout();
      gSystemCarsSession = null;
//      gRepository.shutdown();
      return;
    }
    return;
  }

  /** getSystemCarsSession
   * 
   * @return
   */
  static protected Session getSystemCarsSession() {
    if (gSystemCarsSession==null) {
      try {
        gSystemCarsSession = gRepository.login( new CARS_Credentials( CARS_Definitions.gUSERNAME_GRANTALL, "".toCharArray(), null ));
        gLog.info( "System jecars session login: " + CARS_Definitions.gUSERNAME_GRANTALL );
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
    return gSystemCarsSession;
  }

  /** getSystemAccessSession
   * 
   * @return
   */
  static public Session getSystemAccessSession() {
    return gSystemAccessSession;
  }

  /** getSystemLoginSession
   *
   * @return
   */
  static public Session getSystemLoginSession() {
    return gSystemLoginSession;
  }

  /** getSystemApplicationSession
   *
   * @return
   */
  static public Session getSystemApplicationSession() {
    return gSystemApplicationSession;
  }

  /** getObservationSession
   *
   * @return
   */
  static public Session getObservationSession() {
    return gObservationSession;
  }
   
  /** getSystemToolsSession
   */
  static public Session getSystemToolsSession() throws LoginException, RepositoryException {
    return gSystemToolSession;
  }
  
  /** createMain
   * @param pSession
   * @param pFactory
   * @return
   * @throws java.lang.Exception
   */
  static public CARS_Main createMain( final Session pSession, final CARS_Factory pFactory ) throws RepositoryException {
    return new CARS_DefaultMain( pSession, pFactory );
  }
  
  /** createMain
   * Create a CARS_Main interface
   * @param pCreds use these credentials
   * @param pWhat not used, use "default"
   * @return the CARS_Main interface
   * @throws AccessDeniedException when the actions isn't allowed
   */
  public CARS_Main createMain( final CARS_Credentials pCreds, final String pWhat ) throws AccessDeniedException {
    CARS_Main m;
    try {
      final Session ses = gRepository.login( pCreds );
      m = createMain( ses );
    } catch (Exception e) {
      throw new AccessDeniedException( e );
    }
    return m;
  }

  /** createMain
   * Create a CARS_Main interface
   * @param pCreds use these credentials
   * @param pWhat not used, use "default"
   * @return the CARS_Main interface
   * @throws AccessDeniedException when the actions isn't allowed
   */
  public CARS_Main createMain( final SimpleCredentials pCreds, final String pWhat ) throws AccessDeniedException {
    CARS_Main m;
    try {
      final Session ses = gRepository.login( pCreds );
      m = createMain( ses );
    } catch (Exception e) {
      throw new AccessDeniedException( e );
    }
    return m;
  }

  /** createMain
   * 
   * @param pMain
   * @param pContext
   * @return
   * @throws AccessDeniedException
   * @throws CredentialExpiredException 
   */
  public CARS_Main createMain( final CARS_ActionContext pContext ) throws AccessDeniedException, CredentialExpiredException {
    return createMain( null, pContext );
  }
  
  /** createMain
   * Create a CARS_Main interface using only the context
   * @param pContext the action context
   * @return the CARS_Main interface
   * @throws javax.jcr.AccessDeniedException when the actions isn't allowed
   * @throws javax.security.auth.login.AccountLockedException
   * @throws javax.security.auth.login.CredentialExpiredException
   */
  public CARS_Main createMain( final CARS_Main pMain, final CARS_ActionContext pContext ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main m;
    try {
      final Session ses;
      if (pContext.getAuthKey()==null) {
        final String userName = pContext.getUsername();
        if (userName==null) {
          if (pMain==null) {
            ses = null;
          } else {
            ses = pMain.getSession();
          }
        } else {
          if (CARS_Definitions.gSuperuserName.equals( userName )) {
            throw new AccessDeniedException();
          }
          final CARS_Credentials creds = new CARS_Credentials( userName, pContext.getPassword(), pContext );
          ses = gRepository.login( creds );
        }
      } else {
        final CARS_Credentials creds = new CARS_Credentials( CARS_AccountsApp.AUTHKEY_PREFIX + pContext.getAuthKey(), "".toCharArray(), pContext );
        ses = gRepository.login( creds );
      }
      m = createMain( ses );
      m.addContext( pContext );
      final String userId = m.getSession().getUserID();
      final Session loginSession = CARS_Factory.getSystemLoginSession();
      synchronized( loginSession ) {
        final Node users = loginSession.getNode( "/JeCARS/default/Users" );
        final Node user = users.getNode( userId );
        if ((user.hasProperty( "jecars:PasswordMustChange" )) && (user.getProperty( "jecars:PasswordMustChange" ).getBoolean())) {
          // **** Password must change
          if (user.hasProperty( "jecars:Source" )) {
              final Node source = loginSession.getNode( user.getProperty( "jecars:Source" ).getString().substring(1) );
              if (source.hasProperty( "jecars:ChangePasswordURL" )) {
                throw new CredentialExpiredException( source.getProperty( "jecars:ChangePasswordURL" ).getString() );
              }
          }
          throw new CredentialExpiredException();
        }
      }
//    } catch (AccountLockedException ale) {
//      throw ale;
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (Exception e) {
//    e.printStackTrace();
      throw new AccessDeniedException( e );
    }
    return m;
  }

  /** Create a CARS_Main interface
   *
   * @param pSession
   * @return
   * @throws RepositoryException
   */
  public CARS_Main createMain( final Session pSession ) throws RepositoryException {
    return new CARS_DefaultMain( pSession, this );
  }

  public ICARS_AccessManager getAccessManager() {
    return gAccessManager;
  }
  
  abstract public ICARS_Session getSessionInterface();
  
  /** _getFET
   * 
   * @param pContext
   * @return
   */
  private String _getFET( final CARS_ActionContext pContext ) {
    if (gEnableFET) {
      return pContext.getQueryValue( "FET=" );
    }
    return null;
  }

  /** Do HEAD
   *
   * @param pContext
   * @throws CredentialExpiredException
   * @throws AccessDeniedException
   */
  public void performHeadAction( final CARS_ActionContext pContext ) throws CredentialExpiredException, AccessDeniedException {
    CARS_Main main = null;
    try {
//   System.out.println("HEAD NODE " + pContext.getPathInfo() );
      String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionGET );
      main = createMain( pContext );
      pContext.setMain( main );
      if (gEnableFETLogging && ((fet==null) || (fet.indexOf( "READ" )==-1))) {
        if (pContext.getQueryString()==null) {
          gEventService.offer( new CARS_Event( main, null, null, "URL", "READ", null,
                "HEAD " + pContext.getPathInfo(), null ) );
//          gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "READ",
//                "HEAD " + pContext.getPathInfo() );
        } else {
          gEventService.offer( new CARS_Event( main, null, null, "URL", "READ", null,
                "HEAD " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString()), null ) );
//          gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "READ",
//                "HEAD " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString())  );
        }
      }
      Node cnode = main.getNode( pContext.getPathInfo(), null, true );
      pContext.setThisNode( cnode );
      pContext.prepareResult(); // **** This will cache the result so we can close the connection
//   System.out.println("HEAD NODE " + pContext.getPathInfo() + "----- READY" );
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (AccessDeniedException ade) {
      // TODO
//      gEventManager.addException( main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", ade, pContext.getPathInfo() );
//      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      gEventService.offer( new CARS_Event( main, null, "SYS", "LOGIN", pContext, null ) );
      throw ade;
    } catch (PathNotFoundException pnfe) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", pnfe, pContext.getPathInfo() );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
      gEventService.offer( new CARS_Event( main, null, "SYS", "READ", pContext, null ));
    } catch (Exception e) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", e, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      pContext.setError( e );
      gEventService.offer( new CARS_Event( main, null, "SYS", "READ", pContext, null ));
//    } finally {
    }
    return;
  }

  /** performGetAction
   * 
   * @param pContext
   * @throws CredentialExpiredException
   * @throws AccessDeniedException
   */
  public void performGetAction( final CARS_ActionContext pContext ) throws CredentialExpiredException, AccessDeniedException, CARS_LongPollRequestException {
    performGetAction( pContext, null );
    return;
  }
  
  /** Do GET
   * 
   * @param pContext
   * @param pMain
   * @throws CredentialExpiredException
   * @throws AccessDeniedException 
   */
  public void performGetAction( final CARS_ActionContext pContext, final CARS_Main pMain ) throws CredentialExpiredException, AccessDeniedException, CARS_LongPollRequestException {
    CARS_Main main = pMain;
    try {
//   System.out.println("GET NODE " + pContext.getPathInfo() );
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionGET );
      if (main==null) {
        main = createMain( pContext );
        pContext.setMain( main );
      }
      if (gEnableFETLogging && ((fet==null) || (fet.indexOf( "READ" )==-1))) {
        if (pContext.getQueryString()==null) {
          gEventService.offer( new CARS_Event( main, null, null, "URL", "READ", null, "GET " + pContext.getPathInfo(), null ) );
//          gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "READ",
//                "GET " + pContext.getPathInfo() );
        } else {
          gEventService.offer( new CARS_Event( main, null, null, "URL", "READ", null,
                "GET " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString()), null ) );
//          gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "READ",
//                "GET " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString())  );
        }
      }
      pContext.setCanBeCachedResult( pContext.getQueryString()==null );
      try {
        final Node cnode = main.getNode( pContext.getPathInfo(), null, false );
//        System.out.println("GET NODE " + pContext.getPathInfo() + " ===== READY ");
        pContext.setThisNode( cnode );
        pContext.setThisProperty( main.getCurrentViewProperty() );
      } catch( CARS_RESTMethodHandled cr ) {
      }
      pContext.prepareResult(); // **** This will cache the result so we can close the connection
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (AccessDeniedException ade) {
      // TODO
//      gEventManager.addException( main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", ade, pContext.getPathInfo() );
//      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      gEventService.offer( new CARS_Event( main, null, "SYS", "LOGIN", pContext, null ));
      throw ade;
    } catch (PathNotFoundException pnfe) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", pnfe, pContext.getPathInfo() );
      gEventService.offer( new CARS_Event( main, null, "SYS", "READ", pContext, null ));
    } catch( CARS_LongPollRequestException le ) {
      // **** The request is handled by a different thread
      throw le;
    } catch (Exception e) {
//      LOG.log( Level.INFO, null, e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      pContext.setError( e );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", e, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "READ", pContext, null ));
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }

  
  /** Do POST
   *
   * @param pContext
   * @throws AccessDeniedException
   * @throws CredentialExpiredException
   */
  public void performPostAction( final CARS_ActionContext pContext ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main main = null;
    try {
//   System.out.println("POST NODE " + pContext.getPathInfo() );
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionPOST );
      main = createMain( pContext );      
      pContext.setMain( main );
      final String pathinfo = pContext.getPathInfo();
      if (gEnableFETLogging && ((fet==null) || (fet.indexOf( "WRITE" )==-1))) {
        gEventService.offer( new CARS_Event( main, null, null, "URL", "WRITE", null, "POST " + pathinfo, null ) );
//        gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "WRITE", "POST " + pathinfo );
      }
      if (pathinfo.lastIndexOf( '/' )==-1) {
        throw new PathNotFoundException( pathinfo );
      } else {
        // **** Store the given parameters
        JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        paramsTL = pContext.getParameterMapAsTaglist( paramsTL );
//   System.out.println(" 2 POST NODE " );
        final Node cnode = main.addNode( pathinfo, paramsTL, pContext.getBodyStream(), pContext.getBodyContentType() );
        pContext.setCreatedNode( cnode );
//   System.out.println("POST NODE " + pContext.getPathInfo() + "----- READY" );
      }      
    } catch (CARS_CustomException cce) {
      pContext.setError( cce );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, cce, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
    } catch (ItemExistsException iee) {
      pContext.setError( iee );
      pContext.setErrorCode( 1300 );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, iee, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (AccessDeniedException ade) {
//      gEventManager.addException( main, null, null, null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_LOGIN, ade, pContext.getPathInfo() );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      gEventService.offer( new CARS_Event( main, null, "SYS", "LOGIN", pContext, null ));
      throw ade;
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, pnfe, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
    } catch (NoSuchNodeTypeException nsnte) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( nsnte );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, nsnte, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
    } catch (ConstraintViolationException cve) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_ACCEPTABLE );
      pContext.setError( cve );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, cve, null );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
    } catch (Exception e) {
//   e.printStackTrace();
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, e, null );
      pContext.setError( e );
      gEventService.offer( new CARS_Event( main, null, "SYS", "CREATE", pContext, null ));
//    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }
  
  
  /** Do PUT
   *
   * @param pContext
   * @param pMain
   * @throws Exception
   */
  public void performPutAction( final CARS_ActionContext pContext, final CARS_Main pMain ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main main = pMain;
    try {
//   System.out.println("PUT NODE " + pContext.getPathInfo() );
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionPUT );
      if (main==null) {
        main = createMain( pContext );
        pContext.setMain( main );
      }
      final String pathinfo = pContext.getPathInfo();
      if (gEnableFETLogging && ((fet==null) || (fet.indexOf( "WRITE" )==-1))) {
//        gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "WRITE", "PUT " + pathinfo );
        gEventService.offer( new CARS_Event( main, null, null, "URL", "WRITE", null, "PUT " + pathinfo, null ) );
      }
      if (pathinfo.lastIndexOf( '/' )!=-1) {
        // **** Store the given parameters
        final JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        final Node cnode = main.updateNode( pathinfo, paramsTL, pContext.getBodyStream(), pContext.getBodyContentType() );
        pContext.setThisNode( cnode );
//   System.out.println("PUT NODE " + pContext.getPathInfo() + "----- READY" );
      } else {
        throw new PathNotFoundException( pathinfo );
      }
    } catch (AccessDeniedException ade) {
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
//      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      gEventService.offer( new CARS_Event( main, null, "SYS", "LOGIN", pContext, null ));
      throw ade;
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );      
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", pnfe, null );
      pContext.setError( pnfe );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      gEventService.offer( new CARS_Event( main, null, "SYS", "UPDATE", pContext, null ));
    } catch (RepositoryException re) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", re, null );
      pContext.setError( re );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      gEventService.offer( new CARS_Event( main, null, "SYS", "UPDATE", pContext, null ));
    } catch (Exception e) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", e, null );
//      LOG.log( Level.INFO, null, e );
      pContext.setError( e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      gEventService.offer( new CARS_Event( main, null, "SYS", "UPDATE", pContext, null ));
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//     }
    }
    return;
  }
  
  /** Do DELETE
   * 
   * @param pContext
   * @throws java.lang.Exception
   */
  public void performDeleteAction( CARS_ActionContext pContext ) throws Exception {    
    CARS_Main main = null;
    try {
//   System.out.println("DELETE NODE " + pContext.getPathInfo() );
      pContext.setAction( CARS_ActionContext.gDefActionDELETE );
      main = createMain( pContext );      
      pContext.setMain( main );
      String pathinfo = pContext.getPathInfo();
      if (gEnableFETLogging) {
//        gEventManager.addEventThreaded( main, main.getLoginUser(), null, null, "URL", "DELETE", "DELETE " + pathinfo );
       gEventService.offer( new CARS_Event( main, null, null, "URL", "DELETE", null, "DELETE " + pathinfo, null ) );
     }
      if (pathinfo.lastIndexOf( '/' )!=-1) {
        // **** Store the given parameters
        JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        main.removeNode( pathinfo, paramsTL );
//   System.out.println("DELETE NODE " + pContext.getPathInfo() + "----- READY" );
      } else {
        throw new PathNotFoundException( pathinfo );
      }
    } catch (ReferentialIntegrityException rie) {
//      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", rie, null );
      pContext.setError( rie );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      gEventService.offer( new CARS_Event( main, null, "SYS", "DELETE", pContext, null ));
    } catch (AccessDeniedException ade) {        
//      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", ade, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      gEventService.offer( new CARS_Event( main, null, "SYS", "DELETE", pContext, null ));
      throw ade;
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );      
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
//      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", pnfe, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );      
      pContext.setError( pnfe );
      gEventService.offer( new CARS_Event( main, null, "SYS", "DELETE", pContext, null ));
    } catch (Exception e) {
//      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "DELETE", e, null );
//      LOG.log( Level.INFO, null, e );
      pContext.setError( e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      gEventService.offer( new CARS_Event( main, null, "SYS", "DELETE", pContext, null ));
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//          main.destroy();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }
  
  
  
}
