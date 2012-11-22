/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.msd.jdots.JD_Taglist;

/** JC_Utils
 *
 * @version $Id: JC_Utils.java,v 1.23 2009/06/23 20:30:27 weertj Exp $
 */
public class JC_Utils {


  /** readAsString2
   * 
   * @param pInput
   * @return
   * @throws IOException 
   */
  static public String readAsString2( final InputStream pInput ) throws IOException {
    final char[] buffer = new char[10000];
    final StringBuilder out = new StringBuilder();
    final Reader in = new InputStreamReader(pInput, JC_RESTComm.CHARENCODE );
    try {
      int read;
      do {
        read = in.read(buffer, 0, buffer.length);
        if (read>0) {
          out.append(buffer, 0, read);
        }
      } while (read>=0);
    } finally {
      in.close();
    }
    return out.toString();
  }

    
 /** Read an inputstream as string
  * @param pInput The inputstream
  * @return The resulting string
  * @throws IOException when an error occurs
  */
 static public String readAsString( final InputStream pInput ) throws IOException {
//   try {
     final InputStreamReader isr = new InputStreamReader(pInput);
     final BufferedReader br = new BufferedReader(isr);
     final StringBuilder buf = new StringBuilder();
     String line;
     while((line = br.readLine()) != null) {
       buf.append(line).append('\n');
     }
     return buf.toString();
//   } finally {
//   }
 }

 /** readAsString
  *
  * @param pInput
  * @param pEncoding
  * @return
  * @throws IOException
  */
 static public String readAsString( final InputStream pInput, final String pEncoding ) throws IOException {
     final InputStreamReader isr = new InputStreamReader( pInput, pEncoding );
     final BufferedReader br = new BufferedReader(isr);
     final StringBuilder buf = new StringBuilder();
     String line;
     while((line = br.readLine()) != null) {
       buf.append(line).append('\n');
     }
     return buf.toString();
 }

 /** fillInServerMessage
  * 
  * @param pET
  * @param pServerMessage 
  */
 static private void fillInServerMessage( final JC_ExceptionTags pET, final String pServerMessage ) {
   pET.getErrorTags().putData( JC_ExceptionTags.ERRORTAG_XMLBODY, pServerMessage );
   int ix = pServerMessage.indexOf( "<![CDATA[" );
   if (ix!=-1) {
     final String sm = pServerMessage.substring( ix + "<![CDATA[".length() );
     if (sm.startsWith( "javax.jcr.PathNotFoundException")) {
       pET.setJeCARSErrorCode( JC_ExceptionTags.JECARSERROR_PATHNOTFOUND );
       int st  = sm.indexOf( ':' );
       int end = sm.indexOf( '\n' );
       final String path = sm.substring( st+1, end );
       pET.getErrorTags().putData( JC_ExceptionTags.ERRORTAG_SERVERERROR, "Path not found" );
       pET.getErrorTags().putData( JC_ExceptionTags.ERRORTAG_PATH, path );
     } else {
       // **** just use the message of the returned error, i.e. the part between first ':' and '\n'. 
       // **** returned error sm has format: <class1.name>: <msg>\n at <class2.name>
       int st  = sm.indexOf( ':' );
       int end = sm.indexOf( '\n' );
       final String message = sm.substring( st+1, end );
       pET.getErrorTags().putData( JC_ExceptionTags.ERRORTAG_SERVERERROR, message);
       pET.setJeCARSErrorCode(     JC_ExceptionTags.JECARSERROR_UNKNOWN );
     }
   } else {
     if (pServerMessage.indexOf( "This request requires HTTP authentication" )!=-1) {
       pET.setJeCARSErrorCode(     JC_ExceptionTags.JECARSERROR_AUTHENTICATION_NEEDED );       
       pET.getErrorTags().putData( JC_ExceptionTags.ERRORTAG_SERVERERROR, "Action needs authentication (or not enough rights)" );       
     }
   }
   return;
 }
 
