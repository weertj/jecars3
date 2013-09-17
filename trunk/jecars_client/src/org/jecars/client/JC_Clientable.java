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

import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.List;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.jecars.client.observation.JC_ObservationManager;

/**
 * JC_Clientable
 *
 * Startup options
 * 
 *   jar -jar ... -Dhttp.proxyHost={proxy server] -Dhttp.proxyPort={proxy portnumber}
 *   jar -jar ... -Dhttp.proxyHost={proxy server] -Dhttp.proxyPort={proxy portnumber} -Dhttp.proxyUser={user} -Dhttp.proxyPassword={password}
 *   jar -jar ... -Dhttps.proxyHost={proxy server] -Dhttps.proxyPort={proxy portnumber}
 *   jar -jar ... -Dhttps.proxyHost={proxy server] -Dhttps.proxyPort={proxy portnumber} -Dhttps.proxyUser={user} -Dhttps.proxyPassword={password}
 *
 *
 * @version $Id: JC_Clientable.java,v 1.16 2009/07/31 07:57:01 weertj Exp $
 */
public interface JC_Clientable {

  final static public String VERSION_ID = "v3.0.6";
  final static public String VERSION = "JeCARS 'Elderberry' Client Development " + VERSION_ID;

  @Deprecated
  final static int LOGIN_TYPE_UNKNOWN = 0; 
  @Deprecated
  final static int LOGIN_TYPE_BASIC   = 1;
  @Deprecated
  final static int LOGIN_TYPE_GDATA   = 2;

  static public enum LOGINTYPE { UNKNOWN, BASIC, GDATA };

  final static int POST_USE_PATH = 1;
  final static int POST_USE_SLUG = 2;
  
  final static String CREATENODE_DEFAULT = "*";
  
  final static int POST_AS_GET    = 20;
  final static int PUT_AS_GET     = 21;
  final static int DELETE_AS_GET  = 22;
  final static int HEAD_AS_GET    = 23;
  
  EnumSet<JC_ClientOption> getOptions();
  
  /** setPostCreationMethod
   * 
   * @param pMethod
   *          POST_USE_PATH = The URL path in the POST message is the object path to be created
   *          POST_USE_SLUG = The Slug http request parameter is the object name to be created
   */
  void setPostCreationMethod( int pMethod );

  /** getPostCreationMethod
   * 
   * @return (POST_USE_PATH|POST_USE_SLUG)
   */
  int getPostCreationMethod();
  
  /** setHttpOperation
   * 
   * @param pSet true then set (otheriwse reset)
   * @param pValue (POST_AS_GET|PUT_AS_GET|DELETE_AS_GET)
   */
  void setHttpOperation( boolean pSet, int pValue );
  
  /** getHttpOperation
   * 
   * @param pValue
   * @return (true|false)
   */
  boolean getHttpOperation( int pValue );
  
  /** setLoginType
   * Replaced by setLoginType( LOGINTYPE pType )
   * @param pType
   * @throws org.jecars.client.JC_Exception
   */
  @Deprecated
  void setLoginType( int pType ) throws JC_Exception;

  /** getLoginType
   * Replaced by EnumSet<LOGINTYPE> getLoginTypes()
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Deprecated
  int getLoginType() throws JC_Exception;
  
  /** setProxyCredentials
   * @param pUsername
   * @param pPassword
   */
  void setProxyCredentials( final String pUsername, final char[] pPassword ) throws JC_Exception;

  /** getUsername
   * 
   * @return
   */
  String getUsername();
  
  /** getPassword
   * 
   * @return
   */
  char[] getPassword();

  /** getUserNode
   * 
   * @return
   */
  JC_Nodeable getUserNode() throws JC_Exception;

  /** getUserAsUserNode
   *
   * @return
   */
  JC_UserNode getUserAsUserNode() throws JC_Exception;
  
  /** getUsersNode
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_UsersNode getUsersNode() throws JC_Exception;

  /** getGroupsNode
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_GroupsNode getGroupsNode() throws JC_Exception;

  /** setCredentials
   * @param pUsername
   * @param pPassword
   */
  void setCredentials( final String pUsername, final char[] pPassword ) throws JC_Exception;
  
  /** retrieveGDataAuth
   * 
   * http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html
   * 
   * @return
   */
  JC_GDataAuth retrieveGDataAuth() throws JC_Exception;
  
  /** retrieveGDataAuth
   * 
   * @param pKeyValidInMinutes  The key will stay valid for at least pKeyValidInMinutes minutes
   * @param pSource The application name which create the key (free string)
   * @param pValidationExtension Every the key is checked by JeCARS the "expire" date of the key is extended with pValidationExtension minutes
   * @return The JC_GDataAuth key
   * @throws JC_Exception 
   */
  JC_GDataAuth retrieveGDataAuth( final long pKeyValidInMinutes, final String pSource, final long pValidationExtension ) throws JC_Exception;  

  
  /** setCredentials
   * @param pAuth
   */
  void setCredentials( final JC_GDataAuth pAuth ) throws JC_Exception;

  /** isServerAvailable
   * @return
   */
  boolean isServerAvailable();
  
