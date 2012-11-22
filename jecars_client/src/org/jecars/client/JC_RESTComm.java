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
    
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.*;
import nl.msd.jdots.JD_Taglist;

/**
 * JC_RESTComm
 *
 */
public class JC_RESTComm implements Serializable {

    
  private static final Logger LOG = Logger.getLogger( JC_RESTComm.class.getPackage().getName() );

  static public final String CT_ATOM_XML = "application/atom+xml";
  
  static public final String ATOM_VERSION_1 = "atom_1.0";

  static public final String CHARENCODE = "iso-8859-1";
//  static public final String CHARENCODE = "UTF-8";
  
  static public final String GET    = "GET";
  static public final String PUT    = "PUT";
  static public final String POST   = "POST";
  static public final String DELETE = "DELETE";
  static public final String HEAD   = "HEAD";
  
  static public final String ERRORSTREAM = "ErrorStream";
  static public final String INPUTSTREAM = "InputStream";

  static private boolean COMPRESS_MESSAGES = false;

  private boolean mUsingProxy = false;
  private String  mProxyUsername = null;
  private char[]  mProxyPassword = null;

  private String  mUsername = null;
  private char[]  mPassword = null;
  private String  mGoogleLoginAuth = null;
  
//  private JC_Clientable mClient = null;
    
  /**
   * Name of HTTP header containing the method name that overrides
   * the normal HTTP method. This is used to allow clients that are
   * unable to issue PUT or DELETE methods to emulate such methods.
   * The client would issue a POST method with this header set to
   * PUT or DELETE, and the service translates the invocation to
   * the corresponding request type.
   */
  public static final String METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";
  public boolean             METHOD_OVERRIDE        = false;
  
  public static final String LF = "\n"; 
  public static boolean gInit = false;
  
  static private String gREFERER = "jecars.org";

  /** Creates a new instance of JC_RESTComm
   */
  public JC_RESTComm( JC_Clientable pClient ) {
//    mClient = pClient;
    if (gInit==false) {
      init();
    }
    return;
  }

  /** setReferer
   *
   * @param pRef
   */
  static public void setReferer( final String pRef ) {
    gREFERER = pRef;
    return;
  }

  static public void init() {
    try {      
      trustHttpsCertificates();
      gInit = true;
    } catch( AccessControlException ace) {
      LOG.log( Level.INFO, "Https Certificates not added: " + ace.getMessage() );
    } catch (Exception e) {
      LOG.log( Level.SEVERE, "", e );
    }
    return;
  }