 /** createCommException
  * @param pTags
  * @param pMessage
  * @param pURL
  * @return
  * @throws java.io.IOException
  */
 static public JC_HttpException createCommException( final JD_Taglist pTags, final String pMessage, final String pURL ) {
   int retCode = 0;
   final EnumSet<JC_ClientOption> options = EnumSet.noneOf( JC_ClientOption.class );
   if (pTags!=null) {
     retCode = JC_RESTComm.getResponseCode( pTags );
     final JC_Clientable client = (JC_Clientable)pTags.getData( "JC_Clientable" );
     if (client!=null) {
       options.addAll( client.getOptions() );
     }
   }   
   JC_HttpException e;
   if ((pTags!=null) && (pTags.getData( "ErrorStream" )!=null)) {
     try {
       final String error = JC_Utils.readAsString( JC_RESTComm.getErrorStream( pTags ) );
       if (options.contains( JC_ClientOption.USE_EXCEPTIONS_V2 )) {
         JC_ExceptionTags ets = new JC_ExceptionTags( "[ERROR " + retCode + "] " + pMessage + pURL + ": " + error );
         ets.setHttpErrorCode( JC_HttpErrorCode.createHttpErrorCode( retCode ));
         ets.mServerMessage = error;
         fillInServerMessage( ets, error );
         
         e = ets;
       } else {
         e = JC_HttpException.createErrorHttpException( retCode, pMessage + pURL, error );
       }
//                          JC_Utils.readAsString( JC_RESTComm.getErrorStream( pTags ) ) );
//                          JC_Utils.readAsString( (InputStream)pTags.getData( JC_RESTComm.ERRORSTREAM ) ) );
     } catch( IOException ioe ) {
       e = JC_HttpException.createErrorHttpException( retCode, pMessage + pURL, ioe.getMessage() );
     }
   } else {
     if (pTags==null) {
       e = JC_HttpException.createErrorHttpException( retCode, pMessage + pURL );
     } else {
       final Throwable t = (Throwable)pTags.getData( "ResponseException" );
       if ((t==null) || !(t instanceof Exception)) {
         e = JC_HttpException.createErrorHttpException( retCode, pMessage + pURL );
       } else {
         e = JC_HttpException.createErrorHttpException( retCode, pMessage + pURL, (Exception)t );
       }
     }
   }
   return e;
 }