  /** validCredentials
   * @return
   */
  boolean validCredentials() throws JC_Exception;
  
  /** getNode as stream
   * @param pPath
   * @return JC_Streamble
   */
  JC_Streamable getNodeAsStream( final String pPath ) throws JC_Exception;

  /** getNode as stream
   * @param pPath
   * @return JC_Streamble
   */
  JC_Streamable getNodeAsStream( final JC_Path pPath ) throws JC_Exception;

  /** getNode as stream
   * 
   * @param pPath
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Streamable getNodeAsStream( final String pPath, JC_Params pParams, JC_Filter pFilter, JC_Query pQuery ) throws JC_Exception;

  /** getNode as stream
   * 
   * @param pPath
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Streamable getNodeAsStream( final JC_Path pPath, JC_Params pParams, JC_Filter pFilter, JC_Query pQuery ) throws JC_Exception;

  /** getNode
   * @param pPath e.g.  "/JeCARS/default/Users"
   * @param pRetrieve
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Deprecated
  JC_Nodeable getNode( final String pPath, final boolean pRetrieve ) throws JC_Exception;

  /** getNode
   * @param pPath e.g.  "/JeCARS/default/Users"
   * @return JC_Nodeable
   */
  @Deprecated
  JC_Nodeable getNode( final String pPath ) throws JC_Exception;

  JC_Nodeable getNode( final JC_Nodeable pRelNode, final String pPath ) throws JC_Exception;

  /** getNode
   * @param pPath
   * @return
   */
  @Deprecated
  JC_Nodeable getNode( final JC_Path pPath ) throws JC_Exception;

  /** getSingleNode -- Only retrieve this node, the parent nodes are not created, this function
   *    is preferred above the getNode() variants
   * 
   * @param pPath
   * @return
   * @throws JC_Exception 
   */
  JC_Nodeable getSingleNode( final String pPath ) throws JC_Exception;

  /** getLongPollEvents
   * 
   * @param pPath
   * @return
   * @throws JC_Exception 
   */
  List<org.jecars.client.observation.JC_Event> getLongPollEvents( final String pPath ) throws JC_Exception;
  
  /** getRootNode
   *
   * @return the root node ("/")
   */
  JC_Nodeable getRootNode() throws JC_Exception;
  
  /** getRights
   * 
   * @param pPath
   * @return a JC_Rights object or null if the creds has no rights at all
   * @throws org.jecars.client.JC_Exception
   */
  JC_Rights getRights( final String pPath ) throws JC_Exception;
  
  /** registerNodeClass
   * @param pNodeType
   * @param pClass
   * @throws org.jecars.client.JC_Exception
   */
  void registerNodeClass( String pNodeType, String pClass ) throws JC_Exception;

  /** canBeMorphed
   * 
   * @param pNode
   * @param pNodeType
   * @return
   */
  boolean canBeMorphed( JC_DefaultNode pNode, final String pNodeType ) throws JC_Exception;

  
  /** createNodeClass
   * @param pNodeType   
   * @throws Exception
   */
  JC_Nodeable createNodeClass( final String pNodeType ) throws JC_Exception;

  /** addNamespace
   * 
   * @param pId
   * @param pUrl
   */
  void addNamespace( String pId, String pUrl );
  
  /** getNamespaces
   * 
   * @return
   */
  AbstractMap<String, String>getNamespaces();

  
  /** addListener
   * @param pNode
   * @param pDeep
   * @param pListener
   * @throws org.jecars.client.JC_Exception
   */
  void addListener( JC_Nodeable pNode, boolean pDeep, JC_Listener pListener ) throws JC_Exception;
  
  /** removeListener
   * @param pListener
   * @throws org.jecars.client.JC_Exception
   */
  void removeListener( JC_Listener pListener ) throws JC_Exception;
  
  /** getServerPath
   * @return
   */
  String getServerPath();
  
  /** getRESTComm
   * @return
   */
  JC_RESTComm getRESTComm();

  /** setDefaultParams
   * 
   * @param pParams
   */
  void setDefaultParams( final String pType, JC_Params pParams );

  /** createParams
   * 
   * @return
   */
  JC_Params createParams( final String pType );

  /** sendVersionInfo
   * 
   * @param pMessage
   * @param pBody
   */
  void sendVersionInfo( final String pMessage, final String pBody ) throws Exception;

  /** getObservationManager
   * 
   * @return
   */
  JC_ObservationManager getObservationManager();

  /** setLoginType
   *
   * @param pType
   * @throws JC_Exception
   */
  void setLoginType( LOGINTYPE pType ) throws JC_Exception;

  /** getLoginTypes
   *
   * @return
   * @throws JC_Exception
   */
  EnumSet<LOGINTYPE> getLoginTypes() throws JC_Exception;

  /** isLocalClient
   * 
   * @return
   */
  boolean isLocalClient();

  /** useChunkedStreamingMode
   *
   * @param pUse
   */
  void useChunkedStreamingMode( boolean pUse );

  /** isInChunkedStreamingMode
   * 
   * @return
   */
  boolean isInChunkedStreamingMode();

  
  Throwable getLastThrowable();  

  void clearLastThrowable();

  
}