    /**
   * Method to not verify the hostname
   */
   private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
     @Override
     public boolean verify(String hostname, SSLSession session) {
       return true;
     }
   };
   
   
  static public void trustHttpsCertificates() throws Exception {
//    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    Security.addProvider( (Provider)Class.forName( "com.sun.net.ssl.internal.ssl.Provider" ).newInstance() );
    TrustManager[] trustAllCerts = new TrustManager[] {
         new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
              return;
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
              return;
            }
         }//X509TrustManager
    };//TrustManager[]
    //Install the all-trusting trust manager:
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier( DO_NOT_VERIFY );
   
    return;
  }
  
  /** useCompressMessages
   *
   * @param pCompress
   */
  static public void useCompressMessages( final boolean pCompress ) {
    COMPRESS_MESSAGES = pCompress;
    return;
  }
  
  public void setUsingProxy( boolean pProxy ) {
    mUsingProxy = pProxy;
    return;
  }
  
  public boolean usingProxy() {
    return mUsingProxy;
  }
  
  public String getProxyUsername() {
    return mProxyUsername;
  }
  
  public void setProxyUsername( String pUsername ) {
    mProxyUsername = pUsername;
    return;
  }
  
  public String getProxyPassword() {
    return new String(mProxyPassword);
  }
  
  public void setProxyPassword( char[] pPassword ) {
    mProxyPassword = new String(pPassword).toCharArray();
    return;
  }

  /** setGoogleLoginAuth
   *
   * @param pAuth
   */
  public void setGoogleLoginAuth( final String pAuth ) {
    mGoogleLoginAuth = pAuth;
    return;
  }

  /** getGoogleLoginAuth
   *
   * @return
   */
  protected String getGoogleLoginAuth() {
    return mGoogleLoginAuth;
  }
  
  /** setUsername
   * @param pUsername
   * @param pPassword
   */
  public void setUsername( final String pUsername, final char[] pPassword ) {
    mUsername = pUsername;
    if (pPassword==null) {
      mPassword = null;
    } else {
      mPassword = new String(pPassword).toCharArray();
    }
    return;
  }
  
  public String getUsername() {
    return mUsername;
  }
  
  /** getPassword
   * @return
   */
  public char[] getPassword() {
    final char [] pw;
    if (mPassword==null) {
      pw = null;
    } else {
      pw = new String(mPassword).toCharArray();
    }
    return pw;
  }
  
  /** createHttpConnection
   * @param pURL
   * @return
   * @throws java.lang.Exception
   */
  public HttpURLConnection createHttpConnection( final String pURL ) throws MalformedURLException, IOException {
    return createHttpConnection( pURL, mUsername, mPassword );
  }

  /** apply the ContentsLength to a HttpURLConnection
   * Default disabled because of sporadic problems
   * http://forums.sun.com/thread.jspa?threadID=5188839
   * @param pConn
   * @param pContentsLength
   */
  protected void applyContentsLength( final HttpURLConnection pConn, final long pContentsLength ) {
    if (pContentsLength!=-1) {
      pConn.setFixedLengthStreamingMode( (int)pContentsLength );
    } else {
      pConn.setChunkedStreamingMode( 50000 );
    }
    return;
  }

  public String getCharDecoder() {
    return CHARENCODE;
  }
  
  /** Create http url connection
   * 
   * @param pURL
   * @param pUsername
   * @param pPassword
   * @return
   * @throws java.net.MalformedURLException
   * @throws java.io.IOException
   */
  public HttpURLConnection createHttpConnection( final String pURL, final String pUsername, final char[] pPassword ) throws MalformedURLException, IOException {
    String url = pURL.replace( ' ', '+' );
    if (mGoogleLoginAuth!=null) {
      if (url.indexOf( '?' )==-1) {
        url += "?GOOGLELOGIN_AUTH=" + mGoogleLoginAuth;
      } else {
        url += "&GOOGLELOGIN_AUTH=" + mGoogleLoginAuth;
      }
    }
    final URL u = new URL( url );
    final HttpURLConnection uc = (HttpURLConnection)u.openConnection();
    if (usingProxy()) {
      final String proxyPw = getProxyUsername() + ":" + getProxyPassword();
      final String encProxyPw = BASE64Encoder.encodeBuffer( proxyPw.getBytes() );
      uc.setRequestProperty( "Proxy-Authorization", "Basic " + encProxyPw );
    }
    if ((mGoogleLoginAuth==null) && (pUsername!=null)) {
      final String userPassword = pUsername + ":" + new String(pPassword);
      final String encoding = BASE64Encoder.encodeBuffer( userPassword.getBytes() );
      uc.setRequestProperty ("Authorization", "BASIC " + encoding);
    }
    uc.setUseCaches( false );
    if (COMPRESS_MESSAGES) {
      uc.addRequestProperty( "Accept-Encoding" , "gzip, deflate" );
    }
    uc.setInstanceFollowRedirects( false );
    uc.setRequestProperty( "Connection", "Keep-Alive" );
    uc.setRequestProperty( "User-Agent", JC_Clientable.VERSION );
    uc.setRequestProperty( "Referer", gREFERER );    
    return uc;
  }

  /** handleReply
   *
   * @param pConn
   * @param pTags
   * @return
   * @throws java.io.IOException
   */
  private JD_Taglist handleReply( final HttpURLConnection pConn, final JD_Taglist pTags ) throws IOException {
    pTags.replaceData( "ContentLength",   Integer.valueOf(pConn.getContentLength()) );
    pTags.replaceData( "ContentType",     pConn.getContentType() );
    pTags.replaceData( "ContentEncoding", pConn.getContentEncoding() );
    pTags.replaceData( "LastModified",    pConn.getLastModified() );
    pTags.replaceData( "ResponseCode",    Integer.valueOf(pConn.getResponseCode()) );
    try {
      final InputStream is;
      final String encoding = pConn.getContentEncoding();
      if (encoding!=null && encoding.equalsIgnoreCase("gzip")) {
        is = new GZIPInputStream(pConn.getInputStream());
      } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
        is = new InflaterInputStream(pConn.getInputStream(), new Inflater(true));
      } else {
        is = pConn.getInputStream();
      }
      pTags.replaceData( INPUTSTREAM, is );
    } catch( Exception e ) {        
    }
    try {
      pTags.replaceData( ERRORSTREAM, pConn.getErrorStream() );
    } catch( Exception e ) {
    }
    String headerValue;
    headerValue = pConn.getHeaderField( "Location" );
    if (headerValue!=null) {
      pTags.replaceData( "Location", headerValue );
    }
