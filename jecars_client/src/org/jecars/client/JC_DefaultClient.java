/*
 * Copyright 2008-2011 NLR - National Aerospace Laboratory
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

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.logging.Logger;
import javax.jcr.observation.Event;
import nl.msd.jdots.JD_Taglist;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.jecars.client.observation.JC_DefaultEvent;
import org.jecars.client.observation.JC_DefaultObservationManager;
import org.jecars.client.observation.JC_Event;
import org.jecars.client.observation.JC_ObservationManager;

/**
 * JC_DefaultClient
 *
 * @version $Id: JC_DefaultClient.java,v 1.26 2009/06/22 22:37:48 weertj Exp $
 */
public class JC_DefaultClient implements JC_Clientable, Serializable {

  static final protected Logger gLog = Logger.getLogger( "org.jecars.client" );
  
  private final EnumSet<JC_ClientOption> mOptions = EnumSet.noneOf( JC_ClientOption.class );
          
  private JC_Nodeable  mRootNode             = null;
  private JC_GDataAuth mGDataAuth            = null;
  private String       mServerPath           = null;
//  private int          mLoginType            = JC_Clientable.LOGIN_TYPE_BASIC;
  private EnumSet<LOGINTYPE> mLoginTypes     = EnumSet.of( LOGINTYPE.BASIC );
  private int          mPostCreationMethod   = JC_Clientable.POST_USE_PATH;
  private boolean      mPOST_AS_GET          = false;
  private boolean      mPUT_AS_GET           = false;
  private boolean      mDELETE_AS_GET        = false;
  private boolean      mChunkedStreamingMode = false;
  private Throwable    mLastThrowable        = null;

  private final JC_RESTComm                     mRESTComm;
  private final AbstractMap<String, JC_Params>  mDefaultParams;
  private transient final JC_ObservationManager mObservationManager;

  /** mNodeTypeRegistry
   */
  private final Map<String,String>     mNodeTypeRegistry = new LinkedHashMap<String,String>();
  private final AbstractMap<String, String>  mNamespaces = new HashMap<String, String>();

  /** JC_DefaultClient
   * 
   * @throws JC_Exception
   */
  public JC_DefaultClient() throws JC_Exception {
    mObservationManager = new JC_DefaultObservationManager( this );
    mDefaultParams      = new HashMap<String, JC_Params>();
    mRESTComm           = new JC_RESTComm( this );
    return;
  }
  
  @Override
  public Throwable getLastThrowable() {
    return mLastThrowable;
  }
  
  @Override
  public void clearLastThrowable() {
    mLastThrowable = null;
    return;
  }

  /** getObservationManager
   *
   * @return
   */
  @Override
  public JC_ObservationManager getObservationManager() {
    return mObservationManager;
  }

  /** getOptions
   * 
   * @return 
   */
  @Override  
  public EnumSet<JC_ClientOption> getOptions() {
    return mOptions;
  }

  /** addNamespace
   * 
   * @param pId
   * @param pUrl
   */
  @Override
  public void addNamespace( String pId, String pUrl ) {
    mNamespaces.put( pId, pUrl );
    return;
  }

  /** getNamespaces
   * 
   * @return
   */
  @Override
  public AbstractMap<String, String>getNamespaces() {
    return mNamespaces;
  }
  
  /** setServerPath
   * @param pServerPath
   */
  protected void setServerPath( String pServerPath ) {
    if (pServerPath.endsWith( "/" )) {
      mServerPath = pServerPath.substring( 0, pServerPath.length()-1 );
    } else {
      mServerPath = pServerPath;
    }
    return;
  }
  
  /** getServerPath
   * @return
   */
  @Override
  public String getServerPath() {
    return mServerPath;
  }
  
  /** setPostCreationMethod\
   * 
   * @param pMethod
   *          POST_USE_PATH = The URL path in the POST message is the object path to be created
   *          POST_USE_SLUG = The Slug http request parameter is the object name to be created
   */
  @Override
  public void setPostCreationMethod( int pMethod ) {
    mPostCreationMethod = pMethod;
  }

