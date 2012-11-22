/*
 * Copyright 2007-2012 NLR - National Aerospace Laboratory
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

import com.google.gdata.data.DateTime;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.AccessControlException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.jcr.*;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.*;
import nl.msd.jdots.JD_Taglist;
//import org.apache.jackrabbit.commons.query.GQL;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.output.*;
import org.jecars.support.BASE64Encoder;
import org.jecars.support.CARS_Buffer;
import org.jecars.support.CARS_DefaultPropertyIterator;
import org.jecars.support.CARS_Mime;
import org.jecars.version.CARS_JCRVersionManager;
import org.jecars.version.CARS_VersionManager;

/**
 * CARS_ActionContext.java
 *
 * @version $Id: CARS_ActionContext.java,v 1.29 2009/06/22 22:37:02 weertj Exp $
 */
public class CARS_ActionContext {

  static final protected Logger LOG = Logger.getLogger( "org.jecars" );
  
  final public static long MAX_NO_GETOBJECTS = 10000000L;
  
  final public static int NO_ERROR      = 0;
  final public static int ERROR_UNKNOWN = 1000;

  static public boolean        gUntransport = true;
  static public final Pattern gQueryPattern = Pattern.compile( "&" );
  
  final private static JD_Taglist gOutputGenerators = new JD_Taglist();
  final private static JD_Taglist gVersionManagers  = new JD_Taglist();
  
  // ****
  public final static String DEF_NS           = CARS_Definitions.DEFAULTNS;
  public final static String gDefPublished    = DEF_NS + "Published";
  public final static String DEF_MODIFIED     = DEF_NS + "Modified";
  public final static String gDefLastAccessed = DEF_NS + "LastAccessed";
  public final static String gDefExpireDate   = DEF_NS + "ExpireDate";
  public final static String gDefActions      = DEF_NS + "Actions";
  public final static String gDefTitle        = DEF_NS + "Title";
  public final static String gDefBody         = DEF_NS + "Body";
  public final static String gDefURLResource  = DEF_NS + "urlresource";
  public final static String gDefURL          = DEF_NS + "URL";
  public final static String gDefLink         = DEF_NS + "Link";

  public final static String gDefFullText     = DEF_NS + "q";
  public final static String gDefAlt          = DEF_NS + "alt";
  public final static String gDefXPath        = DEF_NS + "xpath";
  public final static String gDefSQL          = DEF_NS + "sql";
  public final static String gDefSQL2         = DEF_NS + "sql2";
  public final static String gDefQOM          = DEF_NS + "qom";
  public final static String gDefReferences   = DEF_NS + "references";
  public final static String gDefRights       = DEF_NS + "rights";
  public final static String gDefKeywords     = DEF_NS + "keywords";
  public final static String gDefChildNodeDefs= DEF_NS + "childnodedefs";
  public final static String gDefDeep         = DEF_NS + "deep";
  public final static String gDefUpdatedMin   = DEF_NS + "updated-min";
  public final static String gDefUpdatedMax   = DEF_NS + "updated-max";
  public final static String gDefPublishedMin = DEF_NS + "published-min";
  public final static String gDefPublishedMax = DEF_NS + "published-max";
  public final static String gDefCreatedMin   = DEF_NS + "created-min";
  public final static String gDefCreatedMax   = DEF_NS + "created-max";
  public final static String gDefMaxResults   = DEF_NS + "max-results";
  public final static String gDefStartIndex   = DEF_NS + "start-index";
  public final static String gDefThisNode     = DEF_NS + "this-node";
  public final static String gDefOrderBy      = DEF_NS + "orderby";
  public final static String gDefOrderByType  = DEF_NS + "orderbytype";
  public final static String gDefWhere        = DEF_NS + "where";
  public final static String gDefGQL          = DEF_NS + "gql";   // **** GQL is a simple fulltext query language, which supports field prefixes similar to Lucene or Google queries.
  public final static String gDefNamePattern  = DEF_NS + "namePattern";
  public final static String gIncludeBinary   = DEF_NS + "includeBinary";


  /** Versioning parameters
   */
  public final static String gDefaultVCS    = "jcr";
  public final static String gDefVCS        = DEF_NS + "vcs";
  public final static String gDefVCSCmd     = DEF_NS + "vcs-cmd";
  public final static String gDefVCSLabel   = DEF_NS + "vcs-label";
  public final static String gDefVCSHistory = DEF_NS + "vcs-history";
  
  /** Action parameters
   */
  public final static String gDefActionGET    = "GET";
  public final static String gDefActionPUT    = "PUT";
  public final static String gDefActionDELETE = "DELETE";
  public final static String gDefActionPOST   = "POST";

  public final static String NTJ_ROOT = DEF_NS + "root";

  public final static List<String> gIncludeNS = new ArrayList<String>();
  
  protected static String               gFeedHeader    = null;
  protected static String               gServiceHeader = null;

  private String            mAction   = null;
  private String            mUsername = null;
  private char[]            mPassword = null;
  private String            mAuthKey  = null;
  private String            mPathInfo = null;
  private String            mQuery    = null;
  private Map               mParameterMap = null;
  private String            mCategoryFilter  = null;
  private String            mVersionFilter   = null;
  private InputStream       mBodyStream      = null;
  private String            mBodyContentType = null;
  private String            mContextPath         = null;
  private String            mBaseURL             = null;
  private Throwable         mError               = null;
  private int               mErrorCode           = HttpURLConnection.HTTP_OK;
  private Node              mThisNode            = null;
  private Property          mThisProperty        = null;
  private Node              mCreatedNode         = null;
  private Vector            mDeletedNodePaths    = null;
  private String            mResultContentsType   = null;
  private InputStream       mResultContentsStream = null;
  private long              mResultContentsLength = -1L;
  private Map<String,ArrayList<String>> mResultHttpHeaders = null;
  private DateTime          mIfModifiedSince = null;
  private String            mIfNoneMatch     = null;
  private String            mRemoteHost      = "jecars.org";
  private String            mUserAgent       = "-";
  private String            mReferer         = "-";
  private int               mServerPort      = 80;
  private long              mRangeFrom       = -1;
  private long              mRangeTo         = -1;

  /** If true then the If-Modified-Since header is supported
   */
  private transient boolean mCanBeCachedResult = false;

//  private StringBuilder     mReplyXML    = null;
//  private InputStream       mReplyStream = null;
  private CARS_Main         mMain        = null;

  private JD_Taglist        mQueryParametersTL = null;
//  private JD_Taglist        mParametersTL = null;


  /** Creates a new instance of CARS_ActionContext
   */
  public CARS_ActionContext() {
    if (gOutputGenerators.isEmpty()) {
      synchronized( gOutputGenerators ) {
        gOutputGenerators.clear();
        gOutputGenerators.replaceData( "atom", new CARS_OutputGenerator_Atom() );
        LOG.info( "OutputGenerator 'atom' added" );
        gOutputGenerators.replaceData( "html", new CARS_OutputGenerator_HTML() );
        LOG.info( "OutputGenerator 'html' added" );
        gOutputGenerators.replaceData( "textentries", new CARS_OutputGenerator_TextEntries() );
        LOG.info( "OutputGenerator 'textentries' added" );
        gOutputGenerators.replaceData( "properties", new CARS_OutputGenerator_Properties() );
        LOG.info( "OutputGenerator 'properties' added" );
        // **** TODO Elderberry
//        gOutputGenerators.replaceData( "backup", new CARS_OutputGenerator_Backup() );
//        LOG.info( "OutputGenerator 'backup' added" );
        gOutputGenerators.replaceData( "json", new CARS_OutputGenerator_JSON() );
        LOG.info( "OutputGenerator 'json' added" );
        gOutputGenerators.replaceData( "xml", new CARS_OutputGenerator_XML() );
        LOG.info( "OutputGenerator 'xml' added" );
        gOutputGenerators.replaceData( "xmltable", new CARS_OutputGenerator_XMLTable() );
        LOG.info( "OutputGenerator 'xmltable' added" );
        gOutputGenerators.replaceData( "binary", new CARS_OutputGenerator_Binary() );
        LOG.info( "OutputGenerator 'binary' added" );
        // **** TODO Elderberry
//        gOutputGenerators.replaceData( "wfxml", new CARS_OutputGenerator_WorkflowXML() );
//        LOG.info( "OutputGenerator 'wfxml' added" );
      }
    }
    if (gVersionManagers.isEmpty()) {
      synchronized( gVersionManagers ) {
        gVersionManagers.clear();
        gVersionManagers.replaceData( gDefaultVCS, new CARS_JCRVersionManager() );
      }
    }
    return;
  }
  
  /** Get version manager 
   * @param pKey which versionmanager to user, if null then gDefaultVCS
   * @return the instance of the version manager
   */
  static public CARS_VersionManager getVersionManager( String pKey ) {
    if (pKey==null) pKey = gDefaultVCS;
    return (CARS_VersionManager)gVersionManagers.getData( pKey );
  }
  
  /** Retrieve the APP service header, "application/atomsvc+xml" content type
   * @return The service header
   */
  static public String getServiceHeader() {    
    try {
      if (gServiceHeader==null) {
        gServiceHeader = "<?xml version=\"1.0\" encoding='utf-8'?>\n" +
                      "<service xmlns=\"http://purl.org/atom/app#\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n";
      }
    } catch( Exception e ) {
      LOG.log( Level.SEVERE, null, e );
    }
    return gServiceHeader;
  }

  /** Retrieve the APP service header, "application/atomsvc+xml" content type
   * @return The service header
   */
  static public String getFeedHeader() {    
    try {
      if (gFeedHeader==null) {
        gFeedHeader = "<?xml version=\"1.0\" encoding='utf-8'?>\n" +
                      "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:openSearch=\"http://a9.com/-/spec/opensearchrss/1.0/\"\n";
//        Iterator it = gIncludeNS.iterator();
        Session appSession = CARS_Factory.getSystemApplicationSession();
        synchronized(appSession) {
          String nms[] = appSession.getNamespacePrefixes();
          for( int i=0; i<nms.length; i++ ) {
            if (gIncludeNS.contains( nms[i] )) {
              gFeedHeader += " xmlns:" + nms[i] + "=\"" + appSession.getNamespaceURI( nms[i] ) + "\"";
            }
          }
        }
        gFeedHeader += ">\n";
      }
    } catch( Exception e ) {
      LOG.log( Level.SEVERE, null, e );
    }
    return gFeedHeader;
  }
  
  /** addPublicNamespace
   * 
   * @param pNamespace 
   */
  static public void addPublicNamespace( String pNamespace ) {
    gIncludeNS.add( pNamespace );
    gFeedHeader = null;
    return;
  }
  
  /** Create an action context containing the real password
   *
   * @param pUsername
   * @param pPassword
   * @return
   */
  static public CARS_ActionContext createActionContext( final String pUsername, final char pPassword[] ) {
    final CARS_ActionContext ac = new CARS_ActionContext();
    ac.mUsername = pUsername;
    ac.mPassword = pPassword;
    return ac;  
  }