//    pConn.disconnect();
    return pTags;
  }
  
  /** sendMessageDELETE
   * 
   * @param pClient
   * @param pConn
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist sendMessageDELETE( final JC_Clientable pClient, final HttpURLConnection pConn ) throws Exception {
    boolean asGet = false;
    if (pClient!=null) asGet = pClient.getHttpOperation( JC_Clientable.DELETE_AS_GET );
    JD_Taglist tags = new JD_Taglist();
    tags.putData( "JC_Clientable", pClient );
    if (asGet) {
//      gLog.log( Level.INFO, "DELETE (AS GET): " + pConn.getURL() );
      pConn.setRequestMethod( GET );
      pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );
    } else {
//      gLog.log( Level.INFO, "DELETE: " + pConn.getURL() );
      pConn.setRequestMethod( DELETE );
    }
    tags = handleReply( pConn, tags );
    return tags;
  }

  
  /** sendMessageGET
   */
  public JD_Taglist sendMessageGET( final JC_Clientable pClient, final HttpURLConnection pConn ) throws ProtocolException, IOException {
//    gLog.log( Level.INFO, "GET: " + pConn.getURL() );
    JD_Taglist tags = new JD_Taglist();
    tags.putData( "JC_Clientable", pClient );
    pConn.setRequestMethod( GET );
    pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );
    tags = handleReply( pConn, tags );
    return tags;
  }

  /** sendMessageHEAD
   * @param pConn
   * @param pContentsLength
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist sendMessageHEAD( JC_Clientable pClient, HttpURLConnection pConn ) throws ProtocolException, IOException {
    boolean asGet = false;
    if (pClient!=null) {
      asGet = pClient.getHttpOperation( JC_Clientable.HEAD_AS_GET );
    }
    if (asGet) {
//      gLog.log( Level.INFO, "HEAD (AS GET): " + pConn.getURL() );
      pConn.setRequestMethod( GET );
      pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );      
    } else {
      // **** Send as HEAD
//      gLog.log( Level.INFO, "HEAD: " + pConn.getURL() );
      pConn.setRequestMethod( HEAD );
      pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );
    }
    JD_Taglist tags = new JD_Taglist();
    tags.putData( "JC_Clientable", pClient );
    tags = handleReply( pConn, tags );
    return tags;
  }

  /** sendMessagePOST
   * @param pConn
   * @param pContents
   * @param pContentsType
   * @param pContentsLength
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist sendMessagePOST( JC_Clientable pClient, HttpURLConnection pConn, InputStream pContents, String pContentsType, long pContentsLength ) throws Exception {
    boolean asGet = false;
    if (pClient!=null) {
      asGet = pClient.getHttpOperation( JC_Clientable.POST_AS_GET );
    }
    JD_Taglist tags = new JD_Taglist();
    tags.putData( "JC_Clientable", pClient );
    if (asGet) {
//      gLog.log( Level.INFO, "POST (AS GET): " + pConn.getURL() );
      pConn.setRequestMethod( GET );
      pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );
    } else {
      // **** Send as POST
//      gLog.log( Level.INFO, "POST: " + pConn.getURL() );
      pConn.setRequestMethod( POST );
      pConn.setDoOutput( true );        
      pConn.setDoInput(true);
      pConn.setUseCaches(false);
      pConn.setRequestProperty( "Content-Type", pContentsType );//"application/x-www-form-urlencoded" );    
      if ((pClient!=null) && (pClient.isInChunkedStreamingMode())) {
        applyContentsLength( pConn, pContentsLength );
      }
      final OutputStream os = pConn.getOutputStream();
      try {
        sendInputStreamToOutputStream( 50000, pContents, os );
      } finally {
        os.flush();
        os.close();
      }
    }
    tags = handleReply( pConn, tags );
    return tags;
  }

  /** sendMessagePUT
   * @param pConn
   * @param pContents
   * @param pContentsType
   * @param pContentsLength
   * @return
   * @throws java.lang.Exception
   */
  public JD_Taglist sendMessagePUT( JC_Clientable pClient, HttpURLConnection pConn, InputStream pContents, String pContentsType, long pContentsLength ) throws Exception {
    boolean asGet = false;
    if (pClient!=null) asGet = pClient.getHttpOperation( JC_Clientable.PUT_AS_GET );
    JD_Taglist tags = new JD_Taglist();
    tags.putData( "JC_Clientable", pClient );
    if (asGet) {
//      gLog.log( Level.INFO, "PUT (AS GET): " + pConn.getURL() );
      pConn.setRequestMethod( GET );
      pConn.setRequestProperty( "Accept", "application/atom+xml, */*" );
    } else {
//      gLog.log( Level.INFO, "PUT: " + pConn.getURL() );
      pConn.setRequestMethod( PUT );
      pConn.setDoOutput( true );        
      pConn.setDoInput( true );
      pConn.setUseCaches(false);
      pConn.setRequestProperty( "Content-Type", pContentsType );//"application/x-www-form-urlencoded" );    
      if (pClient.isInChunkedStreamingMode()) {
        applyContentsLength( pConn, pContentsLength );
      }
    
      OutputStream os = pConn.getOutputStream();
      try {
        sendInputStreamToOutputStream( 50000, pContents, os );
      } finally {
        os.flush();
        os.close();
      }
    }
    tags = handleReply( pConn, tags );
    return tags;
  }

  /** getResultStream
   * @param pTags
   * @return
   */
  static public InputStream getResultStream( JD_Taglist pTags ) {
    return (InputStream)pTags.getData( INPUTSTREAM );
  }

  /** getErrorStream
   * 
   * @param pTags
   * @return
   */
  static public InputStream getErrorStream( JD_Taglist pTags ) {
    return (InputStream)pTags.getData( ERRORSTREAM );
  }
  
  /** getResponseCode
   * @param pTags
   * @return
   */
  static public int getResponseCode( JD_Taglist pTags ) {
    return ((Integer)pTags.getData( "ResponseCode" )).intValue();
  }
  
  /** sendInputStreamToOutputStream
   * @param pBufferSize
   * @param pInput
   * @param pOutput
   * @throws java.lang.Exception
   */
  static public void sendInputStreamToOutputStream( int pBufferSize, InputStream pInput, OutputStream pOutput ) throws Exception {
    if (pInput==null) return;
    BufferedInputStream  bis = new BufferedInputStream(  pInput );
    BufferedOutputStream bos = new BufferedOutputStream( pOutput );
    try {
      byte[] buff = new byte[pBufferSize];
//      long sended = 0;
      int bytesRead;
      while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
        bos.write(buff, 0, bytesRead);
        bos.flush();
//        sended += bytesRead;
//              System.out.println( "--- " + sended );
      }
    } finally {
      pInput.close();
      if (bis!=null) bis.close();
      if (bos!=null) {
        bos.flush();
        bos.close();
      }
    }
    return;
  }

  /** sendInputStreamToBase64
   * 
   * @param pTotalBufferSize
   * @param pInput
   * @return
   * @throws java.lang.Exception
   */
  static public String sendInputStreamToBase64( int pTotalBufferSize, InputStream pInput ) throws Exception {
    String base64 = null;
    if (pInput==null) return base64;
    BufferedInputStream  bis = new BufferedInputStream(  pInput );
    try {
      byte[] buff = new byte[pTotalBufferSize];
      int bytesRead;
      bytesRead = bis.read(buff, 0, buff.length);
      base64 = BASE64Encoder.encodeBuffer( buff, 0, bytesRead );
    } finally {
      pInput.close();
      if (bis!=null) bis.close();
    }
    
    return base64;
  }
  
}