  /** getPostCreationMethod
   * 
   * @return (POST_USE_PATH|POST_USE_SLUG)
   */
  @Override
  public int getPostCreationMethod() {
    return mPostCreationMethod;
  }
  
 /** setHttpOperation
   * 
   * @param pSet true then set (otheriwse reset)
   * @param pValue (POST_AS_GET|PUT_AS_GET|DELETE_AS_GET)
   */
  @Override
  public void setHttpOperation( boolean pSet, final int pValue ) {
    switch( pValue ) {
      case POST_AS_GET:    mPOST_AS_GET   = pSet; break;
      case PUT_AS_GET:     mPUT_AS_GET    = pSet; break;
      case DELETE_AS_GET:  mDELETE_AS_GET = pSet; break;
    }
    return;
  }
  
  /** getHttpOperation
   * 
   * @param pValue
   * @return (true|false)
   */
  @Override
  public boolean getHttpOperation( final int pValue ) {
    switch( pValue ) {
      case POST_AS_GET:    return mPOST_AS_GET;
      case PUT_AS_GET:     return mPUT_AS_GET;
      case DELETE_AS_GET:  return mDELETE_AS_GET;      
    }
    return false;
  }
  
  /** setCredentials
   * @param pUsername
   * @param pPassword
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void setCredentials( final String pUsername, final char[] pPassword) throws JC_Exception {
    getRESTComm().setUsername( pUsername, pPassword );
    return;
  }

  /** getUsername
   * 
   * @return
   */
  @Override
  public String getUsername() {
    return getRESTComm().getUsername();
  }
  
  /** getPassword
   * 
   * @return
   */
  @Override
  public char[] getPassword() {
    return getRESTComm().getPassword();
  }
  
  /** getUserNode
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getUserNode() throws JC_Exception {
//    return getNode( "/JeCARS/default/Users/" + getUsername() );
    return getRootNode().getNode( "JeCARS" ).getNode( "default" ).getNode( "Users" ).getNode( getUsername() );
  }

  /** getUserAsUserNode
   *
   * @return
   */
  @Override
  public JC_UserNode getUserAsUserNode() throws JC_Exception {
    return (JC_UserNode)getNode( "/JeCARS/default/Users/" + getUsername() ).morphToNodeType();
  }


  /** getUsersNode
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_UsersNode getUsersNode() throws JC_Exception {
    return (JC_UsersNode)getNode( "/JeCARS/default/Users" ).morphToNodeType();
  }

  /** getGroupsNode
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_GroupsNode getGroupsNode() throws JC_Exception {
    return (JC_GroupsNode)getNode( "/JeCARS/default/Groups" ).morphToNodeType();
  }
  
  /** setProxyCredentials
   * @param pUsername
   * @param pPassword
   */
  @Override
  public void setProxyCredentials( final String pUsername, final char[] pPassword ) throws JC_Exception {
    getRESTComm().setProxyUsername(pUsername);
    getRESTComm().setProxyPassword(pPassword);
    return;
  }

  /** setLoginType
   * Replaced by setLoginType( LOGINTYPE pType )
   * @param pType
   * @throws org.jecars.client.JC_Exception
   */
  @Deprecated
  @Override
  public void setLoginType( int pType ) throws JC_Exception {
//    mLoginType = pType;
    switch( pType ) {
      case LOGIN_TYPE_BASIC: {
        mLoginTypes = EnumSet.of( LOGINTYPE.BASIC );
        break;
      }
      case LOGIN_TYPE_GDATA: {
        mLoginTypes = EnumSet.of( LOGINTYPE.GDATA );
        break;
      }
      default: {
        mLoginTypes = EnumSet.of( LOGINTYPE.UNKNOWN );
        break;
      }
    }
    return;
  }