  /** buildURL
   *
   * @param pClient
   * @param pURL
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @throws java.io.UnsupportedEncodingException
   */
  static public void buildURL( final JC_Clientable pClient, final StringBuilder pURL, final JC_Params pParams, final JC_Filter pFilter, final JC_Query pQuery ) throws UnsupportedEncodingException, JC_Exception {

    if ((pParams!=null) || (pFilter!=null) || (pQuery!=null)) {
      char sepChar = '?';

      // **********************
      // **** URL expansion
      if (pParams!=null) {
        if (pParams.getPropertyName()!=null) {
          pURL.append( '/' ).append( pParams.getPropertyName() );
        }
      }
      
      // **********************
      // **** JC_Filter support
      if (pFilter!=null) {
        final Collection<String> filters = pFilter.getCategories();
        if (filters!=null) {
          if (filters.size()!=1) {
            throw new JC_Exception( "Filter categories only one supported" );
          }
          pURL.append( "/-/" ).append( filters.iterator().next() );
        }
        if (pFilter.getNamePattern()!=null) {
          pURL.append( sepChar ).append( "namePattern=" ).append( pFilter.getNamePattern() );
          sepChar = '&';
        }
      }
      // **********************
      // **** JC_Params support
      if (pParams!=null) {
        if (pParams.getEventCollectionID()!=null) {
          pURL.append( sepChar ).append( "jecars:EventCollectionID=" ).append( pParams.getEventCollectionID() );
          sepChar = '&';
        }
        if (pParams.getVCSCommand()!=null) {
          pURL.append( sepChar ).append( "vcs-cmd=" ).append( pParams.getVCSCommand() );
          sepChar = '&';
        }
        if (pParams.getVCSLabel()!=null) {
          pURL.append( sepChar ).append( "vcs-label=" ).append( pParams.getVCSLabel() );
          sepChar = '&';
        }
        if (pParams.isDeep()) {
          pURL.append( sepChar ).append( "deep=true" );
          sepChar = '&';
        }
        if (pParams.isLongPoll()) {
          pURL.append( sepChar ).append( "longpoll=true" );
          sepChar = '&';
        }
        if (pParams.isForced()) {
          pURL.append( sepChar ).append( "force=true" );
          sepChar = '&';
        }
        if (pParams.getIncludeBinary()) {
          pURL.append( sepChar ).append( "includeBinary=true" );
          sepChar = '&';
        }
        if (pParams.getAllProperties()) {
          pURL.append( sepChar ).append( "getAllProperties=true" );
          sepChar = '&';
        }
        if (pParams.getOutputFormat()!=null) {
          pURL.append( sepChar ).append( "alt=" ).append( pParams.getOutputFormat() );
          sepChar = '&';
        }
        final Map otherParams = pParams.getOtherParameters();
        final Set<String> keys = otherParams.keySet();
        if (pClient.isLocalClient()) {
          for (final String key : keys) {
            pURL.append( sepChar ).append( key ).append( '=' ).append( otherParams.get( key ) );
            sepChar = '&';
          }
        } else {
          for (final String key : keys) {
//          pURL.append( sepChar ).append( key ).append( '=' ).append( URLEncoder.encode( (String)otherParams.get( key ), "UTF-8") );
            pURL.append( sepChar ).append( key ).append( '=' ).append( URLEncoder.encode( (String)otherParams.get( key ), JC_RESTComm.CHARENCODE ) );
            sepChar = '&';
          }
        }
        final Collection<JC_Streamable>streams = pParams.getStreamables();
        if (streams!=null) {
          int c = 0;
          try {
            for (JC_Streamable stream : streams) {
              InputStream is = stream.getStream();
              pURL.append( sepChar ).append( "jcr:data" ).append( '=' );
              pURL.append( URLEncoder.encode( JC_Utils.readAsString( is ), "UTF-8" ) );
              sepChar = '&';
            }
          } catch( Exception e ) {
            throw JC_Exception.createErrorException( "Error stream conversion", e );
          }
        }
        if (pParams.getHTTPOverride()!=null) {
          pURL.append( sepChar ).append( JC_RESTComm.METHOD_OVERRIDE_HEADER ).append( '=' ).append( pParams.getHTTPOverride() );
          sepChar = '&';
        }
      }
      // *********************
      // **** JC_Query support
      if (pQuery!=null) {
        if (pQuery.getFullTextQuery()!=null) {
          pURL.append( sepChar ).append( "q=" ).append( pQuery.getFullTextQuery() );
          sepChar = '&';
        }
        if (pQuery.getVersionHistory()) {
          pURL.append( sepChar ).append( "vcs-history" );
          sepChar = '&';
        }
        if (pQuery.getChildNodeDefs()) {
          pURL.append( sepChar ).append( "childnodedefs=property" );
          sepChar = '&';          
        }
        if (pQuery.getWhereString()!=null) {
          if (pClient.isLocalClient()) {
            pURL.append( sepChar ).append( "where=" ).append( pQuery.getWhereString() );
          } else {
            pURL.append( sepChar ).append( "where=" ).append( URLEncoder.encode( pQuery.getWhereString(), JC_RESTComm.CHARENCODE ) );
          }
          sepChar = '&';          
        }
        if (pQuery.getXPathString()!=null) {
          if (pClient.isLocalClient()) {
            pURL.append( sepChar ).append( "xpath=" ).append( pQuery.getXPathString() );
          } else {
            pURL.append( sepChar ).append( "xpath=" ).append( URLEncoder.encode( pQuery.getXPathString(), JC_RESTComm.CHARENCODE ) );
          }
          sepChar = '&';          
        }
        if (pQuery.getOrderByString()!=null) {
          if (pClient.isLocalClient()) {
            pURL.append( sepChar ).append( "orderby=" ).append( pQuery.getOrderByString() );              
          } else {
            pURL.append( sepChar ).append( "orderby=" ).append( URLEncoder.encode( pQuery.getOrderByString(), JC_RESTComm.CHARENCODE ) );
          }
          sepChar = '&';
        }
        if (pQuery.getStartIndex()>=0) {
          pURL.append( sepChar ).append( "start-index=" ).append( pQuery.getStartIndex() );
          sepChar = '&';
        }
        if (pQuery.getMaxResults()>=0) {
          pURL.append( sepChar ).append( "max-results=" ).append( pQuery.getMaxResults() );
          sepChar = '&';
        }
      }
    }
    return; 
  }
 