  /** Create an action context containing the authentication key
   *
   * @param pKey
   * @return
   */
  static public CARS_ActionContext createActionContext( final String pKey ) {
    final CARS_ActionContext ac = new CARS_ActionContext();
    ac.mAuthKey = pKey;
    return ac;  
  }

  /** Clone an action context
   *
   * @param pContext
   * @return
   * @throws Exception
   */
  static public CARS_ActionContext createActionContext( final CARS_ActionContext pContext ) throws CloneNotSupportedException {
    final CARS_ActionContext ac = new CARS_ActionContext();
    if (pContext!=null) {
      ac.mUsername        = pContext.mUsername;
      ac.mPassword        = pContext.mPassword;
      ac.mAuthKey         = pContext.mAuthKey;
      ac.mPathInfo        = pContext.mPathInfo;
      ac.mQuery           = pContext.mQuery;
      ac.mParameterMap    = new HashMap();
      if (pContext.mParameterMap!=null) {
        ac.mParameterMap.putAll( pContext.mParameterMap );
      }
      ac.mCategoryFilter  = pContext.mCategoryFilter;
      ac.mVersionFilter   = pContext.mVersionFilter;
      // ac.mBodyStream 
      // ac.mBodyContentType
      ac.mContextPath     = pContext.mContextPath;
      ac.mBaseURL         = pContext.mBaseURL;
      ac.mError           = pContext.mError;
      ac.mErrorCode       = pContext.mErrorCode;
      ac.mServerPort      = pContext.mServerPort;
      // ac.mThisNode
      // ac.mCreatedNode
      // ac.mDeletedNodePaths
      // ac.mResultContentsType
      // ac.mResultContentsStream
      ac.mMain              = pContext.mMain;
    }
    if (ac.mQueryParametersTL!=null) {
      ac.mQueryParametersTL = (JD_Taglist)ac.mQueryParametersTL.clone();
    }
    return ac;  
  }
  
  /** Phishing check
   */
  static boolean isGoodJCRQueryParameter( String pParam ) {
    return true;
  }
  
  /** setMain
   * 
   * @param pMain
   */
  public void setMain( final CARS_Main pMain ) {
    mMain = pMain;
    mMain.addContext( this );
    return;
  }

  /** putMain
   * 
   * @param pMain 
   */
  public void putMain( final CARS_Main pMain ) {
    mMain = pMain;
    return;
  }

  
  /** getMain
   * 
   * @return
   */
  public CARS_Main getMain() {
    return mMain;
  }
  
  /** resetResults
   * 
   */
  public void resetResults() {
    mResultContentsType = null;
    mResultContentsLength = -1;
    if (mResultContentsStream!=null) {
      try {
        mResultContentsStream.close();
      } catch( IOException ie ) {
        LOG.log( Level.WARNING, ie.getMessage(), ie );
      }
      mResultContentsStream = null;
    }    
  }
  
  /** destroy
   * 
   */
  public void destroy() {
    if (mResultContentsStream!=null) {
      try {
        mResultContentsStream.close();
      } catch( IOException ie ) {
        LOG.log( Level.WARNING, ie.getMessage(), ie );
      }
      mResultContentsStream = null;
    }
    mUsername = null;
    mPassword = null;
    mAuthKey  = null;
    mPathInfo = null;
    mQuery    = null;
    mError    = null;
    mThisNode = null;
    mThisProperty = null;
    if (mQueryParametersTL!=null) {
      mQueryParametersTL.clear();
      mQueryParametersTL = null;
    }
    if (mMain!=null) {
      mMain.removeContext( this );
      mMain = null;
    }
    return;
  }

  /** getContextPath
   * 
   * @return
   */
  public String getContextPath() {
    return mContextPath;
  }
  
  /** Get base context URL path
   * @return the URL path, used to construct http links
   */
  public String getBaseContextURL() {
    return mBaseURL + mContextPath;
  }
  
  public String getUsername() {
    return mUsername;
  }
  
  public char[] getPassword() {
    return mPassword;
  }

  /** getAuthKey
   *
   * @return
   */
  public String getAuthKey() {
    return mAuthKey;
  }

  public String getPathInfo() {
    return mPathInfo;
  }

  /** getParameterMap
   *
   * @return
   */
  public Map getParameterMap() {
    return mParameterMap;
  }

  /** getQueryString
   *
   * @return
   */
  public String getQueryString() {
    return mQuery;
  }
  
  /** getQueryValue
   * 
   * @param pTag
   * @return
   */
  public String getQueryValue( final String pTag ) {
    final String[] params = getQueryStringParts();
    final int len = params.length;
    for (int i=0; i<len; i++ ) {
      if (params[i].startsWith( pTag )) {
        return params[i].substring( pTag.length() );
      }
    }
    return null;
  }

  /** getQueryValueResolved
   *
   * @param pTag
   * @return
   * @throws java.lang.Exception
   */
  public String getQueryValueResolved( final String pTag ) throws RepositoryException, UnsupportedEncodingException {
    final String[] params = getQueryStringParts();
    final int len = params.length;
    for (int i=0; i<len; i++ ) {
      if (params[i].startsWith( pTag )) {
        return convertValueName( pTag, untransportString(params[i].substring( pTag.length()+1 )));
      }
    }
    return null;
  }
  
  /** getQueryStringParts
   * 
   * @return
   */
  public String[] getQueryStringParts() {
    if (getQueryString()!=null) {
      return gQueryPattern.split( getQueryString() );
    }
    return CARS_Utils.EMPTY_STRING_ARRAY;
  }
  
  /** Returns the parameter taglist, this taglist will holds all parameters.
   */
//  public JD_Taglist getParameterTL() {
//    if (mParametersTL==null) mParametersTL = new JD_Taglist();
//    return mParametersTL;
//  }
  
  /** getQueryPartsAsTaglist
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist getQueryPartsAsTaglist() throws UnsupportedEncodingException, RepositoryException {
      
    if (mQueryParametersTL==null) {
      mQueryParametersTL = new JD_Taglist();
      final String[] param = getQueryStringParts();
      for( int i=0; i<param.length; i++ ) {
        final String[] parts = param[i].split( "=", 2 );
        final String propName = convertPropertyName( untransportString(parts[0]));
        if (parts.length==2) {
          mQueryParametersTL.replaceData( propName,
                        convertValueName( propName, untransportString(parts[1])) );
        } else {
          mQueryParametersTL.replaceData( propName,
                        convertValueName( propName, untransportString("")) );
        }
//          if (parts.length==2) { // **** TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//            String propName = convertPropertyName( untransportString(parts[0]));
//            mQueryParametersTL.replaceData( propName,
//                                  convertValueName( propName, untransportString(parts[1])) );
//          }
      }
    }
    return mQueryParametersTL;
  }

  /** addHttpHeader
   *
   * @param pKey
   * @param pValue
   */
  public void addHttpHeader( final String pKey, final String pValue ) {
    if (mResultHttpHeaders==null) mResultHttpHeaders = new HashMap<String,ArrayList<String>>();
    ArrayList<String> currentValues = mResultHttpHeaders.get( pKey );
    if (currentValues==null) {
      currentValues = new ArrayList<String>();
      mResultHttpHeaders.put( pKey, currentValues );
    }
    currentValues.add( pValue );
    return;
  }

  /** getHttpHeader
   *
   * @param pKey
   * @return
   */
  public List<String> getHttpHeader( final String pKey ) {
    if (mResultHttpHeaders!=null) {
      return mResultHttpHeaders.get( pKey );
    }
    return null;
  }

  /** getHttpHeaders
   *
   * @return
   */
  public Map<String,ArrayList<String>> getHttpHeaders() {
    return mResultHttpHeaders;
  }


  /** getParameterMapAsTaglist
   * 
   * @param pTags
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist getParameterMapAsTaglist( final JD_Taglist pTags ) throws RepositoryException {
    JD_Taglist paramsTL = pTags;
    if (getParameterMap()!=null) {
      if (paramsTL==null) {
        paramsTL = new JD_Taglist();
      }
      final Map paramMap = getParameterMap();
//            System.out.println("No. of param sent: " + paramMap.keySet().size());
      final Iterator it = paramMap.entrySet().iterator();
      while (it.hasNext()) { 
 	final Map.Entry entry = (Map.Entry)it.next();
//System.out.println("Key: " + entry.getKey() );
        final String[] vals = (String[])entry.getValue();
//System.out.println("Value: " + vals[0] + "\n");
//        paramsTL.replaceData( entry.getKey(), vals[0] );
        paramsTL.replaceData( convertPropertyName( (String)entry.getKey() ),
                              convertValueName(    (String)entry.getKey(), vals[0] ) );
//}
      }
    }
    return paramsTL;
  }
  
  /** setBodyStream
   * @param pStream
   * @param pContentType
   */
  public void setBodyStream( InputStream pStream, String pContentType ) {
    mBodyStream = pStream;
    mBodyContentType = pContentType;
    return;
  }
  
  /** getBodyContentType
   * 
   * @return
   */
  public String getBodyContentType() {
    return mBodyContentType;
  }
   
  /** getBodyStream
   *  
   * @return
   */
  public InputStream getBodyStream() {
    return mBodyStream;
  }
  
  /** setContentsResultStream
   * @param pStream
   * @param pContentType
   */
  public void setContentsResultStream( final InputStream pStream, final String pContentType ) {
    mResultContentsStream = pStream;
    mResultContentsType   = pContentType;
    return;
  }
  
  /** setContentsLength
   * 
   * @param pLen
   */
  public void setContentsLength( long pLen ) {
    mResultContentsLength = pLen;
    return;
  }

  /** getContentsLength
   *
   * @return
   */
  public long getContentsLength() {
    return mResultContentsLength;
  }

  /** getResultContentsResultStream
   * @return
   */
  public InputStream getResultContentsResultStream() {
    return mResultContentsStream;
  }
  
  /** getResultContentType
   * @return
   */
  public String getResultContentType() {
    return mResultContentsType;
  }
  
  /** getLastModified
   * 
   * @return
   */
  public long getLastModified() throws RepositoryException {
    if (mThisNode!=null) {
      return getUpdatedDateTime( mThisNode );
    }
    return 0;
  }
    
  /** Set pathinfo
   *    '/-/' is category filter
   *    '/#/' is versioning filter
   */
  public void setPathInfo( String pPath ) {
    if (pPath==null) pPath = "";
    int idx = pPath.indexOf( "/-/");
    if (idx!=-1) {
      mPathInfo = pPath.substring( 0, idx );
      setCategoryFilter( pPath.substring( idx+3 ) );
    } else {
      mPathInfo = pPath;
    }
    mPathInfo = mPathInfo.replace( '+', ' ' );
    return;
  }

  /** setParameterMap
   *
   * @param pMap
   */
  public void setParameterMap( final Map pMap ) {
    mParameterMap = pMap;
    return;
  }