  /** getLoginType
   * Replaced by EnumSet<LOGINTYPE> getLoginTypes()
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Deprecated
  @Override
  public int getLoginType() throws JC_Exception {
//    return mLoginType;
    if (mLoginTypes.contains( LOGINTYPE.BASIC )) return LOGIN_TYPE_BASIC;
    if (mLoginTypes.contains( LOGINTYPE.GDATA )) return LOGIN_TYPE_GDATA;
    return LOGIN_TYPE_UNKNOWN;
  }


  /** setLoginType
   *
   * @param pType
   * @throws JC_Exception
   */
  @Override
  public void setLoginType( LOGINTYPE pType ) throws JC_Exception {
    mLoginTypes = EnumSet.of( pType );
    return;
  }

  /** getLoginTypes
   *
   * @return
   */
  @Override
  public EnumSet<LOGINTYPE> getLoginTypes() {
    return mLoginTypes;
  }

  /** retrieveGDataAuth
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_GDataAuth retrieveGDataAuth() throws JC_Exception {
    final JC_GDataAuth gda = new JC_GDataAuth();
    gda.setAuth( this, -1, "JeCARS Client", -1 );
    return gda;
  }

  /** retrieveGDataAuth
   * 
   * @param pKeyValidInHours
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_GDataAuth retrieveGDataAuth( final long pKeyValidInMinutes, final String pSource, final long pValidationExtension ) throws JC_Exception {
    final JC_GDataAuth gda = new JC_GDataAuth();
    gda.setAuth( this, pKeyValidInMinutes, pSource, pValidationExtension );
    return gda;
  }


  /** setCredentials
   * @param pAuth
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void setCredentials( final JC_GDataAuth pAuth ) throws JC_Exception {
    mGDataAuth = pAuth;
    getRESTComm().setGoogleLoginAuth( mGDataAuth.getAuth() );
    getRESTComm().setUsername( null, null );
    return;
  }

  /** isServerAvailable
   * @return
   */    
  @Override
  public boolean isServerAvailable() {
    try {
      final JC_Nodeable n = getNode( "/JeCARS" );
      n.refresh();
    } catch( Exception e ) {
      mLastThrowable = e;
      return false;
    }
    return true;
  }

  /** validCredentials
   *
   * @return
   * @throws JC_Exception
   */
  @Override
  public boolean validCredentials() throws JC_Exception {
    try {
      final JC_Nodeable n = getNode( "/JeCARS" );
      n.refresh();
    } catch( JC_HttpException je ) {
      if (je.getHttpErrorCode().getErrorCode()==HttpURLConnection.HTTP_UNAUTHORIZED) {
        return false;
      }
      throw je;
    }
    return true;
  }
  