  /** urlencode
   * 
   * @param pURL
   * @return
   */
  static public String urlencode( final String pURL ) {
    try {
      return URLEncoder.encode(pURL, JC_RESTComm.CHARENCODE);
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(JC_Utils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /** urldecode
   * 
   * @param pURL
   * @return
   */
  static public String urldecode( final String pURL ) {
    try {
      return URLDecoder.decode(pURL, JC_RESTComm.CHARENCODE);
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(JC_Utils.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }
  
  
  /** getFullNodeURL
   * 
   * @param pClient
   * @param pNode
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public StringBuilder getFullNodeURL( final JC_Clientable pClient, final JC_Nodeable pNode ) throws JC_Exception {
    final StringBuilder url = new StringBuilder( pClient.getServerPath() );
//    ((JC_DefaultNode)pNode).getJCPath().ensureEncode();
    url.append( pNode.getPath() ); 
    return url;
  }

    /** sendInputStreamToOutputStream
   * @param pBufferSize
   * @param pInput
   * @param pOutput
   * @throws java.lang.Exception
   */
  static public void sendInputStreamToOutputStream( final int pBufferSize, final InputStream pInput, final OutputStream pOutput ) throws IOException {
    final BufferedInputStream  bis = new BufferedInputStream(  pInput );
    final BufferedOutputStream bos = new BufferedOutputStream( pOutput );
    try {
      final byte[] buff = new byte[pBufferSize];
//    long sended = 0;
      int bytesRead = bis.read(buff, 0, buff.length);
      while(bytesRead!=-1) {
        bos.write(buff, 0, bytesRead);
        bos.flush();
//        sended += bytesRead;
//             System.out.println( "--- " + sended );
//     try {Thread.sleep( 50 );} catch(Exception e ) {};
        bytesRead = bis.read( buff, 0, buff.length );
      }
    } finally {
      pInput.close();
      if (bis!=null) {
        bis.close();
      }
      if (bos!=null) {
        bos.close();
      }
    }
    return;
  }

  /** sendInputStreamToOutputStreamNoClosing
   *
   * @param pBufferSize
   * @param pInput
   * @param pOutput
   * @throws IOException
   */
  static public void sendInputStreamToOutputStreamNoClosing( final int pBufferSize, final InputStream pInput, final OutputStream pOutput ) throws IOException {
    final BufferedInputStream  bis = new BufferedInputStream(  pInput );
    final BufferedOutputStream bos = new BufferedOutputStream( pOutput );
    final byte[] buff = new byte[pBufferSize];
    int bytesRead = bis.read(buff, 0, buff.length);
    while(bytesRead!=-1) {
      bos.write(buff, 0, bytesRead);
      bos.flush();
      bytesRead = bis.read( buff, 0, buff.length );
    }
    return;
  }

  /** toRelative
   * 
   * @param pRelp
   * @return 
   */
  static public String toRelative( final String pRelp ) {
    int ix = -1;
    int c  = 0;
    while( (ix=pRelp.indexOf( '/', ix+1 ))!=-1 ) {
      c++;
    }
    String pre = "";
    while( c-->0 ) {
      pre += "../";
    }
    return pre;
  }
  


}