  /** getParameterFromMap
   *
   * @param pKey
   * @return
   */
  public String getParameterStringFromMap( final String pKey ) {
    if (mParameterMap!=null) {
      String[] o = (String[])mParameterMap.get( pKey );
      if ((o!=null) && (o.length>0)) {
        return o[0].toString();
      }
    }
    return null;
  }

  /** setQueryString
   *
   * @param pQuery
   */
  public void setQueryString( String pQuery ) {
    mQuery = pQuery;
    return;
  }
  
  /** There are parameter given which makes it neccesary to construct a query
   *
   * @param tags
   * @return true or false
   * @throws java.lang.Exception
   */
  private boolean mustConstructQuery( final JD_Taglist tags ) {
    if (getCategoryFilter()!=null)      return true;
    if (tags.getData( gDefDeep         )!=null) return true;
    if (tags.getData( gDefCreatedMin   )!=null) return true;
    if (tags.getData( gDefCreatedMax   )!=null) return true;
    if (tags.getData( gDefUpdatedMax   )!=null) return true;
    if (tags.getData( gDefPublishedMin )!=null) return true;
    if (tags.getData( gDefPublishedMax )!=null) return true;
    if (tags.getData( gDefUpdatedMin   )!=null) return true;
    if (tags.getData( gDefUpdatedMax   )!=null) return true;
    if (tags.getData( gDefFullText     )!=null) return true;
    if (tags.getData( gDefOrderBy      )!=null) return true;
    if (tags.getData( gDefWhere        )!=null) return true;
    if (tags.getData( gDefGQL          )!=null) return true;
    return false;
  }
  
  /** Set version filter (command)
   *  Version control is performed by parameter commands. (PUT)
   *    
   * @pParam pVersion
   *            /#/latest
   *            /#/oldest
   */
  public void setVersioningFilter( String pVersion ) {
    if (isGoodJCRQueryParameter( pVersion )) {
      mVersionFilter = pVersion;
    }
    return;
  }
  
  public String getVersioningFilter() {
    return mVersionFilter;
  }
  
  public void setCategoryFilter( String pCat ) {
    if (isGoodJCRQueryParameter( pCat )) {
      mCategoryFilter = pCat;
    }
    return;
  }
  
  public String getCategoryFilter() {
    return mCategoryFilter;
  }
  
  public void setBaseURL( String pURL ) {
    mBaseURL = pURL;
    return;
  }
  
  public void setContextPath( String pPath ) {
    mContextPath = pPath;
    return;
  }
  
  public int getErrorCode() {
    return mErrorCode;
  }
  
  public void setErrorCode( final int pErrorCode ) {
    mErrorCode = pErrorCode;
    return;
  }

  /** setError
   *
   * @param pError
   */
  public void setError( final Throwable pError ) {
    mError = pError;
    return;
  }

  /** getError
   *
   * @return
   */
  public Throwable getError() {
    return mError;
  }

  /** setCanBeCachedResult
   * 
   * @param pSet
   */
  public void setCanBeCachedResult( final boolean pSet ) {
    mCanBeCachedResult = pSet;
    return;
  }

  /** canBeCachedResult
   *
   * @return
   */
  public boolean canBeCachedResult() {
//    return false; // **** TODO
    return mCanBeCachedResult;
  }

  /** setRequestETag
   * 
   * @param pETag
   */
  public void setIfNoneMatch( final String pIfNoneMatch ) {
    mIfNoneMatch = pIfNoneMatch;
    return;
  }

  /** getIfNoneMatch
   *
   * @return
   */
  public String getIfNoneMatch() {
    return mIfNoneMatch;
  }

  /** setIfModifiedSince
   * 
   * @param pDate
   */
  public void setIfModifiedSince( final String pDate ) throws com.google.gdata.util.ParseException {
    if (pDate==null) {
      mIfModifiedSince = null;
    } else {
      mIfModifiedSince = DateTime.parseRfc822( pDate );
    }
    return;
  }

  /** getIfModifiedSince
   *
   * @return
   */
  public DateTime getIfModifiedSince() {
    return mIfModifiedSince;
  }

  /** setServerPort
   * 
   * @param pPort
   */
  public void setServerPort( final int pPort ) {
    mServerPort = pPort;
    return;
  }
  
  /** getServerPort
   * 
   * @return
   */
  public int getServerPort() {
    return mServerPort;
  }
  
  /** setReferer
   * 
   * @param pReferer
   */
  public void setReferer( final String pReferer ) {
    mReferer = pReferer;
    return;
  }

  /** getReferer
   *
   * @return
   */
  public String getReferer() {
    return mReferer;
  }


  /** setUserAgent
   * 
   * @param pAgent
   */
  public void setUserAgent( final String pAgent ) {
    mUserAgent = pAgent;
    return;
  }

  /** getUserAgent
   *
   * @return
   */
  public String getUserAgent() {
    return mUserAgent;
  }

  /** setRemoteHost
   * 
   * @param pHost
   */
  public void setRemoteHost( final String pHost ) {
    mRemoteHost = pHost;
    return;
  }

  /** getRemoteHost
   *
   * @return
   */
  public String getRemoteHost() {
    return mRemoteHost;
  }

  /** setThisNode
   *
   * @param pNode
   * @throws java.lang.Exception
   */
  public void setThisNode( final Node pNode ) {
    mThisNode = pNode;
    return;
  }

  /** getThisNode
   *
   * @return
   */
  public Node getThisNode() {
    return mThisNode;
  }

  /** setRange
   * 
   * @param pFrom
   * @param pTo 
   */
  public void setRange( final long pFrom, final long pTo ) {
    mRangeFrom = pFrom;
    mRangeTo   = pTo;
    return;
  }
  
  public long getRangeFrom() {
    return mRangeFrom;
  }
  
  public long getRangeTo() {
    return mRangeTo;
  }

  
  public void setThisProperty( final Property pProperty ) {
    mThisProperty = pProperty;
    return;
  }

  /** getThisProperty
   *
   * @return
   */
  public Property getThisProperty() {
    return mThisProperty;
  }

  /** getETag
   *
   * @return
   * @throws RepositoryException
   */
  public String getETag() throws RepositoryException {
    final StringBuilder etag = new StringBuilder();
    if (mThisNode!=null) {
      final DateTime dtime = new DateTime( getLastModified() );
      etag.append( '"' ).append( mThisNode.getPath() );
      etag.append( '|' ).append( getUsername() );
      etag.append( '|' ).append( dtime.toStringRfc822() );
      etag.append( '"' );
    }
    return etag.toString();
  }

  /** setCreatedNode
   *
   * @param pNode
   */
  public void setCreatedNode( Node pNode ) {
    mCreatedNode = pNode;
    return;
  }

  /** getCreatedNodePath
   * 
   * @return
   * @throws javax.jcr.RepositoryException
   */
  public String getCreatedNodePath() throws RepositoryException {
    if (mCreatedNode!=null) {
      return mCreatedNode.getPath();
    }
    return null;
  }


  @Deprecated
  public Node getCreatedNode() {
    return mCreatedNode;
  }
  
  public void addDeletedNodePath( String pPath ) {
    if (mDeletedNodePaths==null) mDeletedNodePaths = new Vector();
    mDeletedNodePaths.add( pPath );
    return;
  }
  
  private String createHeader( String pAlt, boolean pIsFeed ) {
    String header = null;
    if (pAlt.startsWith( "atom" )) {
      if (pAlt.startsWith( "atomsvc")) {
        header = getServiceHeader();          
        setContentType( "application/atomsvc+xml" );
      } else {
        if (pIsFeed==true) {
          header = getFeedHeader();
        } else {
          header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
        }
        setContentType( "application/atom+xml" );
      }
    } else if (pAlt.equals( "simple" )) {
      header = "";
      setContentType( "text/plain" );
    }
    return header;
  }