  /** getNode as stream
   * 
   * @param pPath
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Streamable getNodeAsStream( final String pPath ) throws JC_Exception {
    JC_Streamable s = null;
    
    final JC_RESTComm comm = getRESTComm();
    
    try {
        final HttpURLConnection conn = comm.createHttpConnection( getServerPath() + pPath );
        final JD_Taglist   tags = comm.sendMessageGET( this, conn );
        if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
//        s = new JC_DefaultStream();
          s = JC_DefaultStream.createStream( JC_RESTComm.getResultStream( tags ) , (String)tags.getData( "ContentType" ), (String)tags.getData( "ContentEncoding" ) );
//        s.setStream( JC_RESTComm.getResultStream( tags ) );
//        s.setContentType( (String)tags.getData( "ContentType" ) );
        } else {
          throw JC_Utils.createCommException( tags, "while retrieving object ", pPath );
        }
    } catch( IOException ie ) {
      throw new JC_Exception(ie);
    }

    return s;
  }

  /** getNode as stream
   * @param pPath
   * @return JC_Streamble
   */
  @Override
  public JC_Streamable getNodeAsStream( final JC_Path pPath ) throws JC_Exception {
    return getNodeAsStream( pPath.getPath() );
  }
  
  
  /** getNode as stream
   * 
   * @param pPath
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Streamable getNodeAsStream( final String pPath, JC_Params pParams, JC_Filter pFilter, JC_Query pQuery ) throws JC_Exception {
    JC_Streamable s = null;
    
    JC_RESTComm comm = getRESTComm();   
    StringBuilder url = new StringBuilder( getServerPath() );
    url.append( pPath );
    try {
      JC_Utils.buildURL( this, url, pParams, pFilter, pQuery );
//      gLog.log( Level.INFO, "GET: " + url.toString() );
      HttpURLConnection conn = comm.createHttpConnection( url.toString() );
      JD_Taglist tags = comm.sendMessageGET( this, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        s = new JC_DefaultStream();
        s.setStream( JC_RESTComm.getResultStream( tags ) );
        s.setContentType( (String)tags.getData( "ContentType" ) );
      } else {
        throw JC_Utils.createCommException( tags, "while retrieving object", pPath );
      }
    } catch( IOException ie ) {
      throw new JC_Exception(ie);
    }
    return s;    
  }
  
  /** getNode as stream
   * 
   * @param pPath
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Streamable getNodeAsStream( final JC_Path pPath, final JC_Params pParams, final JC_Filter pFilter, final JC_Query pQuery ) throws JC_Exception {
    return getNodeAsStream( pPath.getPath(), pParams, pFilter, pQuery );
  }


  /** getNode
   * @param pPath e.g.  "/JeCARS/default/Users"
   * @param pRetrieve
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  @Deprecated
  public JC_Nodeable getNode( final String pPath ) throws JC_Exception {
    return getNode( pPath, true );
  }

  /** getSingleNode
   * 
   * @param pPath
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Nodeable getSingleNode( final String pPath ) throws JC_Exception {
    JC_DefaultNode node = (JC_DefaultNode)createNodeClass( JC_Clientable.CREATENODE_DEFAULT );
    node.setPath( pPath );
    node.populateProperties( null );
    return node;
  }
  
  /** getNode
   * 
   * @param pRelNode
   * @param pPath
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Nodeable getNode( final JC_Nodeable pRelNode, final String pPath ) throws JC_Exception {
    if ((pRelNode==null) || pRelNode.isNull()) {
      return getNode( pPath );
    }
    final JC_Path p = new JC_Path( pPath );
    p.resolveTo( pRelNode.getPath() );
    return getNode( p );
  }

  
  /** getNode
   * 
   * Returns the node specified by the path. The returned Node object
   * is synchronized with JeCARS and therefore up-to-date.
   * 
   * @param pPath Path of the Node
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  @Deprecated
  public JC_Nodeable getNode( final String pPath, final boolean pRetrieve ) throws JC_Exception {

    JC_DefaultNode newNode;
    JC_DefaultNode parentNode;
    final JC_DefaultNode returnNode;

    newNode = (JC_DefaultNode)createNodeClass( JC_Clientable.CREATENODE_DEFAULT );
    returnNode = newNode;

    JC_Path path = new JC_Path(pPath);
    
    while( path.hasPaths() ) {
        
      newNode.setPath( path.toString() );
      path = path.getParent();  
      parentNode = (JC_DefaultNode)createNodeClass( JC_Clientable.CREATENODE_DEFAULT );
      parentNode.setPath(path.toString());             
        
      if(path.toString().equals("")) {
        newNode.setParent(this.getRootNode());
      } else {
        newNode.setParent(parentNode);
      }        
      newNode = parentNode;
    }
    if (pRetrieve) {
      returnNode.populateProperties( null );
    }
    return returnNode;    
  }

  /** getNode
   * @param pPath
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  @Deprecated
  public JC_Nodeable getNode( final JC_Path pPath ) throws JC_Exception {
    return getNode( pPath.getPath(), true );
  }

  @Override
  public List<JC_Event> getLongPollEvents( final String pPath ) throws JC_Exception {

    final List<JC_Event> events = new ArrayList<JC_Event>();
    
    JC_RESTComm comm = getRESTComm();   
    StringBuilder url = new StringBuilder( getServerPath() );
    url.append( pPath );
    try {
      final JC_Params p = createParams( JC_RESTComm.GET ).cloneParams();
      p.setLongPoll( true );
      JC_Utils.buildURL( this, url, p, null, null );
      HttpURLConnection conn = comm.createHttpConnection( url.toString() );
      JD_Taglist tags = comm.sendMessageGET( this, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        Properties props = new Properties();
        props.load( JC_RESTComm.getResultStream( tags ) );
        int ix = 0;
        while( props.containsKey( "Event.Path." + ix )) {
          final Calendar date = Calendar.getInstance();
          date.setTimeInMillis( Long.parseLong( props.getProperty( "Event.Date." + ix ) ));
          int type = Integer.parseInt( props.getProperty( "Event.Type." + ix ) );
          JC_Event.TYPE evtype = JC_Event.TYPE.NODE_ADDED;
          switch( type ) {
            case Event.NODE_ADDED: {
              evtype = JC_Event.TYPE.NODE_ADDED;
              break;
            }
            case Event.NODE_MOVED: {
              evtype = JC_Event.TYPE.NODE_MOVED;
              break;
            }
            case Event.NODE_REMOVED: {
              evtype = JC_Event.TYPE.NODE_REMOVED;
              break;
            }
            case Event.PROPERTY_ADDED: {
              evtype = JC_Event.TYPE.PROPERTY_ADDED;
              break;
            }
            case Event.PROPERTY_CHANGED: {
              evtype = JC_Event.TYPE.PROPERTY_CHANGED;
              break;
            }
            case Event.PROPERTY_REMOVED: {
              evtype = JC_Event.TYPE.PROPERTY_REMOVED;
              break;
            }
          }
          final JC_Event ev = new JC_DefaultEvent( date,
                    props.getProperty( "Event.Identifier." + ix ),
                    new JC_Path( props.getProperty( "Event.Path." + ix )),                  
                    evtype );
          
          events.add( ev );
        }
      } else {
        throw JC_Utils.createCommException( tags, "while retrieving object", pPath );
      }
    } catch( IOException ie ) {
      throw new JC_Exception(ie);
    }
    return events;
  }

  
  /** getRights
   * 
   * @param pPath
   * @return a JC_Rights object or null if the creds has no rights at all
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Rights getRights( final String pPath ) throws JC_Exception {
    final JC_Nodeable n = getRootNode().getNode( pPath.substring(1) );
    return n.getRights( "/JeCARS/default/Users/" + getUsername() );
  }

  
  /** getRootNode
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getRootNode() throws JC_Exception {
    if (mRootNode==null) {
      try {
        JC_RootNode rootnode = null;
        String sp = getServerPath();
        List<String>subpaths = new ArrayList<String>();
        do {
          if (rootnode!=null) {
            subpaths.add( 0, sp.substring( sp.lastIndexOf( '/' )+1 ) );
            sp = sp.substring( 0, sp.lastIndexOf( '/' ));
            setServerPath( sp );
          }
          rootnode = (JC_RootNode)createNodeClass( "[root]" );
          rootnode.setClient( this );
          rootnode.setPath( "/" );
          rootnode.refresh();
          rootnode.getProperties();
        } while( !"rep:root".equals(rootnode.getNodeType()) );
        JC_Nodeable rootn = rootnode;
        for( final String spn : subpaths ) {
          rootn = rootn.getNode( spn );
        }
        mRootNode = rootn;
      } catch( Exception e ) {
        throw new JC_Exception( e );
      }
    }
    return mRootNode;
  }
    
  /** registerNodeClass
   * @param pNodeType
   * @param pClass
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void registerNodeClass( final String pNodeType, final String pClass ) throws JC_Exception {
    if (!mNodeTypeRegistry.containsKey( pNodeType )) {
      mNodeTypeRegistry.put( pNodeType, pClass );
    }
    return;
  }

  @Override
  public void registerOverrideNodeClass(String pNodeType, String pClass) throws JC_Exception {
    mNodeTypeRegistry.put( pNodeType, pClass );
    return;
  }

  
  
  /** createNodeClass
   * @param pNodeType
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable createNodeClass( final String pNodeType ) throws JC_Exception {
    JC_DefaultNode dn;
    try {
      if (mNodeTypeRegistry.containsKey( pNodeType )) {
        dn = (JC_DefaultNode)Class.forName( mNodeTypeRegistry.get( pNodeType ) ).newInstance();
        dn.setClient( this );
        final int ix = pNodeType.indexOf( '|' );
        if (ix==-1) {
          dn.setNodeType( pNodeType );
        } else {
          dn.setNodeType( pNodeType.substring( ix )+1 );
        }
      } else {
        dn = (JC_DefaultNode)Class.forName( mNodeTypeRegistry.get( CREATENODE_DEFAULT )).newInstance();
        dn.setClient( this );
        dn.setNodeType( pNodeType );
      }      
    } catch(ClassNotFoundException cne) {
      throw new JC_Exception( cne ); 
    } catch(InstantiationException ie) {
      throw new JC_Exception( ie );
    } catch( IllegalAccessException iae ) {
      throw new JC_Exception( iae );
    } catch( Exception e ) {
      throw new JC_Exception( e );      
    }
    return dn;
  }

  /** canBeMorphed
   * 
   * @param pNode
   * @param pNodeType
   * @return
   */
  @Override
  public boolean canBeMorphed( final JC_DefaultNode pNode, final String pNodeType ) throws JC_Exception {
    final String nt = pNode.getNodeType();
    if (nt.equals(pNodeType)) {
      if (mNodeTypeRegistry.containsKey( pNodeType )) {
        final String clss = mNodeTypeRegistry.get( pNodeType );
        final String ntc = pNode.getClass().getCanonicalName();
        return !clss.equals(ntc);
      }
    }
    return false;
  }

