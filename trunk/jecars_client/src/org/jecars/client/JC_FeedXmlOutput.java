/*
 * Copyright 2008-2009 NLR - National Aerospace Laboratory
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

import com.google.gdata.util.common.base.StringUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @version $Id: JC_FeedXmlOutput.java,v 1.8 2009/06/21 18:39:46 weertj Exp $
 */
public class JC_FeedXmlOutput {
  
  private final JC_Params     mParams;
  private final StringBuilder mDocument;

  private AbstractMap<String, String>mNamespaces = null;
  
  /** JC_FeedXmlOutput
   */
  public JC_FeedXmlOutput( JC_Params pParams ) {
    if (pParams!=null) {
      mParams   = pParams;
      mDocument = null;
    } else {
      mParams   = null;
      mDocument = new StringBuilder();
      setHeader();
    }
    // **** Default namespaces
    mNamespaces = new HashMap<String, String>();
    addNamespace( "jcr",    "http://www.jcp.org/jcr/1.0" );
    addNamespace( "jecars", "http://jecars.org" );
    return;
  }
  
  /** getParams
   * 
   * @return
   */
  public JC_Params getParams() {
    return mParams;
  }
  
  /** addNamespace
   * 
   * @param pId
   * @param pUrl
   */
  public void addNamespace( String pId, String pUrl ) {
    mNamespaces.put( pId, pUrl );
    return;
  }
  
  /** encodeTransportContent
   * @param pS
   * @return
   * @throws java.io.UnsupportedEncodingException
   */
  static public String encodeTransportContent( final String pS ) {
    return StringUtil.xmlContentEscape( pS );
//    return URLEncoder.encode( pS, "UTF-8" );
  }
  
  static public String encodeTransport( final String pS ) throws UnsupportedEncodingException {
    return StringUtil.xmlEscape( pS );
  }
  
  /** decodeTransport
   * @param pS
   * @return
   * @throws java.io.UnsupportedEncodingException
   */
  static public String decodeTransport( final String pS ) throws UnsupportedEncodingException {
    return URLDecoder.decode( pS, JC_RESTComm.CHARENCODE );
  }

  /** startNewEntry
   * 
   */
  protected void startNewEntry() {
    if (mParams!=null) {
        
    } else {
      mDocument.append("<entry xmlns=\"http://www.w3.org/2005/Atom\" ");
      Iterator<String> it = mNamespaces.keySet().iterator();
      String id, url;
     while( it.hasNext() ) {
        id = it.next();
        url = mNamespaces.get( id );
        mDocument.append( "xmlns:" ).append( id ).append( "=\"" ).append( url ).append( "\" ");
      }
      mDocument.append(">\n");    
    }
    return;
  }

  /** addProperty
   * @param pName
   * @param pValue
   * @throws java.io.UnsupportedEncodingException
   */
  protected void addProperty( final String pName, final String pValue ) throws UnsupportedEncodingException {
    if (mParams!=null) {
      mParams.addOtherParameter( pName, pValue );
    } else {
      mDocument.append("<").append( pName ).append(">");
      mDocument.append( encodeTransportContent( pValue ) );
      mDocument.append("</").append( pName ).append(">\n");
    }
    return;
  }

  /** addProperty
   * 
   * @param pName
   * @param pValue
   * @throws java.io.UnsupportedEncodingException
   */
  protected void addProperty( final String pName, final Long pValue ) throws UnsupportedEncodingException {
    if (mParams!=null) {
      mParams.addOtherParameter( pName, pValue.toString() );
    } else {
      mDocument.append("<").append( pName ).append(">");
      mDocument.append( pValue );
      mDocument.append("</").append( pName ).append(">\n");
    }
    return;
  }

  /** addProperty
   * 
   * @param pName
   * @param pValue
   * @throws java.io.UnsupportedEncodingException
   */
  protected void addProperty( final String pName, final Double pValue ) throws UnsupportedEncodingException {
    if (mParams!=null) {
      mParams.addOtherParameter( pName, pValue.toString() );
    } else {
      mDocument.append("<").append( pName ).append(">");
      mDocument.append( pValue );
      mDocument.append("</").append( pName ).append(">\n");
    }
    return;
  }

  /** addProperty
   * 
   * @param pName
   * @param pValue
   * @throws java.io.UnsupportedEncodingException
   */
  protected void addProperty( final String pName, final Boolean pValue ) throws UnsupportedEncodingException {
    if (mParams!=null) {
      mParams.addOtherParameter( pName, pValue.toString() );
    } else {
      mDocument.append("<").append( pName ).append(">");
      mDocument.append( pValue );
      mDocument.append("</").append( pName ).append(">\n");
    }
    return;
  }
  
  /** addProperty
   * 
   * @param pName
   * @param pNodeValue
   * @throws org.jecars.client.JC_Exception
   */
  protected void addProperty( final String pName, final JC_Nodeable pNodeValue ) throws JC_Exception {
    if (mParams!=null) {
        
    } else {
      mDocument.append("<").append( pName ).append(">");
      mDocument.append( encodeTransportContent( pNodeValue.getPath() ) );    
      mDocument.append("</").append( pName ).append(">\n");
    }
    return;
  }
 
  /** addAuthor
   * @param pValue
   * @throws java.io.UnsupportedEncodingException
   */
  protected void addAuthor( final String pValue) throws UnsupportedEncodingException {
    if (mParams!=null) {
        
    } else {
      mDocument.append("<author><name>");
      mDocument.append( encodeTransportContent(pValue) );
      mDocument.append("</name></author>\n");
    }
    return;
  }

  /**
   * @param pValue
   */
  protected void addCategory( final String pValue ) throws UnsupportedEncodingException {
    if (mParams!=null) {
      mParams.addOtherParameter( "jcr:primaryType", pValue );
    } else {
      mDocument.append("<category term=\"");
      mDocument.append( encodeTransport(pValue) );
      mDocument.append("\"/>\n");
    }
    return;
  }

  protected void addLinkViaURL( final JC_Clientable pClient, final String pUrl ) {
    if (mParams!=null) {
        
    } else {
      mDocument.append("<link rel=\"");
      mDocument.append("via\" ");
      mDocument.append("ref=\"");
      mDocument.append( pUrl );
      mDocument.append("\" ");
      mDocument.append("href=\"");
      mDocument.append( pClient.getServerPath() ).append( pUrl );
      mDocument.append("\"/>");
    }
  }

  /** setHeader
   * 
   */
  private void setHeader() {
//    if (mDocument!=null) mDocument.append("<?xml version=\"1.0\"?>\n" );
    if (mDocument!=null) mDocument.append("<?xml version=\"1.0\" encoding=\"" ).append( JC_RESTComm.CHARENCODE ).append( "\"?>\n" );
    return;
  }
  
  /** closeEntry
   */
  protected void closeEntry() {
    if (mDocument!=null) mDocument.append("</entry>\n");
    return;
  }
  
  /** getXml
   * @return
   */
  protected String getXml() {
    if (mDocument!=null) {
      return mDocument.toString();
    }
    return null;
  }
  
  /** toString
   * @return
   */
  @Override
  public String toString() {
    return getXml();
  }

}