  private String createFooter( String pAlt, boolean pIsFeed ) {
    String header = null;
    if (pAlt.startsWith( "atom" )) {
      if (pAlt.startsWith( "atomsvc")) {
        header = "</service>\n";
      } else {
        if (pIsFeed==true) {
          header = "</feed>\n";
        } else {
          header = "";
        }
      }
    } else if (pAlt.equals( "simple" )) {
      header = "";
    } else if (pAlt.startsWith( "html" )) {
      header = "</body></html>\n";
    }
    return header;
  }
  

  
  static public String htmlEscape(String s) {
    StringBuilder sb = null;
    String replacement;
    int start = 0;
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '"':
          replacement = "&quot;";
          break;
        case '&':
          replacement = "&amp;";
          break;
        case '<':
          replacement = "&lt;";
          break;
        case '>':
          replacement = "&gt;";
          break;
        default:
          replacement = null;
      }
      if (replacement != null) {
        if (sb == null) {
          sb = new StringBuilder(s.length() + replacement.length() - 1);
        }
        if (i > start) {
          sb.append(s.substring(start, i));
        }
        sb.append(replacement);
        start = i+1;
      }
    }
    if (start > 0) {
      sb.append(s.substring(start));
    }
    if (sb != null) {
      return sb.toString();
    }
    return s;
  }

  
  /** transportString
   *
   * @param pS
   * @return
   * @throws UnsupportedEncodingException
   */
  static public String transportString( final String pS ) throws UnsupportedEncodingException {
    return CARS_Utils.encode( pS );
//    return URLEncoder.encode( pS, "UTF-8" );
  }

  /** untransportString
   *
   * @param pS
   * @return
   * @throws UnsupportedEncodingException
   */
  static public String untransportString( final String pS ) throws UnsupportedEncodingException {
    if (gUntransport) return URLDecoder.decode( pS, "UTF-8" );
    return pS;
  }

  /** getPublishedDate
   *
   * @param pNode
   * @return
   * @throws Exception
   */
  public String getPublishedDate( final Node pNode ) throws Exception {
    String pub;
    if (pNode.hasProperty( gDefPublished )) {
      pub = pNode.getProperty( gDefPublished ).getString();        
    } else if (pNode.hasProperty( "jcr:created" )) {
      pub = pNode.getProperty( "jcr:created" ).getString();
    } else {
      pub = ISO8601.format(Calendar.getInstance());
    }
    return pub;    
  }

  /** getUpdatedDate
   *
   * @param pNode
   * @return
   * @throws Exception
   */
  public String getUpdatedDate( final Node pNode ) throws Exception {
    String upd;
    if (pNode.hasProperty( DEF_MODIFIED )) {
      upd = pNode.getProperty( DEF_MODIFIED ).getString();
    } else {
      upd = getPublishedDate( pNode );
    }
    return upd;    
  }

  /** getPublishedDateTime
   * 
   * @param pNode
   * @return
   * @throws java.lang.Exception
   */
  public long getPublishedDateTime( final Node pNode ) throws RepositoryException {
    if (pNode.hasProperty( gDefPublished )) {
      return pNode.getProperty( gDefPublished ).getDate().getTimeInMillis();
    } else if (pNode.hasProperty( "jcr:lastModified" )) {
      return pNode.getProperty( "jcr:lastModified" ).getDate().getTimeInMillis();
    } else if (pNode.hasProperty( "jcr:created" )) {
      return pNode.getProperty( "jcr:created" ).getDate().getTimeInMillis();
    } else {
      return 0L;
    }
  }

  /** getUpdatedDateTime
   * 
   * @param pNode
   * @return
   * @throws java.lang.Exception
   */
  public long getUpdatedDateTime( Node pNode ) throws RepositoryException {
    if (pNode.hasProperty( DEF_MODIFIED )) {
      return pNode.getProperty( DEF_MODIFIED ).getDate().getTimeInMillis();
    } else {
      return getPublishedDateTime( pNode );
    }
  }
  
  public String getAction() {
    return mAction;
  }
  
  public void setAction( String pAction ) {
    mAction = pAction;
  }
 
  /** addPropertyValue
   * 
   * @param pBuilder
   * @param pPropName
   * @param pNode
   * @param pValue
   * @param pAlt
   * @throws Exception 
   */
  private void addPropertyValue( final CARS_Buffer pBuilder, String pPropName, Node pNode, Value pValue, String pAlt ) throws Exception {
    switch( pValue.getType() ) {
      case PropertyType.BINARY: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + transportString( pValue.getString() ) + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getString() + "\n" );            
          }
          break;
      }
      case PropertyType.BOOLEAN: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + pValue.getBoolean() + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getBoolean() + "\n" );
          }
          break;
      }
      case PropertyType.DATE: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + pValue.getString() + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getString() + "\n" );
          }
          break;
      }
      case PropertyType.DOUBLE: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + pValue.getDouble() + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getDouble() + "\n" );
          }
          break;
      }
      case PropertyType.LONG: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + pValue.getLong() + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getLong() + "\n" );
          } else if (pAlt.equals( "html_edit" )) {
            pBuilder.append( pPropName + " = <INPUT type=\"text\" name=\"" + pPropName + 
                             "\" value=\"" + pValue.getString() + "\"><BR>\n" );
          }
          break;
      }
      case PropertyType.NAME: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + transportString( pValue.getString() ) + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getString() + "\n" );
          }
          break;
      }
      case PropertyType.PATH: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + transportString( pValue.getString() ) + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getString() + "\n" );
          }
          break;
      }
      case PropertyType.REFERENCE: {
          try {
            if (pAlt.startsWith( "atom" )) {
              pBuilder.append( "<" + pPropName + " uuid=\"" + transportString( pValue.getString() ) + "\">" +
                                 mBaseURL + mContextPath + pNode.getSession().getNodeByUUID(pValue.getString()).getPath() +
                               "</" + pPropName + ">\n" );
            } else if (pAlt.equals( "simple" )) {
              pBuilder.append( "  " + pPropName + " = " + mBaseURL + mContextPath + pNode.getSession().getNodeByUUID(pValue.getString()).getPath() + "\n" );
            }
          } catch (ItemNotFoundException infe) {
            // **** No right for item. skip it
          }
          break;
      }
      case PropertyType.STRING: {
          if (pAlt.startsWith( "atom" )) {
            pBuilder.append( "<" + pPropName + ">" + pValue.getString() + "</" + pPropName + ">\n" );
          } else if (pAlt.equals( "simple" )) {
            pBuilder.append( "  " + pPropName + " = " + pValue.getString() + "\n" );
          } else if (pAlt.equals( "html_edit" )) {
            pBuilder.append( pPropName + " = <INPUT type=\"text\" name=\"" + pPropName + 
                             "\" value=\"" + pValue.getString() + "\"><BR>\n" );
          }
          break;
      }
    }
    return;
  }

  /** setCalenderToNowParameter
   * @param pCal
   * @param pNow
   * @return
   */
  static final private Calendar setCalenderToNowParameter( Calendar pCal, String pNow ) {
    char unit = pNow.charAt( pNow.length()-1 );
    String value = pNow.substring( 3, pNow.length()-1 );
    switch( unit ) {
        case 's': {
          pCal.add( Calendar.SECOND, Integer.parseInt( value ) );
          break;
        }
        case 'm': {
          pCal.add( Calendar.MINUTE, Integer.parseInt( value ) );
          break;
        }
        case 'h': {
          pCal.add( Calendar.HOUR, Integer.parseInt( value ) );
          break;
        }
        case 'd': {
          pCal.add( Calendar.DAY_OF_YEAR, Integer.parseInt( value ) );
          break;
        }
      }
    return pCal;
  }
  
  /** Get date from parameter, include the "now" search
   * @param Date string
   * @return ISO8601 date string
   */
  static final private String getDateFromParam( String pDate ) {
    if (pDate.startsWith( "now" )) {
      Calendar cal = Calendar.getInstance();
      cal = setCalenderToNowParameter( cal, pDate );
      pDate = ISO8601.format(cal);
    }
    return pDate;
  }
  
  /** Get calendar data from string
   * @param pDate ISO8601
   * @return calendar
   * @throws java.text.ParseException
   */
  static final public Calendar getCalendarFromString( String pDate ) throws ParseException {
    Calendar c = null;
//    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
    if (pDate.startsWith( "now" )) {
      Calendar cal = Calendar.getInstance();
      c = setCalenderToNowParameter( cal, pDate );
//      cal.add( Calendar.HOUR, Integer.parseInt( pDate.substring( 3 )) );
    } else {
      c = ISO8601.parse( pDate );
    }    
    if (c==null) throw new ParseException( pDate, 0 );
    return c;
  }

  /** checkAndConvert_Q
   * @param pQ
   * @return
   */
  static final public String checkAndConvert_Q( String pQ ) {
    String q = pQ;
    q = q.replace( '\'', '"' );
    return q;
  }
  
  /** checkAndConvert_order
   * @param pOrder
   * @return
   */
  static final public String checkAndConvert_order( final String pOrder ) throws Exception {
    String o = pOrder;
    if (o.indexOf( " " )!=-1) throw new Exception( "No spaces in ORDERBY allowed" );
    for( int i = 0; i<pOrder.length(); i++ ) {    
      if (Character.isLetter(o.charAt(i))==false) {
        if (o.charAt(i)!=':') throw new Exception( "illegal character: " + o.charAt(i) );
      }
    }
    return o;
  }

  /** checkAndConvert_ordertype
   * @param pOrder
   * @return
   */
  static final public String checkAndConvert_ordertype( final String pOrderType ) {
    final String o = pOrderType;
    if ((pOrderType.equalsIgnoreCase( "DESC" )) ||
        (pOrderType.equalsIgnoreCase( "ASC" ))) return o;
    throw new IllegalArgumentException( "[ORDERBYTYPE] Only DESC|ASC as values allowed" );
  }
 
  /** Retrieve the rangeiterator with possible filter and other...
   * @return The RangeIterator holding the results
   * @throws Exception when an error occurs.
   */
  protected RangeIterator getRangeIterator() throws Exception {
    RangeIterator ni = null;
    final JD_Taglist params = getQueryPartsAsTaglist();
    if (mustConstructQuery( params )) {
      // **** We must use the JCR QueryManager
      final StringBuilder query = new StringBuilder( "SELECT * FROM " );

      if (getCategoryFilter()!=null) {
        final String catF = getCategoryFilter();
        if (catF.indexOf( ':' )==-1) {
          query.append( DEF_NS );
        }
        query.append( catF );
      } else {
        query.append( "nt:base" );
      }      
      query.append( " WHERE " );

      String orderBy = "";
      
      if ((params.getData( gDefDeep )!=null) &&
          ((String)params.getData( gDefDeep )).compareToIgnoreCase( "true" )==0) {
        if ("/".equals( mThisNode.getPath() )) {
          query.append( " (jcr:path LIKE '%')" );
        } else {
          query.append( " (jcr:path LIKE '" ).append( mThisNode.getPath() ).append( "/%')" );
        }
      } else {
        query.append( " (jcr:path LIKE '" ).append( mThisNode.getPath() ).append( "/%' AND NOT jcr:path LIKE '" ).append( mThisNode.getPath() ).append( "/%/%')" );
      }

      String param = "?", pvalue;
      try {
        if ((pvalue=(String)params.getData( gDefCreatedMin ))!=null) {
          param = gDefCreatedMin;
          final String date = getDateFromParam( pvalue );
          query.append( " AND jcr:created >= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefCreatedMax )!=null) {
          param = gDefCreatedMax;
          final String date = getDateFromParam( (String)params.getData( gDefCreatedMax ) );
          query.append( " AND jcr:created <= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefUpdatedMin )!=null) {
          param = gDefUpdatedMin;
          final String date = getDateFromParam( (String)params.getData( gDefUpdatedMin ) );
          query.append( " AND jecars:Modified >= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefUpdatedMax )!=null) {
          param = gDefUpdatedMax;
          final String date = getDateFromParam( (String)params.getData( gDefUpdatedMax ) );
          query.append( " AND jecars:Modified <= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefPublishedMin )!=null) {
          param = gDefPublishedMin;
          final String date = getDateFromParam( (String)params.getData( gDefUpdatedMin ) );
          query.append( " AND jecars:Published >= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefPublishedMax )!=null) {
          param = gDefPublishedMax;
          final String date = getDateFromParam( (String)params.getData( gDefPublishedMax ) );
          query.append( " AND jecars:Published <= TIMESTAMP '" ).append( date ).append( "'" );
        }
        if (params.getData( gDefWhere )!=null) {
          param = gDefWhere;
          query.append( " AND (" ).append( (String)params.getData( gDefWhere ) ).append( ")" );
        }
      } catch (Exception e) {
        CARS_Factory.getEventManager().addException( mMain, mMain.getLoginUser(), mThisNode, null,
                    CARS_EventManager.EVENTCAT_URL, CARS_EventManager.EVENTTYPE_QUERY,
                    e, "query param error " + param + " = " + query );
        throw e;
      }

      if (params.getData( gDefKeywords )!=null) {
        query.append( " AND (jecars:Keywords LIKE '" ).append( (String)params.getData( gDefKeywords ) ).append( "')" );
      }
      
      if (params.getData( gDefFullText )!=null) {
        final String qtext = (String)params.getData( gDefFullText );
        query.append( " AND CONTAINS(*, '" ).append( checkAndConvert_Q(qtext) ).append( "')" );
        orderBy = " ORDER BY jcr:score DESC";
      }

      if (params.getData( gDefOrderBy )!=null) {
        String order = (String)params.getData( gDefOrderBy );
        orderBy = " ORDER BY " + checkAndConvert_order( order );
        if (params.getData( gDefOrderByType )!=null) {
          order = (String)params.getData( gDefOrderByType );
          orderBy += " " + checkAndConvert_ordertype( order );
        }
      }
      
      query.append( orderBy );
      
      if ((pvalue=(String)params.getData( gDefGQL ))!=null) {
        // **** Google Query Language (Jackrabbit v1.5 and higher)
//        final RowIterator ri = GQL.execute( pvalue, mThisNode.getSession(), mThisNode.getPath() );
//        ni = ri;
      } else {
        // **** Do normal query call
        try {
          final Query q = mThisNode.getSession().getWorkspace().getQueryManager().createQuery( query.toString(), Query.SQL );
          ni = q.execute().getNodes();
          CARS_Factory.getEventManager().addEventThreaded( mMain, mMain.getLoginUser(), mThisNode, null,
                    CARS_EventManager.EVENTCAT_URL, CARS_EventManager.EVENTTYPE_QUERY,
                    "JCR SQL query = " + query + " result = " + ni.getSize() );
        } catch (Exception e) {
          CARS_Factory.getEventManager().addException( mMain, mMain.getLoginUser(), mThisNode, null,
                    CARS_EventManager.EVENTCAT_URL, CARS_EventManager.EVENTTYPE_QUERY,
                    e, "JCR SQL query = " + query );
          throw e;
        }
      }
    } else if (getQueryString()!=null) {
      
      // **************************************************
      // **** There is a query string sql=... or xpath=....
//      JD_Taglist paramsTL = getQueryPartsAsTaglist();
      JD_Taglist paramsTL = params;
      if (paramsTL.getData( gDefXPath )!=null) {
        String query = (String)paramsTL.getData( gDefXPath );
        CARS_Factory.getEventManager().addEvent( mMain, mMain.getLoginUser(), mThisNode, null, "URL", "QUERY",
                query );
        Query q = mThisNode.getSession().getWorkspace().getQueryManager().createQuery( 
                query,Query.XPATH );
        ni = q.execute().getNodes();        
      } else if (paramsTL.getData( gDefSQL )!=null) {
        String query = (String)paramsTL.getData( gDefSQL );
        CARS_Factory.getEventManager().addEvent( mMain, mMain.getLoginUser(), mThisNode, null, "URL", "QUERY",
                query );
        Query q = mThisNode.getSession().getWorkspace().getQueryManager().createQuery( 
                query,Query.SQL );
//        ni = q.execute().getNodes();
        ni = CARS_QueryManager.executeQuery( q ).getNodes();
      } else if (paramsTL.getData( gDefSQL2 )!=null) {
        String query = (String)paramsTL.getData( gDefSQL2 );
        CARS_Factory.getEventManager().addEvent( mMain, mMain.getLoginUser(), mThisNode, null, "URL", "QUERY",
                query );
        final Query q = mThisNode.getSession().getWorkspace().getQueryManager().createQuery( 
                query,Query.JCR_SQL2 );
        ni = CARS_QueryManager.executeQuery( q ).getNodes();
      } else if (paramsTL.getData( gDefQOM )!=null) {
        String query = (String)paramsTL.getData( gDefQOM );
        CARS_Factory.getEventManager().addEvent( mMain, mMain.getLoginUser(), mThisNode, null, "URL", "QUERY",
                query );
        final Query q = mThisNode.getSession().getWorkspace().getQueryManager().createQuery( 
                query,Query.JCR_JQOM );
        ni = CARS_QueryManager.executeQuery( q ).getNodes();
      } else if (paramsTL.getData( gDefReferences )!=null) {
        
        // ***********************************
        // **** Get the references
        PropertyIterator pi = mThisNode.getReferences();
        CARS_DefaultPropertyIterator dpi = new CARS_DefaultPropertyIterator();
        while( pi.hasNext() ) {
          Property p = pi.nextProperty();
          dpi.storeObject( p );
        }
        ni = dpi;

      } else if (paramsTL.getData( gDefVCSHistory )!=null) {

        // ****************************************
        // **** Get the version history of the node
        final StringBuilder sbvh = new StringBuilder();
        final CARS_VersionManager vm = getVersionManager( null );
        final List<String> history = vm.history( mThisNode );
        for( String his : history) {
          sbvh.append( his ).append( '\n' );
        }
        ByteArrayInputStream bais = new ByteArrayInputStream( sbvh.toString().getBytes() );
        setContentsResultStream( bais, "text/plain" );


      } else if (paramsTL.getData( gDefRights )!=null) {

        // ***********************************
        // **** Get the rights on this object
          final String object = (String)paramsTL.getData( gDefRights );
          final Node          rn = mThisNode.getSession().getRootNode().getNode( object.substring(1) );
          final Session sysSession = CARS_Factory.getSystemCarsSession();
          synchronized( sysSession ) {
            final QueryManager qm = sysSession.getWorkspace().getQueryManager();
            final List<Node> nodes = new ArrayList<Node>();
            CARS_Factory.getLastFactory().getAccessManager().fillPrincipalsForGroupMembers( qm, nodes, rn.getPath() );
          
            // **** Only the permissions on the same level
            // **** "SELECT * FROM jecars:permissionable WHERE (jecars:Principal='"
            final StringBuilder qu = new StringBuilder( CARS_Definitions.gSelectPermission );
            qu.append( rn.getPath() ).append( "'" );
            for (Node node : nodes) {
              qu.append( " OR " ).append( CARS_Definitions.gPrincipal ).append( "='" ).append( node.getPath() ).append( "'" );
            }
            qu.append( ")" );
            final Query q = qm.createQuery( qu.toString(), Query.SQL );
            final QueryResult qr = q.execute();
            final NodeIterator pni = qr.getNodes();
            Node n;
          
            final CARS_DefaultPropertyIterator dpi = new CARS_DefaultPropertyIterator();
            final JD_Taglist actions = new JD_Taglist();
          
            Property acts;
            while( pni.hasNext() ) {
              n = pni.nextNode();
              if (n.getParent().isSame( mThisNode.getParent() )) {
                // **** Permission on the parent level
                acts = n.getProperty( gDefActions );
                final Value[] vals = acts.getValues();
                for (Value value : vals) {
                  actions.replaceData( value.getString(), gDefActions );
                }
              } else if ((mThisNode.getPath().startsWith( n.getParent().getPath() )) &&
                         (n.hasProperty( "jecars:Delegate" )) && (n.getProperty( "jecars:Delegate" ).getBoolean())) {
//              dpi.storeObject( n.getProperty( "jecars:Actions" ) );
                acts = n.getProperty( gDefActions );
                final Value[] vals = acts.getValues();
                for (Value value : vals) {
                  actions.replaceData( value.getString(), gDefActions );
                }
              } else if (mThisNode.getPath().startsWith( n.getParent().getPath() )) {
                acts = n.getProperty( gDefActions );
                final Value[] vals = acts.getValues();
                for (Value value : vals) {
                  if (CARS_Definitions.P_ADDNODE.equals( value.getString() )) {
                    actions.replaceData( value.getString(), gDefActions );
                  }
                }
              }
            }
            dpi.storeObject( actions );
            ni = dpi;
          }
        
      } else if (paramsTL.getData( gDefChildNodeDefs )!=null) {

        // ***********************************
        // **** Get the child node definitions
        Node user = mThisNode.getSession().getRootNode().getNode( CARS_Definitions.gUsersPath  + "/" + mUsername );
        Node temp = user.getNode( CARS_Definitions.DEFAULTNS + "Temp" );
        NodeType nt =  mThisNode.getSession().getWorkspace().getNodeTypeManager().getNodeType( mThisNode.getProperty( "jcr:primaryType" ).getString() );
        NodeDefinition[] ndefs = nt.getChildNodeDefinitions();
        Node cntemp = temp.addNode( "childnodedefs_results_" + System.currentTimeMillis(), CARS_Definitions.DEFAULTNS + "datafoldermultiple" );
        Calendar c = Calendar.getInstance();
        c.add( Calendar.MINUTE, 5  ); // **** Temp data will be removed after 5 minutes
        cntemp.setProperty( gDefExpireDate, c );
        for (int i=0; i<ndefs.length; i++ ) {
          NodeType cnt[] = ndefs[i].getRequiredPrimaryTypes();
          for (int cni=0; cni<cnt.length; cni++ ) {
            Node n = cntemp.addNode( cnt[cni].getName(), CARS_Definitions.DEFAULTNS + "root" );
          }
        }
        temp.save();
        ni = cntemp.getNodes();
      } else {
        String pvalue;

        // **************************
        // **** Check for namePattern
        if ((pvalue=(String)params.getData( gDefNamePattern ))!=null) {
          ni = mThisNode.getNodes( pvalue );
        } else {
          ni = mThisNode.getNodes();
        }
      }
    } else {
      // **** No complex query needed
      ni = mThisNode.getNodes();
    }
    return ni;
  }

  /** getAtomID
   *
   * @param pNode
   * @return
   * @throws Exception
   */
  public String getAtomID( Node pNode ) throws Exception {
    try {
//      return "urn:uuid:" + pNode.getUUID();
//      return "urn:uuid:" + pNode.getPath();
      return "urn:uuid:" + pNode.getIdentifier();
    } catch (Exception e) {
      return transportString(pNode.getPath());
    }
  }
  
  /** Add a node according to the atom service protocol
   */
  private void addNodeEntry_atomsvc( final CARS_Buffer pReply, Node pNode, String pPath ) throws RepositoryException {
    // *****************************
    // **** Atom Service
    try {
      pReply.append( "<workspace>" );
      pReply.append( "<atom:title>ws_" + pNode.getName() + "</atom:title>\n" );            
      pReply.append( "<collection href=\"" + pPath + "?alt=atom_entry\">\n" );
      pReply.append( "<atom:title>" + pNode.getName() + "</atom:title>\n" );            
    } finally {
      pReply.append( "</collection>\n</workspace>" );
    }
    return;
  }
  
  /** Add node links
   */
  private void addNodeEntry_addlinks( boolean pSelf, final CARS_Buffer pReply, Node pThisNode, Node pNode,
                                      String pBasePath, String pPath, long pFromNode, long pToNode ) throws RepositoryException {
    String path;
    if (pPath==null) {
      path = pBasePath + pNode.getPath();
    } else {
      path = pBasePath + pPath;
    }
       
    if (pFromNode>1) {
        pReply.append( "<link rel=\"previous\" type=\"application/atom+xml\" href=\"" + path );
        long si = pFromNode-(pToNode-pFromNode);
        if (si<1) si = 1;
        pReply.append( "?max-results=" + (pToNode-pFromNode) + "%26start-index=" + si ); // TODO
        pReply.append( "\"/>\n" );
        pReply.append( "<link rel=\"next\" type=\"application/atom+xml\" href=\"" + path );
        pReply.append( "?max-results=" + (pToNode-pFromNode) + "%26start-index=" + (pToNode+1) ); // TODO
        pReply.append( "\"/>\n" );
    }
    if (pSelf==true) {
      pReply.append( "<link rel=\"self\" type=\"application/atom+xml\" href=\"" + path + "\"/>\n" );
    }
    pReply.append( "<link rel=\"alternate\" type=\"application/atom+xml\" href=\"" + path + "\"/>\n" );
    try {
      if ((pThisNode!=null) && (pNode!=null)) {
        pThisNode.getSession().checkPermission( pNode.getPath(), "set_property" );
        pReply.append( "<link rel=\"edit\" type=\"application/atom+xml\" href=\"" + path + "\"/>\n" );
      }
    } catch (AccessControlException ace) {              
    }
    return;
  }

  /** Opensearch items
   */
  private void addNodeEntry_opensearch( final CARS_Buffer pReply, Node pNode, long pFromNode, long pToNode ) {
    if (pFromNode>0) {
      long totresults = (pToNode-pFromNode)+1;
      pReply.append( "<openSearch:totalResults>" ).append( totresults ).append( "</openSearch:totalResults>\n" );
      pReply.append( "<openSearch:startIndex>" ).append( pFromNode ).append( "</openSearch:startIndex>\n" );
    }
    return;
  }

  
  /** Add a node entry
   * 
   * @param pReply
   * @param pNode
   * @param pPath
   * @param pAlt
   * @param pSelf
   * @param pIsFeed
   * @param pFromNode
   * @param pToNode
   * @param pNodeNo
   * @param pOutputGen
   * @throws java.lang.Exception
   */
  private void addNodeEntry( final CARS_Buffer pReply, final Node pNode, String pPath,
                             final String pAlt, final boolean pSelf, final boolean pIsFeed,
                             final long pFromNode, final long pToNode, final long pNodeNo, final CARS_OutputGenerator pOutputGen ) throws Exception {
    
    Node thisNode = mThisNode;
    if (thisNode==null) thisNode = mCreatedNode;

    if ((pPath==null) && (pNode!=null)) pPath = pNode.getPath();
    
    if (pOutputGen!=null) {
      if (pSelf) {
        final JD_Taglist tags = getQueryPartsAsTaglist();
        if (tags.getData( gDefThisNode )!=null) {
          final String v = (String)tags.getData( gDefThisNode );
//          if (v.equals( "not" )==false) {
          if (!"not".equals( v )) {
            pOutputGen.addThisNodeEntry( this, pReply, pNode, pFromNode, pToNode );
          }
        } else {
         pOutputGen.addThisNodeEntry( this, pReply, pNode, pFromNode, pToNode );
        }
      } else {
        pOutputGen.addChildNodeEntry( this, pReply, pNode, pNodeNo );
      }
      return;
    }
    
    if (pAlt.startsWith( "atom" )) {
      String basePath = mBaseURL + mContextPath;
      if (pAlt.startsWith( "atomsvc" )) {
        // *****************************
        // **** Atom Service
        addNodeEntry_atomsvc( pReply, pNode, pPath);
        return;
      }
      
      // *****************************
      // **** Atom
      if ((!pSelf) || (!pIsFeed)) {
        pReply.append( "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:jecars=\"http://jecars.org/\">\n" );
      }
      try {
        if (pNode!=null) {
          pReply.append( "<title>" + pNode.getName() + "</title>\n" );            
          if (pNode.hasProperty( gDefTitle )) {
            pReply.append( "<subtitle>" + transportString(pNode.getProperty( gDefTitle ).getString()) + "</subtitle>\n" );
          }
        }
        pReply.append( "<author><name>" + CARS_Definitions.DEFAULTAUTHOR + "</name></author>\n");
        pReply.append( "<rights>" + CARS_Definitions.DEFAULTRIGHTS + "</rights>\n");
//        if (pSelf==true) {
//          addNodeEntry_addlinks( pSelf, pReply, pNode, mThisNode, basePath, pPath, pFromNode, pToNode );
//        } else {
        addNodeEntry_addlinks( pSelf, pReply, pNode, mThisNode, basePath, pPath, pFromNode, pToNode );
//        }
        if (pNode!=null) {
          pReply.append( "<published>" + getPublishedDate(pNode) + "</published>\n" );
          pReply.append( "<updated>"   + getUpdatedDate(pNode) + "</updated>\n" );
          pReply.append( "<id>" + getAtomID(pNode) + "</id>\n" );
          pReply.append( "<category term='" + pNode.getPrimaryNodeType().getName() + "'/>\n" );
          if (pNode.hasProperty( gDefBody )==true) {
            pReply.append( "<content type=\"html\"><![CDATA[<p>" );
            pReply.append( pNode.getProperty( gDefBody ).getString() );
            pReply.append( "</p>]]></content>\n" );
          }
          addNodeEntry_opensearch( pReply, pNode, pFromNode, pToNode );
        }
        
        if (pSelf) {
          // **** Add the properties as result
          PropertyIterator pi = thisNode.getProperties();          
          addMultiPropertyEntry( pReply, thisNode, pi, pAlt );
        }

        
      } finally {
        if ((!pSelf) || (!pIsFeed)) {
          pReply.append( "</entry>\n" );
        }
      }
    } else if ("simple".equals( pAlt )) {
      // *****************************
      // **** Simple
      if (pNode!=null) {
        if (pNode.hasProperty( gDefTitle )) {
          pReply.append( "title =" + pNode.getProperty( gDefTitle ).getString() + "\n" );
        } else {
          pReply.append( "title =" + pNode.getName() + "\n" );            
        }
      }
      if (pPath==null) {
        pReply.append( "  link      =" + mBaseURL + mContextPath + pNode.getPath() + "\n" );
      } else {
        pReply.append( "  link      =" + mBaseURL + mContextPath + pPath + "\n" );        
      }
      if (pNode!=null) {
        pReply.append( "  published =" + getPublishedDate(pNode) + "\n" );
        pReply.append( "  updated   =" + getUpdatedDate(pNode) + "\n" );
        pReply.append( "  id        =" + getAtomID(pNode) + "\n" );
        pReply.append( "  category  =" + pNode.getPrimaryNodeType().getName() + "\n" );
      }
      
      if (pSelf) {
        // **** Add the properties as result
        PropertyIterator pi = thisNode.getProperties();
        Property p;
        while( pi.hasNext() ) {
          p = pi.nextProperty();
          if (p.getName().startsWith( DEF_NS )) {
            if (p.getDefinition().isMultiple()) {
              Value[] vals = p.getValues();
              for( int val=0; val<vals.length; val++ ) {
                addPropertyValue( pReply, p.getName(), thisNode, vals[val], pAlt );
              }
            } else {
              addPropertyValue( pReply, p.getName(), thisNode, p.getValue(), pAlt );
            }
          }
        }
      }     
    } else if ("html_edit".equals( pAlt )) {
      // *****************************
      // **** HTML simple EDIT forms
      if (pNode.hasProperty( gDefTitle )) {
        pReply.append( "<H2>" + pNode.getProperty( gDefTitle ).getString() + "</H2>\n" );
      } else {
        pReply.append( "<H2>" + pNode.getName() + "</H2>\n" );
      }
      if (pSelf) {
        pReply.append( "<FORM action=\"" + mBaseURL + mContextPath + pNode.getPath() + "\" method=\"POST\">" );
        // **** Add the properties as result
        PropertyIterator pi = thisNode.getProperties();
        Property p;
        while( pi.hasNext() ) {
          p = pi.nextProperty();
          if (p.getName().startsWith( DEF_NS )) {
            if (p.getDefinition().isMultiple()) {
              Value[] vals = p.getValues();
              for( int val=0; val<vals.length; val++ ) {
                addPropertyValue( pReply, p.getName(), thisNode, vals[val], pAlt );
              }
            } else {
              addPropertyValue( pReply, p.getName(), thisNode, p.getValue(), pAlt );
            }
          }
        }
        pReply.append( "<INPUT type=\"submit\" value=\"Send\"> <INPUT type=\"reset\"></FORM>" );
      }
    }

    return;
  }


  /** Add a multi property entry
   */
  private void addMultiPropertyEntry( final CARS_Buffer pReply, Node pThisNode, PropertyIterator pIt, String pAlt ) throws Exception {    

    // **** Add the properties as result
    Property p;
    Object po;
    while( pIt.hasNext() ) {
      po = pIt.next();
      if (po instanceof Property) {
        p = (Property)po;
        if (p.getName().indexOf(':')!=-1) {
          String prefix = p.getName().substring( 0, p.getName().indexOf(':'));
          if (gIncludeNS.contains( prefix )) {
             if (p.getType()!=PropertyType.BINARY) {
               if (p.getDefinition().isMultiple()) {
                 pReply.append( "<" ) .append( p.getName() ).append( " multi=\"true\">\n" );
                 try {
                   Value[] vals = p.getValues();
                   for( int val=0; val<vals.length; val++ ) {
                     addPropertyValue( pReply, p.getName(), pThisNode, vals[val], pAlt );
                   }
                 } finally {
                   pReply.append( "</" ).append( p.getName() ).append( ">\n" );
                 }
               } else {
                 addPropertyValue( pReply, p.getName(), pThisNode, p.getValue(), pAlt );
               }
             }
           }
         }
       } else if (po instanceof JD_Taglist) {
         JD_Taglist tags = (JD_Taglist)po;
         Iterator it = tags.getIterator();
         String key;
         while( it.hasNext() ) {
           key = (String)it.next();
           pReply.append( "<" ).append( String.valueOf(tags.getData( key )) ).append( ">" ).append( key
                   ).append( "</" ).append( String.valueOf(tags.getData( key ))).append( ">\n" );
         }
       }
     }
     return;
   }
  
  
  /** Create the reply for a POST (create object) message
   */
  private void createCreateNodeResult( final CARS_Buffer pReply ) {
    String alt = "atom";
    try {
      JD_Taglist params = getQueryPartsAsTaglist();
      if (params.getData( gDefAlt )!=null) alt = (String)params.getData( gDefAlt );
      pReply.append( createHeader( alt, false ) );      
      addNodeEntry( pReply, mCreatedNode, null, alt, true, false, 0L, 0L, 0L, null );
    } catch( Exception e ) {
      setErrorCode( HttpURLConnection.HTTP_BAD_REQUEST );
      setError( e );
      createError( pReply );
      LOG.log( Level.INFO, null, e );
    } finally {
      pReply.append( createFooter( alt, false ) );
    }
    return;
  }

  /** Create the reply for a DELETE (delete object) message
   */
  private void createDeleteNodeResult( final CARS_Buffer pReply ) {
    String alt = "atom";
    try {
      JD_Taglist params = getQueryPartsAsTaglist();
      if (params.getData( gDefAlt )!=null) alt = (String)params.getData( gDefAlt );
      pReply.append( createHeader( alt, false ) );
      Iterator it = mDeletedNodePaths.iterator();
      while( it.hasNext() ) {
        String p = (String)it.next();
        addNodeEntry( pReply, null, p, alt, false, false, 0L, 0L, 0L, null );
      }
    } catch( Exception e ) {
      setErrorCode( HttpURLConnection.HTTP_BAD_REQUEST );
      setError( e );
      createError( pReply );
      LOG.log( Level.INFO, null, e );
    } finally {
      pReply.append( createFooter( alt, false ) );
    }
    return;
  }

  
  /** Create the reply for a GET message
   *
   * @param pReply
   */
  private void createGetNodesResult( final CARS_Buffer pReply ) {
    String alt = "atom";
    boolean isFeed = true;
    long maxResult = MAX_NO_GETOBJECTS;
    CARS_OutputGenerator outputGen = null;
    try {
      final JD_Taglist params = getQueryPartsAsTaglist();
      if (params.getData( gDefAlt )!=null) alt = (String)params.getData( gDefAlt );
      outputGen = (CARS_OutputGenerator)gOutputGenerators.getData( alt );
      
      if (outputGen!=null) {
        outputGen.createHeader( this, pReply );
      } else {
        if ("atom_entry".equals( alt ) || "atomsvc".equals( alt )) isFeed = false;
        pReply.append( createHeader( alt, isFeed ) );
      }
      
      // **** Get result nodes
      long fromNode = 0L, toNode = maxResult;
//      boolean paging = false;
      final RangeIterator getNodesResult = getRangeIterator();
      if (getNodesResult!=null) {
        if (getNodesResult.getSize()>maxResult) {
          fromNode = 1L;
//          paging = true;
        } else {
          toNode = getNodesResult.getSize();
        }
      }
      
      // **** Check for start-index
      if (params.getData( gDefStartIndex )!=null) {
        try {
          fromNode = Long.parseLong( (String)params.getData( gDefStartIndex ) );
        } catch (NumberFormatException nfe) {
          setErrorCode( HttpURLConnection.HTTP_BAD_REQUEST );
          setError( nfe );
          createError( pReply );
          return;
        }
        
      }            
      
      // **** Check for paging (max-results)
      if (params.getData( gDefMaxResults )!=null) {
        try {
          final long maxresults = Long.parseLong( (String)params.getData( gDefMaxResults ) );
          if (maxResult>maxresults) {
            maxResult = maxresults;
            if (fromNode==0L) fromNode = 1L;
            toNode = (fromNode+maxResult)-1;
//            paging = true;
          }
        } catch (NumberFormatException nfe) {
          setErrorCode( HttpURLConnection.HTTP_BAD_REQUEST );
          setError( nfe );
          createError( pReply );
          return;
        }
      }
            
      // **** Add the node entries
      if ((mThisNode!=null) && (!"atomsvc".equals( alt ))) {
        addNodeEntry( pReply, mThisNode, null, alt, true, isFeed, fromNode, toNode, 0L, outputGen );
      }
      if (!"atom_entry".equals( alt )) {
        if (getNodesResult instanceof NodeIterator) {
          final NodeIterator ni = (NodeIterator)getNodesResult;

          int count = 0;
          while( ni.hasNext() ) {
            count++;
            if (count>maxResult) break;
            final Node n = ni.nextNode();
            addNodeEntry( pReply, n, null, alt, false, isFeed, 0L, 0L, count, outputGen );
          }
          // **** TODO, Issue #1, Result paging implementation
          // **** Skipping to start-index
      /*
          if ((fromNode-1)<ni.getSize()) {
            if ((fromNode-1)>0) ni.skip( fromNode-1 );
            int count = 0;
            while( ni.hasNext() ) {
              count++;
              if (count>maxResult) break;
              final Node n = ni.nextNode();
              addNodeEntry( pReply, n, null, alt, false, isFeed, 0L, 0L, count, outputGen );
            }
          }
       *
       */
       } else if (getNodesResult instanceof RowIterator) {
          final RowIterator ri = (RowIterator)getNodesResult;

          int count = 0;
          while( ri.hasNext() ) {
            count++;
            if (count>maxResult) break;
            final Row n = ri.nextRow();
            addNodeEntry( pReply, n.getNode(), null, alt, false, isFeed, 0L, 0L, count, outputGen );
          }
        } else if (getNodesResult instanceof PropertyIterator) {
          final PropertyIterator pi = (PropertyIterator)getNodesResult;
          addMultiPropertyEntry( pReply, mThisNode, pi, alt );
        }
      }
    } catch( Exception e ) {
      setErrorCode( HttpURLConnection.HTTP_BAD_REQUEST );
      setError( e );
      createError( pReply );
//      LOG.log( Level.WARNING, null, e );
    } finally {
      if (outputGen==null) {
        pReply.append( createFooter( alt, isFeed ) );
      } else {
        outputGen.createFooter( this, pReply );
      }
    }
    return;
  }

  /** createError
   *
   * @param pReply
   */
  private void createError( final CARS_Buffer pReply ) {
    setContentType( "application/xml" );
    pReply.append( "<?xml version=\"1.0\" encoding='utf-8'?>\n" );
    pReply.append( "<error errorCode=\"" + getErrorCode() + "\">\n" );
    pReply.append( "<![CDATA[" );
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    mError.printStackTrace(pw);
    pReply.append( sw.getBuffer().toString() );
    pReply.append( "]]>\n" );
    pReply.append( "</error>\n" );
    return;
  }

  private String _getContentType() {
    if (mError!=null) return "text/xml";
    try {
      if (mThisNode!=null) {
        if (mThisNode.hasProperty( "jcr:mimeType" )) {
          return mThisNode.getProperty( "jcr:mimeType" ).getString();
        }
      }

    } catch (Exception e) {
      LOG.log( Level.SEVERE, null, e );
    }
    return "application/atom+xml";
  }
  
  public void setContentType( String pType ) {
    mResultContentsType = pType;
    return;
  }
  
  public String getContentType() {
    if (mResultContentsType==null) {
      mResultContentsType = _getContentType();

    }
    return mResultContentsType;
  }

  /** prepareResult
   *
   */
  public void prepareResult() throws CARS_LongPollRequestException {
    if (mThisNode!=null) {
      try {
        final String longpoll = getParameterStringFromMap( "longpoll" );
        if ((longpoll!=null) && ("true".equalsIgnoreCase(longpoll))) {
          // **** 
          throw new CARS_LongPollRequestException( mThisNode.getPath() );
        } else {
          if (mThisNode.isNodeType( "jecars:datafile" )) {
            mCanBeCachedResult = true;
          }
        }
      } catch( RepositoryException re ) {
        LOG.log( Level.WARNING, re.getMessage(), re );
      }
    }
    return;
  }
   
  /** setBinaryResult
   * 
   * @param pResponseHeaders
   * @param pParams
   * @param pBinary
   * @throws RepositoryException
   * @throws IOException
   * @throws NoSuchAlgorithmException 
   */
  private void setBinaryResult( final JD_Taglist pResponseHeaders, final JD_Taglist pParams, final Binary pBinary ) throws RepositoryException, IOException, NoSuchAlgorithmException {
    final JD_Taglist tags = getParameterMapAsTaglist(null);
    final InputStream is = pBinary.getStream();
    if ((tags.getData( gDefStartIndex )!=null) && (tags.getData( gDefMaxResults )!=null)) {
      // **** Start index, max results
      final long start = Long.parseLong( (String)pParams.getData( gDefStartIndex ) );
      final long len   = Long.parseLong( (String)pParams.getData( gDefMaxResults ) );
      is.skip( start);
      final byte[] data = new byte[(int)len];
      is.read( data );
      is.close();
      byte[] digest = MessageDigest.getInstance( "MD5" ).digest( data );
      pResponseHeaders.putData( "Content-Length", String.valueOf(len) );
      pResponseHeaders.putData( "Content-Range", "bytes " + start + "-" + (start+len-1) + "/" + pBinary.getSize() );
      pResponseHeaders.putData( "Content-MD5", BASE64Encoder.encodeBuffer( digest ) );
      mResultContentsStream = new ByteArrayInputStream( data );
      setErrorCode( HttpURLConnection.HTTP_PARTIAL );
    } else if ((getRangeFrom()>=0) && (getRangeTo()>=0)) {
      // **** HTTP 1.1 range
      final long len;
      final long start = getRangeFrom();
      if (getRangeTo()==Long.MAX_VALUE) {
        len = pBinary.getSize()-start;
      } else {
        len = (getRangeTo()-getRangeFrom())+1;
      }
      if (is.skip( start )==start) {
        
        final byte[] data = new byte[(int)len];
        int rd = is.read( data );
        if (rd==len) {
          byte[] digest = MessageDigest.getInstance( "MD5" ).digest( data );
          pResponseHeaders.putData( "Content-Length", String.valueOf(len) );
          pResponseHeaders.putData( "Content-Range", "bytes " + start + "-" + (start+len-1) + "/" + pBinary.getSize() );
          pResponseHeaders.putData( "Content-MD5", BASE64Encoder.encodeBuffer( digest ) );
          mResultContentsStream = new ByteArrayInputStream( data );
          setErrorCode( HttpURLConnection.HTTP_PARTIAL );
        } else {
          setErrorCode( 416 ); // **** Requested Range Not Satisfiable
        }
      } else {
        setErrorCode( 416 ); // **** Requested Range Not Satisfiable
      }
      is.close();
    } else {
      pResponseHeaders.putData( "Content-Length", String.valueOf(pBinary.getSize()) );
      mResultContentsStream = is;
    }
    return;
  }

  /** getResultObject
   * 
   * @param pNode
   * @return
   * @throws RepositoryException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws Exception 
   */
  public Object getResultObject( final Node pNode ) throws RepositoryException, IOException, NoSuchAlgorithmException, Exception {
    return getResultObject( new JD_Taglist(), new JD_Taglist(), null, pNode );
  }

  /** getResultObject
   * 
   * @param pParams
   * @param pResponseHeaders
   * @param pProperty
   * @param pNode
   * @return
   * @throws RepositoryException
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws Exception 
   */
  public Object getResultObject(
          final JD_Taglist pParams,
          final JD_Taglist pResponseHeaders,
          final Property pProperty,
          final Node pNode ) throws RepositoryException, IOException, NoSuchAlgorithmException, Exception {
    setThisNode( pNode );
    setThisProperty( pProperty );
    Object result = null;
    String alt = null;
    if (pParams.getData( gDefAlt )!=null) {
      alt = (String)pParams.getData( gDefAlt );
    }
    if (pProperty!=null) {

      // **************************************
      // **** The result of a property is asked
      switch( pProperty.getType() ) {
        case PropertyType.BINARY: {
          setBinaryResult( pResponseHeaders, pParams, pProperty.getBinary() );
          mResultContentsType = "jcr/binary";
          setContentType( "jcr/binary" );
          result = mResultContentsStream;
          break;
        }
        default:
          final StringBuilder replyString = new StringBuilder(pProperty.getValue().getString());
          setContentType( "text/plain" );
          result = replyString;
      }         
          
    } else if (pNode!=null) {
          
      Node thisNode = pNode;
          
      // **********************
      // **** jecars:Link check
      if ((alt==null) && thisNode.hasProperty( gDefLink )) {
        thisNode = CARS_Utils.getLinkedNode( getMain(), thisNode );
      }

      // *************************************  
      // **** URL check
      if ((thisNode.isNodeType( gDefURLResource )) && (alt==null) && (mResultContentsStream==null) &&
         ((thisNode.hasProperty(gDefURL)) || (thisNode.hasProperty("jcr:data")) ||
          (thisNode.hasProperty("jecars:PathToDatafile"))) ) {
        if (thisNode.hasProperty(gDefURL)) {
          final String urlString = thisNode.getProperty( gDefURL ).getString();
          final URL url = new URL( urlString );
          final URLConnection urlc = url.openConnection();
          setContentsLength( urlc.getContentLength() );
          result = urlc.getInputStream();
          if (thisNode.hasProperty( "jcr:mimeType" )) {
            setContentType( thisNode.getProperty( "jcr:mimeType" ).getString() );                  
          } else {
            setContentType( CARS_Mime.getMIMEType( urlString, null ) );
          }
        } else {
          Binary bin = null;
          if (thisNode.hasProperty("jecars:PathToDatafile")) {
            // **** A link to a jecars:datafile node
            final String path = thisNode.getProperty( "jecars:PathToDatafile" ).getValue().getString();
            bin = getMain().getSession().getNode( path ).getProperty( "jcr:data" ).getBinary();
          } else if (thisNode.hasProperty( "jecars:PathToFile" )) {
            // **** PathToFile
            boolean read = true;
            if (thisNode.hasProperty( "jecars:Partial" )) {
              read = !thisNode.getProperty( "jecars:Partial" ).getBoolean();
            }
            if (read) {
              final File f = new File( thisNode.getProperty( "jecars:PathToFile" ).getString() );
              if (f.canRead()) {
                bin = thisNode.getSession().getValueFactory().createBinary( new FileInputStream(f) );
              }
            }
          }
          // **************************
          // ***** Binary
          if (bin==null) {
            bin = thisNode.getProperty( "jcr:data" ).getBinary();
          }
          setBinaryResult( pResponseHeaders, pParams, bin );
          result = mResultContentsStream;
          if (thisNode.hasProperty( "jcr:mimeType" )) {
            setContentType( thisNode.getProperty( "jcr:mimeType" ).getString() );              
          } else {
            setContentType( CARS_Mime.getMIMEType( thisNode.getName(), null ) );
          }
        }
      } else {
        // **** Result stream set?
        if ((mResultContentsStream!=null) && (alt==null)) {
          result = mResultContentsStream;
          setContentType( mResultContentsType );
        } else {
          CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
          createGetNodesResult( buffer );
          if (mError!=null) {
            buffer = new CARS_Buffer( new StringBuilder() );
            createError( buffer );
            result = buffer;
          } else {
            if ((mResultContentsStream!=null) && (alt==null)) {
              result = mResultContentsStream;
              setContentType( mResultContentsType );
            } else {
              result = buffer;
            }
          }
        }
      }
    } else if (mCreatedNode!=null) {
      // **** Result stream set?
      if ((mResultContentsStream!=null) && (alt==null)) {
        result = mResultContentsStream;
        setContentType( mResultContentsType );
      } else {
        // **** A new node has been created
        CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
        createCreateNodeResult( buffer );
        if (mError!=null) {
          buffer = new CARS_Buffer( new StringBuilder() );
          createError( buffer );
        }
        result = buffer;
      }
    } else if (mDeletedNodePaths!=null) {
      // **** Node(s) have been deleted
      CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
      createDeleteNodeResult( buffer );
      if (mError!=null) {
        buffer = new CARS_Buffer( new StringBuilder() );
        createError( buffer );
      }
      result = buffer;
    }
    return result;
  }
  
  /** Get result
   * 
   * @param pResponseHeaders
   * @return 
   */
  @Deprecated
  public Object getResult( final JD_Taglist pResponseHeaders ) {
    Object result = null;
    try {
      if (mError!=null) {
        final CARS_Buffer replyBuf = new CARS_Buffer( new StringBuilder() );
        createError( replyBuf );
        result = replyBuf;
      } else {
        String alt = null;
        final JD_Taglist params = getQueryPartsAsTaglist();
        if (params.getData( gDefAlt )!=null) {
          alt = (String)params.getData( gDefAlt );
        }
        if (mThisProperty!=null) {

          // **************************************
          // **** The result of a property is asked
          switch( mThisProperty.getType() ) {
            case PropertyType.BINARY: {
              setBinaryResult( pResponseHeaders, params, mThisProperty.getBinary() );
              mResultContentsType = "jcr/binary";
              setContentType( "jcr/binary" );
              result = mResultContentsStream;
              break;
            }
            default:
              final StringBuilder replyString = new StringBuilder(mThisProperty.getValue().getString());
              setContentType( "text/plain" );
              result = replyString;
          }
          
          
        } else if (mThisNode!=null) {
          
          Node thisNode = mThisNode;
          
          // **********************
          // **** jecars:Link check
          if ((alt==null) && thisNode.hasProperty( gDefLink )) {
            thisNode = CARS_Utils.getLinkedNode( getMain(), thisNode );
          }
          
          // *************************************  
          // **** URL check
          if ((thisNode.isNodeType( gDefURLResource )) && (alt==null) && (mResultContentsStream==null) &&
             ((thisNode.hasProperty(gDefURL)) || (thisNode.hasProperty("jcr:data"))) ) {              
            if (thisNode.hasProperty(gDefURL)) {
              final String urlString = thisNode.getProperty( gDefURL ).getString();
              final URL url = new URL( urlString );
//              result = url.openStream();
              final URLConnection urlc = url.openConnection();
              setContentsLength( urlc.getContentLength() );
              result = urlc.getInputStream();
              if (thisNode.hasProperty( "jcr:mimeType" )) {
                setContentType( thisNode.getProperty( "jcr:mimeType" ).getString() );                  
              } else {
                setContentType( CARS_Mime.getMIMEType( urlString, null ) );
              }
            } else {
              Binary bin = null;
              if (thisNode.hasProperty( "jecars:PathToFile" )) {
                boolean read = true;
                if (thisNode.hasProperty( "jecars:Partial" )) {
                  read = !thisNode.getProperty( "jecars:Partial" ).getBoolean();
                }
                if (read) {
                  final File f = new File( thisNode.getProperty( "jecars:PathToFile" ).getString() );
                  if (f.canRead()) {
//                      System.out.println("STREAM FILE SDSD " + f.getAbsolutePath() );
                    bin = thisNode.getSession().getValueFactory().createBinary( new FileInputStream(f) );
                  }
                }
              }
              // **************************
              // ***** Binary
              if (bin==null) {
                bin = thisNode.getProperty( "jcr:data" ).getBinary();
              }
              setBinaryResult( pResponseHeaders, params, bin );
              result = mResultContentsStream;
  //              result = bin.getStream();
  //              pResponseHeaders.putData( "Content-Length", String.valueOf(bin.getSize()) );
              if (thisNode.hasProperty( "jcr:mimeType" )) {
                setContentType( thisNode.getProperty( "jcr:mimeType" ).getString() );              
              } else {
                setContentType( CARS_Mime.getMIMEType( thisNode.getName(), null ) );
              }
            }
          } else {
            // **** Result stream set?
            if ((mResultContentsStream!=null) && (alt==null)) {
              result = mResultContentsStream;
              setContentType( mResultContentsType );
            } else {
//              StringBuilder replyString = new StringBuilder();
              CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
              createGetNodesResult( buffer );
              if (mError!=null) {
                buffer = new CARS_Buffer( new StringBuilder() );
//                replyString = new StringBuilder();
                createError( buffer );
                result = buffer;
              } else {
                if ((mResultContentsStream!=null) && (alt==null)) {
                  result = mResultContentsStream;
                  setContentType( mResultContentsType );
                } else {
                  result = buffer;
                }
              }
//              result = replyString;
            }
          }
        } else if (mCreatedNode!=null) {
          // **** Result stream set?
          if ((mResultContentsStream!=null) && (alt==null)) {
            result = mResultContentsStream;
            setContentType( mResultContentsType );
          } else {
            // **** A new node has been created
            CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
//            StringBuilder replyString = new StringBuilder();
            createCreateNodeResult( buffer );
            if (mError!=null) {
              buffer = new CARS_Buffer( new StringBuilder() );
              createError( buffer );
            }
            result = buffer;
          }
        } else if (mDeletedNodePaths!=null) {
          // **** Node(s) have been deleted
          CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
//          StringBuilder replyString = new StringBuilder();
          createDeleteNodeResult( buffer );
          if (mError!=null) {
            buffer = new CARS_Buffer( new StringBuilder() );
//            replyString = new StringBuilder();
            createError( buffer );
          }
          result = buffer;
        }
      }
    } catch (NoSuchAlgorithmException nse) {
      setError( nse );
      final CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
//      final StringBuilder replyString = new StringBuilder();
      createError( buffer );
      result = buffer;
    } catch (RepositoryException re) {
      setError( re );
      final CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
//      final StringBuilder replyString = new StringBuilder();
      createError( buffer );
      result = buffer;
    } catch (IOException e) {
      setError( e );
      final CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
//      final StringBuilder replyString = new StringBuilder();
      createError( buffer );
      result = buffer;
    } catch (Exception e) {
      setError( e );
      final CARS_Buffer buffer = new CARS_Buffer( new StringBuilder() );
      createError( buffer );
      result = buffer;
    }
    
    return result;
  }

  /** isValidNamespace
   *
   * @param pNamespace
   * @return
   * @throws java.lang.Exception
   */
  static public boolean isValidNamespace( String pNamespace ) throws RepositoryException {
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      final String[] pre = appSession.getNamespacePrefixes();
      for( int i = 0; i<pre.length; i++ ) {
        if (pre[i].equals( pNamespace )) return true;
      }
    }
    return false;
  }

  /** convertValueName
   *
   * @param pProperty
   * @param pValue
   * @return
   * @throws java.lang.Exception
   */
  static public String convertValueName( final String pProperty, final String pValue ) throws RepositoryException {
    String p = null;
    if ("jcr:primaryType".equals( pProperty )) {
      if (pValue.indexOf( ':' )!=-1) {
        if (!pValue.startsWith( DEF_NS )) {
          if (!isValidNamespace( pValue.substring( 0, pValue.indexOf(':' )))) {
            p = pValue.replace( pValue.substring( 0, pValue.indexOf( ':' )), CARS_Definitions.DEFAULTNS1 );
          } else {
            p = pValue;
          }
        } else {
          p = pValue;              
        }
      } else {
        p = DEF_NS + pValue;
      }
    } else {
      p = pValue;
    }
    return p;
  }

  /** Convert property name, when its starts with an unknown namespace
   */
  static public String convertPropertyName( String pProperty ) {
    String p = null;    
    for( String ns: gIncludeNS ) {
      if (pProperty.startsWith( ns )==true) return pProperty;
    }
    if (pProperty.indexOf( ':' )!=-1) {
      p = pProperty.replace( pProperty.substring( 0, pProperty.indexOf( ':' )), CARS_Definitions.DEFAULTNS1 );
    } else {
      p = DEF_NS + pProperty;
    }
    return p;
  }
  
}