  /** getRESTComm
   * @return
   */
  @Override
  public JC_RESTComm getRESTComm() {
    return mRESTComm;
  }

  /** addListener
   * @param pNode
   * @param pDeep
   * @param pListener
   * @throws org.jecars.client.JC_Exception
   */  
  @Override
  public void addListener(JC_Nodeable pNode, boolean pDeep, JC_Listener pListener) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /** removeListener
   * @param pListener
   * @throws org.jecars.client.JC_Exception
   */    
  @Override
  public void removeListener(JC_Listener pListener) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }


  /** setDefaultParams
   *
   * @param pParams
   */
  @Override
  public void setDefaultParams( final String pType, JC_Params pParams ) {
    mDefaultParams.put( pType, pParams );
    return;
  }

  /** createParams
   *
   * @param pType
   * @return
   */
  @Override
  public JC_Params createParams( final String pType ) {
    if (pType!=null) {
      if (mDefaultParams.containsKey(pType)) {
        return mDefaultParams.get( pType );
      } else {
        JC_Params p = JC_Params.createParams();
        setDefaultParams( pType, p );
        return p;
      }
    }
    return JC_Params.createParams();
  }

  /** sendVersionInfo
   *
   * @param pMessage
   * @param pBody
   */
  @Override
  public void sendVersionInfo( final String pMessage, final String pBody ) throws Exception {
    final JC_DefaultNode reportVersion = (JC_DefaultNode)getNode( "/JeCARS/ApplicationSources/ToolsApp" );
    final JC_Params params = createParams( JC_RESTComm.GET );
    if (pMessage!=null) {
      params.addOtherParameter( "Message", pMessage );
    }
    if (pBody!=null) {
      params.addOtherParameter( "Body", pBody );
    }
    reportVersion.getNode( "reportVersion", params );
    reportVersion.destroy();
    return;
  }

  /** isLocalClient
   *
   * @return
   */
  @Override
  public boolean isLocalClient() {
    return false;
  }


  /** useChunkedStreamingMode
   *
   * @param pUse
   */
  @Override
  public void useChunkedStreamingMode( final boolean pUse ) {
    mChunkedStreamingMode = pUse;
    return;
  }

  /** isInChunkedStreamingMode
   *
   * @return
   */
  @Override
  public boolean isInChunkedStreamingMode() {
    return mChunkedStreamingMode;
  }


}
