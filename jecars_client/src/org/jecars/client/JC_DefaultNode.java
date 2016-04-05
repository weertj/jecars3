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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import nl.msd.jdots.JD_Taglist;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.client.nt.JC_PermissionNode;

/**
 * JC_DefaultNode
 *
 * @version $Id: JC_DefaultNode.java,v 1.71 2009/07/29 14:22:11 weertj Exp $
 */
public class JC_DefaultNode extends JC_DefaultItem implements JC_Nodeable {
  
  static final protected Logger gLog = Logger.getLogger( "org.jecars.client" );
  
  static final public JC_Nodeable[]     TMPL_ARR   = new JC_Nodeable[0];
  static final public JC_Propertyable[] TMPL_ARR_P = new JC_Propertyable[0];
  
  static final public Pattern gPropPattern = Pattern.compile( "=" );

  static public enum PROPS { NONE, NULL, FORCED_REMOVE, ERROR, SYNCHRONIZED, DESTROYED, GOTNODES, CREATEUSINGCOPY };

  static final public String PROP_TITLE = "jecars:Title";
  static final public String PROP_BODY  = "jecars:Body";

  private JC_Nodeable                 mParentNode       = null;
  private List<JC_Nodeable>           mChildNodes       = null;
  private List<JC_Propertyable>       mProperties       = null;
  private JC_Path                     mPath             = null;
//  private boolean                     mCreateUsingCopy  = false;
  private String                      mSelfLink         = null;
  private String                      mSourceObjectUrl  = null;
  private String                      mNodeType         = null;
  private long                        mLastModified     = 0L;
  private String                      mID               = null;
  private final EnumSet<PROPS>        mNodeProps        = EnumSet.of( PROPS.NONE );
  private JC_Clientable               mClient           = null;
  
  /** JC_DefaultNode
   */
  public JC_DefaultNode() {
    super();
    return;
  }

  @Override
  public boolean equals( Object pObj ) {
    try {
      if ((pObj!=null) && (pObj instanceof JC_Nodeable)) {
        return getPath().equals( ((JC_Nodeable)pObj ).getPath() );
      }
    } catch( Exception e ) {      
    }
    return false;
  }

  @Override
  public String getID() {
    return mID;
  }

  @Override
  public void setID( final String pID ) {
    mID = pID;
    return;
  }

  /** copyToNode
   * 
   * @param pNode
   * @throws org.jecars.client.JC_Exception
   */
  public void copyToNode( final JC_DefaultNode pNode ) throws JC_Exception {
    pNode.mParentNode      = mParentNode;
    if (mChildNodes!=null) {
      if (mChildNodes instanceof ArrayList) {
        pNode.mChildNodes = (ArrayList<JC_Nodeable>)(((ArrayList<JC_Nodeable>)mChildNodes).clone());
      } else {
        pNode.mChildNodes = new ArrayList<JC_Nodeable>( mChildNodes );
      }
    }
    if (mProperties!=null) {
      pNode.mProperties = new ArrayList<JC_Propertyable>();
      try {
        for (JC_Propertyable prop : mProperties) {
          pNode.mProperties.add( (JC_Propertyable)(((JC_DefaultProperty)prop).clone()) );
        }
      } catch( CloneNotSupportedException cnse ) {
        throw JC_Exception.createErrorException( null, cnse );
      }
    }
    morphTo( pNode );
    pNode.mPath            = (JC_Path)mPath.clone();
//    pNode.mHasSynchronized = mHasSynchronized;
//    pNode.mCreateUsingCopy = mCreateUsingCopy;
    pNode.mSelfLink        = mSelfLink;
    pNode.mSourceObjectUrl = mSourceObjectUrl;
    pNode.mNodeType        = mNodeType;
    pNode.mNodeProps.clear();
    pNode.mNodeProps.copyOf( mNodeProps );
    if (mParentNode!=null) {
      if (((JC_DefaultNode)pNode.mParentNode).mChildNodes!=null) {
          ((JC_DefaultNode)pNode.mParentNode).mChildNodes.add( pNode );
      }
    }
    return;
  }

  /** morphToNode
   * 
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_DefaultNode morphToNodeType() throws JC_Exception {
    final String nt;
    final boolean morph;
    if (hasProperty( JC_Defs.UNSTRUCT_NODETYPE )) {
      nt = getNodeType() + '|' + getProperty( JC_Defs.UNSTRUCT_NODETYPE ).getValueString();
      morph = true;
    } else {
      nt = getNodeType();
      morph = false;
    }
    final JC_Clientable client = getClient();
    if (client.canBeMorphed( this, nt ) || morph) {
      final JC_DefaultNode node = (JC_DefaultNode)client.createNodeClass( nt );
      if (node!=null) {
        copyToNode( node );
        destroy( false );
        node.initPropertyMembers();
        return node;
      }
    }
    return this;
  }
  
  /** createFeedXml
   * 
   * @return
   */
  public JC_FeedXmlOutput createFeedXml( JC_Params pParams ) {
    JC_FeedXmlOutput fxo = new JC_FeedXmlOutput( pParams );
    AbstractMap<String, String>namespaces = getClient().getNamespaces();
    Iterator<String> it = namespaces.keySet().iterator();
    String id, url;
    while( it.hasNext() ) {
      id = it.next();
      url = namespaces.get( id );
      fxo.addNamespace( id, url );
    }
    return fxo;
  }
  
  /** getSelfLink
   * Get the complete link (including base URL) to itself or null when unknown.
   * 
   * @return
   */
  @Override
  public String getSelfLink() {
    return mSelfLink;
  }

  /** setSelfLink
   * 
   * @param pLink 
   */
  public void setSelfLink( final String pLink ) {
    mSelfLink = pLink;
    return;
  }
  
  /** setParent
   * @param pParent
   */
  public void setParent( final JC_Nodeable pParent ) {
    mParentNode = pParent;
    return;
  }
  
  /** isParent -- Check if this node is parent of the given child node
   * 
   * @param pChildNode
   * @return
   * @throws JC_Exception 
   */
  @Override
  public boolean isParent( final JC_Nodeable pChildNode ) throws JC_Exception {
    return pChildNode.getPath().startsWith( getPath() );
  }

//  private Collection<JC_Nodeable> _getNodes() throws JC_Exception {
    
//  }

  
  /** destroy
   * 
   */
  protected void destroy() {
    destroy( false );
    return;
  }

  /** _destroyProperties
   * 
   */
  private void _destroyProperties() {
    if (mProperties!=null) {
      final JC_Propertyable[] props = mProperties.toArray( TMPL_ARR_P );
      for( JC_Propertyable prop : props ) {
        if (prop instanceof JC_DefaultProperty) {
          JC_DefaultProperty dp = (JC_DefaultProperty)prop;
          dp.destroy();
        }
      }
      mProperties.clear();
      mProperties = null;
    }
    return;
  }
  
  /** destroy
   * 
   * @param pRefresh as refresh?
   */
  private void destroy( final boolean pRefresh ) {
    if (mChildNodes!=null) {
      final JC_Nodeable[] cnodes = mChildNodes.toArray( TMPL_ARR );
      for (JC_Nodeable cnode : cnodes) {
        if (cnode instanceof JC_DefaultNode) {
          final JC_DefaultNode dn = (JC_DefaultNode)cnode;
          dn.destroy();
        }
      }
      mChildNodes.clear();
      mChildNodes = null;
    }
    _destroyProperties();
    if (!pRefresh) {
      if (mParentNode!=null) {
        final JC_DefaultNode pdn = (JC_DefaultNode)mParentNode;
        if (pdn.mChildNodes!=null) {
          pdn.mChildNodes.remove( this );
        }
        mParentNode = null;
      }
//      mPath = null;
      setDestroyed();
//      mNodeProps.add( PROPS.DESTROYED );
    } else {
      setSynchronized( false );
    }
    return;
  }
    
  /** getNodesExt
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<? extends JC_Nodeable> getNodesExt() throws JC_Exception {
    return getNodes();
  }

  /** getNumberOfChildNodes
   *
   * @return
   */
  @Override
  public int getNumberOfChildNodes() throws JC_Exception {
    final int no;
    final Collection<JC_Nodeable>nodes = getNodes();
    if (nodes==null) {
      no = 0;
    } else {
      no = nodes.size();
    }
    return no;
  }

  /** getNodeList
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public List<JC_Nodeable> getNodeList() throws JC_Exception {
    List<JC_Nodeable>nodes = (List<JC_Nodeable>)getNodes();
    if (nodes==null) {
      nodes = Collections.EMPTY_LIST;
    }
    return nodes;
  }


  /** getNodes
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<JC_Nodeable> getNodes() throws JC_Exception {
    Collection<JC_Nodeable> nodes;
    if (isNew() && (mChildNodes==null)) {
      nodes = null;
//    } else if (mChildNodes==null) {
    } else if (!mNodeProps.contains( PROPS.GOTNODES )) {
      mChildNodes = (ArrayList<JC_Nodeable>)getNodes( null, null, null );
      mNodeProps.add( PROPS.GOTNODES );
    }
    if (mChildNodes==null) {
      nodes = null;
    } else {
      nodes = Collections.unmodifiableList(mChildNodes);
    }
    return nodes;
  }

  @Override
  public Collection<JC_Nodeable> getNodesAllProperties() throws JC_Exception {
    Collection<JC_Nodeable> nodes;
    if (isNew() && (mChildNodes==null)) {
    } else if (!mNodeProps.contains( PROPS.GOTNODES )) {
      JC_Params p = mClient.createParams( JC_RESTComm.GET ).cloneParams();
      p.setAllProperties( true );
      mChildNodes = (List<JC_Nodeable>)getNodes( p, null, null );
      mNodeProps.add( PROPS.GOTNODES );
    }
    if (mChildNodes==null) {
      nodes = null;
    } else {
      nodes = Collections.unmodifiableList(mChildNodes);
    }
    return nodes;
  }

  
  /** getNodesExt
   * 
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<? extends JC_Nodeable> getNodesExt( JC_Params pParams, JC_Filter pFilter, JC_Query pQuery ) throws JC_Exception {
    return getNodes( pParams, pFilter, pQuery );
  }

  /** getLongPolling
   * 
   * @return
   * @throws JC_Exception 
   */
  @Override
  public Map<String, String> getLongPolling() throws JC_Exception {
    final Map<String, String> lpoll = new HashMap<String, String>();
    final JC_Clientable client = getClient();
    final JC_RESTComm     comm = client.getRESTComm();
    final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
    final JC_Params params = client.createParams( JC_RESTComm.GET ).cloneParams();
    params.setLongPoll( true );
    HttpURLConnection conn = null;
    try {
      JC_Utils.buildURL( client, url, params, null, null );
      conn = comm.createHttpConnection( url.toString() );
      final JD_Taglist tags = comm.sendMessageGET( client, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        String result = JC_Utils.readAsString( is );
//          System.out.println("result = " + result );
        Properties pollprops = new Properties();
        pollprops.load( new ByteArrayInputStream( result.getBytes() ) );
        for( Map.Entry<Object,Object> entry : pollprops.entrySet() ) {
          String key = (String)entry.getKey();
          if (key.startsWith( "Event.Path." )) {
            lpoll.put( ((String)entry.getValue()).substring(getPath().length()+1), "" );
          }
        }
      }
    } catch( MalformedURLException mue ) {
      throw JC_Exception.createErrorException( mue.getMessage(), mue );
    } catch( IOException ioe ) {
      throw JC_Exception.createErrorException( ioe.getMessage(), ioe );
    } finally {
      if (conn!=null) {
        conn.disconnect();
      }
    }
    
    
    return lpoll;
  }
  
  /** getNodes
   * 
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<JC_Nodeable> getNodes( JC_Params pParams, final JC_Filter pFilter, final JC_Query pQuery ) throws JC_Exception {
    Collection<JC_Nodeable> nodes;
    HttpURLConnection conn = null;
    try {
      final JC_Clientable client = getClient();
      final JC_RESTComm     comm = client.getRESTComm();
      final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
      if (pParams==null) {
        pParams = client.createParams( JC_RESTComm.GET );
      }
      JC_Utils.buildURL( client, url, pParams, pFilter, pQuery );
//      gLog.log( Level.INFO, "GET: " + url.toString() );
      conn = comm.createHttpConnection( url.toString() );
//    long time = System.currentTimeMillis();
      final JD_Taglist tags = comm.sendMessageGET( client, conn );
//    System.out.println( "GET in : " + (System.currentTimeMillis()-time) );
//    time = System.currentTimeMillis();
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        nodes = new ArrayList<JC_Nodeable>();
        if (pParams!=null) {
          if (pParams.getOutputFormat()!=null) {
            if (pParams.getOutputFormat().equals( JC_Defs.OUTPUTTYPE_PROPERTIES )) {
              _parseChildNodes_Properties( is, client, nodes, pParams );
            } else {
              JC_Factory.ATOM_RETRIEVER.parseChildNodes( this, is, client, nodes );
//              _parseChildNodes_Atom( is, client, nodes );              
            }
          } else {
            JC_Factory.ATOM_RETRIEVER.parseChildNodes( this, is, client, nodes );
//            _parseChildNodes_Atom( is, client, nodes );            
          }
        } else {
          JC_Factory.ATOM_RETRIEVER.parseChildNodes( this, is, client, nodes );
//          _parseChildNodes_Atom( is, client, nodes );
        }
        // **** Options check
        if (pParams.getAllProperties()) {
          // **** Set all nodes to synchronized
          for( JC_Nodeable n : nodes ) {
            ((JC_DefaultNode)n).setSynchronized(true);
          }
        }
      } else {
        // **** ERROR
        final JC_HttpException e = JC_Utils.createCommException( tags, "while retrieving objects ", url.toString() );
        if (e.getHttpErrorCode().getErrorCode()==HttpURLConnection.HTTP_NOT_FOUND) {
          setSynchronized( true );
        }
        throw e;        
      }
//    } catch( FeedException fe ) {
//      throw JC_Exception.createErrorException( fe.getMessage(), fe );        
    } catch( MalformedURLException mue ) {
      throw JC_Exception.createErrorException( mue.getMessage(), mue );
    } catch( IOException ioe ) {
      throw JC_Exception.createErrorException( ioe.getMessage(), ioe );
    } finally {
      if (conn!=null) {
        conn.disconnect();
      }
    }
    return nodes;
  }

  /** _parseChildNodes_Properties
   * 
   * @param pStream
   * @param pClient
   * @param pNodes
   * @return
   */
  private Collection<JC_Nodeable> _parseChildNodes_Properties( final InputStream pStream, final JC_Clientable pClient,
                                        final Collection<JC_Nodeable>pNodes, final JC_Params pParams ) throws IOException, JC_Exception {
    final InputStreamReader isr = new InputStreamReader( pStream );
    final BufferedReader     br = new BufferedReader( isr );
    try {
      boolean allParams = false;
      if (pParams!=null) {
        allParams = pParams.getAllProperties();
//        final String gp = pParams.getOtherParameter( JC_Defs.PARAM_GETALLPROPS );
//        allParams = JC_Defs.TRUE.equals( gp );
//        if ((gp!=null) && (gp.equals( JC_Defs.TRUE ))) {
//          allParams = true;
//        }
      }
      long no = 1;
      final String path = getPath() + '/';
      String name, nt, line = br.readLine();
      String[] sline = null;
      while( line!=null ) {
        if ("".equals(line)) break;
        final String prefix = no + ".";
        if (sline==null) {
          sline = new String[2];
          final int ix = line.indexOf( '=' );
          if (ix!=-1) {
  //          sline[0] = line.substring( 0, ix );
            sline[1] = line.substring( ix+1 );
          }
//          sline = gPropPattern.split( line, 0 );
        }
        name = sline[1];
        line = br.readLine();
        final int ix = line.indexOf( '=' );
        sline = new String[2];
        if (ix!=-1) {
          sline[0] = line.substring( 0, ix );
          sline[1] = line.substring( ix+1 );          
        }
//        sline = gPropPattern.split( line, 0 );
        nt   = sline[1];
        final JC_DefaultNode nodeable = (JC_DefaultNode)pClient.createNodeClass( nt );
        nodeable.setParent( this );
        nodeable.setName(   name );
        nodeable.setNodeType( nt );
        nodeable.setPath( path + name );
        line = br.readLine();
        while( (line!=null) ) {
          if ("".equals(line)) break;            
          sline = new String[2];
          final int ixx = line.indexOf( '=' );
          if (ixx!=-1) {
            sline[0] = line.substring( 0, ixx );
            sline[1] = line.substring( ixx+1 );          
          }
  //        sline = gPropPattern.split( line, 0 );
          if (sline[0].startsWith( prefix )) {
            if (sline.length==2) {
              nodeable._addProperty( (String)sline[0].substring(prefix.length()),
                        URLDecoder.decode(sline[1],"UTF-8"), JC_PropertyType.TYPENAME_STRING );
            } else {
              nodeable._addProperty( (String)sline[0].substring(prefix.length()), "", JC_PropertyType.TYPENAME_STRING );
            }
          } else {
            break;
          }
          line = br.readLine();
        }
        nodeable.setSynchronized( allParams );
        pNodes.add( nodeable );
        no++;
      }
    } finally {
      br.close();
      isr.close();
    }
      
    return pNodes;
  }
  
//  /** _parseChildNodes_Atom
//   * 
//   * @param pStream
//   * @param pClient
//   * @param pNodes
//   * @return
//   * @throws com.sun.syndication.io.FeedException
//   * @throws org.jecars.client.JC_Exception
//   * @throws java.io.UnsupportedEncodingException
//   */
//  private Collection<JC_Nodeable> _parseChildNodes_Atom( InputStream pStream, JC_Clientable pClient, Collection<JC_Nodeable>pNodes ) throws FeedException, JC_Exception, UnsupportedEncodingException {
//    WireFeedInput wfinput = new WireFeedInput();
//    wfinput.setXmlHealerOn( false );
//    Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( pStream, JC_RESTComm.CHARENCODE ) );
//    _addChildNodes( atomFeed, pClient, pNodes );
//    return pNodes;
//  }
//
//  /** _addLinkInfo
//   * 
//   * @param pNode
//   * @param pLink
//   */
//  private void _addLinkInfo( final JC_DefaultNode pNode, final Link pLink ) {
//    if (pLink.getRel().equals( "self" )) {
//      JC_Path selfPath = new JC_Path( pLink.getHref() );
//      selfPath.ensureDecode();
//      pNode.mSelfLink = selfPath.toString();
//    }
//    return;
//  }

  
//  /** addChildNodes
//   * 
//   * @param pAtomFeed
//   * @param pClient
//   * @param pChildNodes
//   * @throws org.jecars.client.JC_Exception
//   */
//  private void _addChildNodes( Feed pAtomFeed, JC_Clientable pClient, Collection<JC_Nodeable>pChildNodes ) throws JC_Exception, UnsupportedEncodingException {
//
//    String selfurl = JC_Utils.getFullNodeURL( pClient, this ).toString();
// 
//    String nt;
//    Iterator entries = pAtomFeed.getEntries().iterator();
//    while (entries.hasNext()) {
//      Entry entry = (Entry)entries.next();
//      nt = _getNodeTypeFromCats( entry.getCategories() );
//      JC_DefaultNode nodeable = (JC_DefaultNode)pClient.createNodeClass( nt );
//      nodeable.setNodeType( nt );
////      nodeable._setNodeType( entry.getCategories() );
//      
//      List<Link> l = entry.getOtherLinks();
//      for (Link olink : l) {
//        if (olink.getRel().equals( "self" )) {
//          _addLinkInfo( nodeable, olink );
//          if (nodeable.mSelfLink.startsWith( selfurl )) {
//            String sl = nodeable.mSelfLink.substring( selfurl.length()+1 );
//            if (sl.indexOf('/')==-1) {
//              // **** Is a real child
//              nodeable.setParent(this);
//              JC_Path pathBuffer = new JC_Path(getPath());
//              pathBuffer.addChildPath( entry.getTitle() );
//              nodeable.setParent( this );
//              nodeable.setName( entry.getTitle() );
//              nodeable.setID(   entry.getId() );
//              nodeable.setJCPath( pathBuffer );
//            } else {
//              // **** This node is not a child... probably result of a query
//              nodeable.setParent( this );
//              nodeable.setName( entry.getTitle() );
//              nodeable.setID(   entry.getId() );
//              nodeable.setPath( nodeable.mSelfLink.substring( pClient.getServerPath().length() ) );
//              List<Content> conlist = entry.getContents();
//              for (Content cl : conlist) {
//                nodeable._addProperty( "jecars:Body", cl.getValue(), JC_PropertyType.TYPENAME_STRING );
//              }
//            }
//            break;
//          }
//        }
//      }
//
//      // **** Set Node properties
////      nodeable.populateProperties(entry);
//      pChildNodes.add(nodeable);
//    }
//    return;
//  }
   
//  /** addForeignProps
//   * 
//   * @param pAtomFeed
//   * @throws java.lang.Exception
//   */
//  private void _addForeignProps( Feed pAtomFeed ) throws JC_Exception, UnsupportedEncodingException, DataConversionException {
//   
//    final List<Element> fmc = (List<Element>)pAtomFeed.getForeignMarkup();
//    for (Element fmcEntry : fmc) {
////    Iterator fmi = fmc.iterator();
////    while (fmi.hasNext()) {
////      Element fmcEntry = (Element) fmi.next();
//      String name = fmcEntry.getNamespacePrefix() + ':' + JC_Utils.urlencode( fmcEntry.getName() );
////   System.out.println( "a=--0d -0ko " + name );
//      final Attribute typeAttr = fmcEntry.getAttribute( "type" );
//      Attribute multiAttr = fmcEntry.getAttribute( "multi" );
//      if (multiAttr!=null) {
//        if (!multiAttr.getBooleanValue()) multiAttr = null;
//      }
//      if (multiAttr!=null) {
//        // **** Multi value property
//        final List<Element> chs = (List<Element>)fmcEntry.getChildren();
//        if (chs.isEmpty()) {
//          _addMultiProperty( name, null, false );
//        } else {
//          for (Element citEntry : chs) {
//            name = citEntry.getNamespacePrefix() + ':' + JC_Utils.urlencode( citEntry.getName() );
//            _addMultiProperty( name, citEntry.getValue(), true );
//          }
//        }
///*
//        Iterator cit = chs.iterator();
//        //ArrayList<StringValue> vals = new ArrayList<StringValue>();
//        while (cit.hasNext()) {
//          Element citEntry = (Element) cit.next();
//          name = citEntry.getNamespacePrefix() + ":" + JC_Utils.urlencode( citEntry.getName() );              
////       System.out.println( "MVP  " + name + " = " +JC_Utils.urldecode( citEntry.getValue() )  );
////          _addMultiProperty( name, JC_Utils.urldecode( citEntry.getValue() ) );
//          _addMultiProperty( name, citEntry.getValue() );
//        }
// */
//      } else {
//        if (typeAttr!=null) {
//          _addProperty( name, fmcEntry.getValue(), typeAttr.getValue() );          
//        } else {
//          _addProperty( name, fmcEntry.getValue(), JC_PropertyType.TYPENAME_UNDEFINED );
//        }
////        _addProperty( name, JC_Utils.urldecode(fmcEntry.getValue()) );
//      }
//    }
//    return;
//  }
  

  /** getProperties
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<JC_Propertyable> getProperties() throws JC_Exception {
    if (!isSynchronized()) {
      _destroyProperties();
      populateProperties( null );
    }
//    return (Collection<JC_Propertyable>)mProperties.clone();
    return Collections.unmodifiableList( mProperties );
  }    

  /** getOrCreateProperties
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_Propertyable> getOrCreateProperties() {
    if (mProperties==null) {
      mProperties = new ArrayList<JC_Propertyable>();
    }
    return mProperties;
  }

  /** getChildNodes
   * @return
   */
  private List<JC_Nodeable> getChildNodes() {
    return mChildNodes;
  }
  
  /** getOrCreateChildNodes
   * @return
   */
  private List<JC_Nodeable> getOrCreateChildNodes() {
    if(mChildNodes==null) {
      mChildNodes = new ArrayList<JC_Nodeable>();
    }
    return mChildNodes;
  }
  
  
  /** setPath
   * @param pPath
   */
  public void setPath( final String pPath ) {
    mPath = new JC_Path( pPath );
    return;
  }

  /** setPath
   * WARNING(!) the parameter is not(!!) cloned but is used as reference
   * @param pPath
   */
  public void setJCPath( final JC_Path pPath ) {
    mPath = pPath;
    return;
  }


  /** getJCPath
   * WARNING(!) the return parameter is not(!!) cloned but is used as reference
   * @return
   */
  public JC_Path getJCPath() {
    return mPath;
  }

  /** getPath_JC, creates(!) a JC_Path and returns it
   * @return
   */
  @Override
  public JC_Path getPath_JC() {
    return (JC_Path)mPath.clone();
  }


  /** getPath
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public String getPath() throws JC_Exception {
    if (mPath!=null) {
      return mPath.toString();
    }
    throw new JC_Exception( "Path is empty." );
  }

  @Override
  public String getMimeType() {
    try {
      return getProperty( "jcr:mimeType").getValueString();
    } catch( JC_Exception je ) {
      return "";
    }
  }

  /** getName
   * @return
   */
  @Override
  public String getName() {
    final String name = super.getName();
    if (name==null) {
      setName( getJCPath().getChild() );
    } else {
      return name;
    }
    return super.getName();
  }
  
  /** setClient
   * 
   * @param pC 
   */
  public void setClient( final JC_Clientable pC ) {
    mClient = pC;
    return;
  }

  /** getClient
   * @return
   */
  @Override
  public JC_Clientable getClient() {
    if (mClient==null) {
      JC_Nodeable n = this;
      JC_Clientable c;
      n = ((JC_DefaultNode)n).getParentNotSynchronized();
      c = n.getClient();
      return c;
    }
    return mClient;
  }
  
  /** getParentNotSynchronized
   * lightweight: returns parent node without unnecessary JeCARS communication
   * 
   * @return
   */
  private JC_Nodeable getParentNotSynchronized() {
    return mParentNode;
  }
  
  /** getParent
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getParent() throws JC_Exception {
    if(!((JC_DefaultNode)mParentNode).isSynchronized()) {
      ((JC_DefaultNode)mParentNode).populateProperties( null );
    }
    return mParentNode;
  }

  /** getRights
   *
   * @param pPrincipal
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Rights getRights( final String pPrincipal ) throws JC_Exception {      
    JC_Rights rights = new JC_Rights();
    final JC_Clientable client = getClient();
    final JD_Taglist    tags;
    final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
    final JC_RESTComm comm = client.getRESTComm();
    final JC_Params p = client.createParams( JC_RESTComm.GET ).cloneParams();
    p.addOtherParameter( "rights", pPrincipal );
    try {
      JC_Utils.buildURL( client, url, p, null, null );
      final HttpURLConnection conn = comm.createHttpConnection( url.toString() );
      tags = comm.sendMessageGET( client, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        rights = JC_Factory.ATOM_RETRIEVER.getRights( this, is );
      } else {
        // **** ERROR
        throw JC_Utils.createCommException( tags, "while retrieving rights ", url.toString() + " for " + pPrincipal );
      }
    } catch( IOException ie ) {
      throw new JC_Exception(ie);
    }
    return rights;
  }
      
      
//    final     JC_Rights rights = new JC_Rights();
//    final JC_Clientable client = getClient();
//    final JD_Taglist    tags;
//    final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
//    try {
//      final JC_RESTComm comm = client.getRESTComm();
//      final JC_Params p = client.createParams( JC_RESTComm.GET ).cloneParams();
//      p.addOtherParameter( "rights", pPrincipal );
//      JC_Utils.buildURL( client, url, p, null, null );
//      final HttpURLConnection conn = comm.createHttpConnection( url.toString() );
//      tags = comm.sendMessageGET( client, conn );
//      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
//        final InputStream is = JC_RESTComm.getResultStream( tags );
//        final WireFeedInput wfinput = new WireFeedInput();
//        wfinput.setXmlHealerOn( false );
//        final Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( is, JC_RESTComm.CHARENCODE ));
//        final List fmc = (List)atomFeed.getForeignMarkup();
//        final Iterator fmi = fmc.iterator();
//        while (fmi.hasNext()) {
//          final Element fmcEntry = (Element)fmi.next();
//          final String name = fmcEntry.getNamespacePrefix() + ":" + JC_Utils.urlencode( fmcEntry.getName() );
//          if ("jecars:Actions".equals( name )) {
//            rights.addRight( fmcEntry.getValue() );
//          }
//        }
//      } else {
//        // **** ERROR
//        throw JC_Utils.createCommException( tags, "while retrieving rights ", url.toString() + " for " + pPrincipal );
//      }
//    } catch( FeedException fe ) {
//      throw JC_Exception.createErrorException( fe.getMessage(), fe );
//    } catch( MalformedURLException mue ) {
//      throw JC_Exception.createErrorException( mue.getMessage(), mue );
//    } catch( IOException ioe ) {
//      throw JC_Exception.createErrorException( ioe.getMessage(), ioe );
//    }
//
//    return rights;
//  }

  /** getNode
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getNode( final String pName ) throws JC_Exception {
    return getNode( pName, true );
  }

   /** getNode
   * 
   * @param pName
   * @param pRetrieve if true then the node actually retrieved from the JeCARS server
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getNode( final String pName, final boolean pRetrieve ) throws JC_Exception {
    return getNode( pName, pRetrieve, null );
  }

  /** getNode
   * 
   * @param pName
   * @param pParams
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getNode( final String pName, final JC_Params pParams ) throws JC_Exception {
    return getNode( pName, true, pParams );    
  }


  /** _peekNode
   *
   *
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  private JC_Nodeable _peekNode( final String pName ) throws JC_Exception {
    final List<JC_Nodeable> nodes = getChildNodes();
    JC_Nodeable resnode = null;
    if (nodes!=null) {
      for (JC_Nodeable node : nodes ) {
        if ((!node.isInErrorState()) && (!node.isDestroyed())) {
          if ((pName.equals(node.getName())) ||
               pName.equals(node.getProperty( JC_Defs.ATOM_TITLE ).getValue())) {
            if (node.isNew() || node.isSynchronized()) { // **** When a node is just added it must be find with hasNode()
              resnode = node;
              break;
            }
          }
        }
      }
    }
    return resnode;
  }

  /** _peekNodeOnName
   * 
   * @param pName
   * @return
   * @throws JC_Exception
   */
  private JC_Nodeable _peekNodeOnName( final String pName ) throws JC_Exception {
    List<JC_Nodeable> nodes = getChildNodes();
    if (nodes!=null) {
      for (JC_Nodeable node : nodes ) {
        if (pName.equals(node.getName())) {
          return node;
        }
      }
    }
    return null;
  }


  /** getNode
   * 
   * @param pName
   * @param pRetrieve if true then the node actually retrieved from the JeCARS server
   * @param pParams
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getNode( final String pName, final boolean pRetrieve, final JC_Params pParams ) throws JC_Exception {

    if ((pName==null) || ("".equals( pName ))) {
      throw new JC_Exception( "Cannot find path " + pName + "." );
    }

    if (pName.indexOf( '/' )!=-1) {
      // **** The pName is a path
      final StringTokenizer st = new StringTokenizer( pName, "/" );
      JC_Nodeable runNode = this;
      while( st.hasMoreTokens() ) {
        runNode = runNode.getNode( st.nextToken(), pRetrieve, pParams );
      }
      return runNode;
    }
    
    Collection<JC_Nodeable> nodes = getChildNodes();
    if (nodes!=null) {
      for (JC_Nodeable node : nodes ) {
        boolean or = pName.equals(node.getName());
        if ((!or) && (!node.isDestroyed())) {
          final JC_Propertyable prop = node.getProperty( JC_Defs.ATOM_TITLE, pRetrieve, pParams );
          if ((prop!=null) && (pName.equals( prop.getValue() ) )) {
            or = true;
          }
        }
        if (or) {
          if (node.isSynchronized()) {
            return node;
          } else {
            final JC_DefaultNode dnode = (JC_DefaultNode)node;
            dnode.setParent( this );
            dnode.populateProperties( pParams );            
            return dnode;
          }
        }
      }
    }
    if (mNodeProps.contains( PROPS.GOTNODES )) {
      final JC_HttpException e = JC_Utils.createCommException( null, "while retrieving object ", pName );
      throw e;
    }
    final JC_Clientable client = getClient();
    final JC_DefaultNode nodeable = (JC_DefaultNode)client.createNodeClass( JC_Clientable.CREATENODE_DEFAULT );
    nodeable.setParent(this);
    final JC_Path pathBuffer = (JC_Path)getJCPath().clone();
    pathBuffer.addChildPath( pName );
    nodeable.setJCPath( pathBuffer );
    if (pRetrieve) {
      nodeable.populateProperties( pParams );
    }
    nodes = getOrCreateChildNodes();
    nodes.add( nodeable );
    return nodeable;
  }

  /** resolve
   * 
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_Nodeable resolve() throws JC_Exception {
    if (hasProperty( "jecars:Link"  )) {
//      final JC_Nodeable n = getClient().getNode( getProperty( "jecars:Link" ).getValueString() );
      final JC_Nodeable n = getClient().getSingleNode( getProperty( "jecars:Link" ).getValueString() );
      if (n.getPath().equals( getPath() )) {
        return n;
      } else {
        return n.resolve();
      }
    }
    return morphToNodeType();
  }

  /** getResolvedNode
   * 
   * @param pName
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_Nodeable getResolvedNode( final String pName ) throws JC_Exception {
    return getResolvedNode( pName, getClient().createParams( JC_RESTComm.GET ) );
  }


  /** getResolvedNode
   *
   * @param pName
   * @param pRetrieve if true then the node actually retrieved from the JeCARS server
   * @param pParams
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getResolvedNode( final String pName, final JC_Params pParams ) throws JC_Exception {

    if ((pName==null) || ("".equals( pName ))) {
      throw new JC_Exception( "Cannot find path " + pName + "." );
    }

    final String nodeName;

    // **** Check for path in the pName
    final int ix = pName.indexOf( '/', 0 );
    if (ix!=-1) {
      nodeName = pName.substring( 0, ix );
    } else {
      nodeName = pName;
    }

    Collection<JC_Nodeable> nodes = getChildNodes();
    if (nodes!=null) {
      for (JC_Nodeable node : nodes ) {
        boolean or = nodeName.equals(node.getName());
        if (!or) {
          final JC_Propertyable prop = node.getProperty( JC_Defs.ATOM_TITLE, true );
          if ((prop!=null) && (nodeName.equals( prop.getValue() ) )) {
            or = true;
          }
        }
        if (or) {
          if (node.isSynchronized()) {
            if (ix!=-1) {
              return node.getResolvedNode( pName.substring( ix+1 ), pParams );
            } else {
              return node;
            }
          } else {
            JC_DefaultNode dnode = (JC_DefaultNode)node;
            dnode.populateProperties( pParams );
            if (ix!=-1) {
              return dnode.getResolvedNode( pName.substring( ix+1 ), pParams );
            } else {
              return dnode;
            }
          }
        }
      }
    }
    JC_Clientable client = getClient();
    final JC_DefaultNode nodeable = (JC_DefaultNode)client.createNodeClass( JC_Clientable.CREATENODE_DEFAULT );
    nodeable.setParent(this);
    JC_Path pathBuffer = (JC_Path)getJCPath().clone();
    pathBuffer.addChildPath( nodeName );
    nodeable.setJCPath( pathBuffer );
    nodeable.populateProperties( pParams );
    nodes = getOrCreateChildNodes();
    nodes.add( nodeable );

    if (ix!=-1) {
      return nodeable.getResolvedNode( pName.substring( ix+1 ), pParams );
    }
    return nodeable;
  }

  /** hasNode
   * When the node isn't found in the cache a HEAD call will be performed to check
   * if the object is available at the server.
   *
   * @param pName
   * @return
   */
  @Override
  public boolean hasNode( final String pName ) throws JC_Exception {
    try {
      if ("".equals( pName )) return false;
      if (_peekNode( pName )==null) {
        final JC_Clientable client = getClient();
        JD_Taglist            tags = null;
        final StringBuilder    url = JC_Utils.getFullNodeURL( client, this ).append( "/" ).append( pName );
        final JC_RESTComm     comm = client.getRESTComm();
        JC_Utils.buildURL( client, url, null, null, null );
        HttpURLConnection conn = comm.createHttpConnection( url.toString() );
        tags = comm.sendMessageHEAD( client, conn );
        if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
          return true;
        }
      } else {
        return true;
      }
    } catch( IOException ioe ) {
      throw new JC_Exception( ioe );
    }
    return false;
  }

  /** hasNode
   *
   * @param pName
   * @param pGetNodes if true then a getnodes() is performed (before the hasNode check) when the cache is empty.
   * @param pDoHead if true then when the node isn't found in the cache a HEAD call
   *                will be performed to check if the object is available at the server.
   * @return
   * @throws JC_Exception
   */
  @Override
  public boolean hasNode( final String pName, final boolean pGetNodes, final boolean pDoHead ) throws JC_Exception {
    if ((pGetNodes) && (mChildNodes==null)) {
      getNodes();
    }
    if (pDoHead) {
      return hasNode( pName );
    }
    return _peekNode( pName )!=null;
  }

  /** hasNodeNameCheck, the same as hasNode(...) but only a getName() check is done, not a ATOM_TITLE check
   *
   * @param pName
   * @param pGetNodes if true then a getnodes() is performed (before the hasNode check) when the cache is empty.
   * @param pDoHead if true then when the node isn't found in the cache a HEAD call
   *                will be performed to check if the object is available at the server.
   * @return
   * @throws JC_Exception
   */
  @Override
  public boolean hasNodeNameCheck( final String pName, final boolean pGetNodes, final boolean pDoHead ) throws JC_Exception {
    if ((pGetNodes) && (mChildNodes==null)) {
      getNodes();
    }
    if (pDoHead) {
      return hasNode( pName );
    }
    return _peekNodeOnName( pName )!=null;
  }

  /** checkProperty
   * 
   * @param pName
   * @return
   */
  public JC_Propertyable checkProperty( final String pName ) {
    if (mProperties!=null) {
      for( JC_Propertyable prop : mProperties ) {
        if (prop.getName().equals( pName )) return prop;
      }
    }      
    return null;
  }
  
  /** hasProperty
   * @param pName
   * @return
   */
  @Override
  public boolean hasProperty( final String pName ) throws JC_Exception {
    try {
      getProperty( pName );
      return true;
    } catch( JC_Exception e ){
      return false;
    }
  }

  /** hasProperty
   * 
   * @param pName
   * @param pRetrieve
   * @return
   * @throws JC_Exception 
   */
  @Override
  public boolean hasProperty( final String pName, final boolean pRetrieve ) throws JC_Exception {
    try {
      getProperty( pName, pRetrieve );
      return true;
    } catch( JC_Exception e ){
      return false;
    }
  }

  
  /** _getProperty
   * 
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  private JC_Propertyable _getProperty( final String pName ) throws JC_Exception {
    if (mProperties!=null) {
      for (JC_Propertyable prop : mProperties) {
        if (prop.getName().equals(pName)) {
          return prop;
        }
      }
    }
    return null;
  }

  /** getPropertyStream
   * 
   * @param pName
   * @param pProps
   * @param pOffset =-1 not used
   * @param pLength =-1 not used
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Streamable getPropertyStream( final String pName, final EnumSet<JC_StreamProp> pProps, final long pOffset, final long pLength ) throws JC_Exception {
    
    final JC_Clientable client = getClient();
    final JC_RESTComm     comm = client.getRESTComm();
    final JC_Params          p = client.createParams( JC_RESTComm.GET ).cloneParams();
//    p.addOtherParameter( "rights", pPrincipal );
    final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
    try {
      p.setPropertyName( pName );
      if ((pOffset==-1) && (pLength==-1)) {
        JC_Utils.buildURL( client, url, p, null, null );
      } else {
        JC_Query query = JC_Query.createQuery();
        if (!pProps.contains( JC_StreamProp.FRAGMENT )) {
          if (pOffset!=-1) query.setStartIndex( pOffset );
          if (pLength!=-1) query.setMaxResults( pLength );
        }
        JC_Utils.buildURL( client, url, p, null, query );
      }
      final HttpURLConnection conn = comm.createHttpConnection( url.toString() );
      if (pProps.contains( JC_StreamProp.FRAGMENT )) {
        conn.setRequestProperty( "Range", "bytes=" + pOffset + "-" + (pOffset+pLength-1) );
      }
      JD_Taglist tags = comm.sendMessageGET( client, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        final JC_Streamable stream = JC_DefaultStream.createStream( is, "" );
        return stream;
      } else if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_PARTIAL) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        final JC_Streamable stream = JC_DefaultStream.createStream( is, "" );
        return stream;
      } else {
        // **** ERROR
        final JC_HttpException e = JC_Utils.createCommException( tags, "while retrieving property ", url.toString() );
        throw e;
      }
    } catch( IOException ioe ) {
      throw JC_Exception.createErrorException( ioe.getMessage(), ioe );
    }
  }

  /** getProperty
   * 
   * @param pName
   * @param pRetrieve
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable getProperty( final String pName, final boolean pRetrieve ) throws JC_Exception {
    if (!pRetrieve) {
      return _getProperty( pName );
    }
    return getProperty( pName );
  }

  /** getProperty
   * 
   * @param pName
   * @param pRetrieve
   * @param pParams
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Propertyable getProperty( final String pName, final boolean pRetrieve, final JC_Params pParams ) throws JC_Exception {
    if (!pRetrieve) {
      return _getProperty( pName );
    }
    return getProperty( pName, pParams );
  }

  /** getProperty
   *
   * @param pName
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_Propertyable getProperty( final String pName ) throws JC_Exception {
    return getProperty( pName, null );
  }


  /** getProperty
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable getProperty( final String pName, final JC_Params pParams ) throws JC_Exception {
    JC_Propertyable prop;
    if (mProperties==null) {
      if (!isNew() && (!isDestroyed())) {
        populateProperties( pParams );
      }
    } else {
      prop = _getProperty( pName );
      if (prop!=null) {
        return prop;
      }
      if ((!isNew()) && (!isSynchronized())) {
        mProperties.clear();
        mProperties = null;
        populateProperties( pParams );
      }
    }
    prop = _getProperty( pName );
    if (prop==null) {
      throw JC_Exception.createErrorException( JC_Exception.ERROR_PROPERTYNOTFOUND, "No such property: " + getPath() + "/" + pName );
    }
    return prop;
  }


  /** addPermissionNode
   * Created information is not saved
   *
   * @param pName
   * @param pPrincipal
   * @param pRights
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_PermissionNode addPermissionNode( final String pName, final JC_Nodeable pPrincipal, final Collection<String>pRights ) throws JC_Exception {
    final JC_PermissionNode perm;
    if (pName==null) {
      perm = (JC_PermissionNode)addNode( "P_" + pPrincipal.getName(), "jecars:Permission" ).morphToNodeType();
      perm.addRights( pPrincipal, pRights );
    } else {
      perm = (JC_PermissionNode)addNode( pName, "jecars:Permission" ).morphToNodeType();
      if (pRights!=null) {
        perm.addRights( pPrincipal, pRights );
      }
    }
    return perm;
  }

  /** getOrAddNode
   * Get the node or add it, if it isn't there
   * @param pName
   * @param pNodeType
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable getOrAddNode( final String pName, final String pNodeType ) throws JC_Exception {
    if (hasNode( pName )) {
      return getNode( pName );
    }
    return addNode( pName, pNodeType );
  }

  /** addNode
   * @param pName
   * @param pNodeType
   * @return
   */
  @Override
  public JC_Nodeable addNode( final String pName, final String pNodeType ) throws JC_Exception {         
    JC_DefaultNode nodeable;
    try {
      final JC_Clientable client = getClient();
      nodeable = (JC_DefaultNode)client.createNodeClass( pNodeType );
      nodeable.setNew( true );
      
      // **** Set Node meta-data
      nodeable.setName( pName );
      nodeable.setParent( this );
      final JC_Path pathBuffer = new JC_Path(getPath());
      pathBuffer.addChildPath(pName);
      nodeable.setJCPath( pathBuffer );

      // **** Set Node properties
      final int ix = pNodeType.indexOf('|');
      if (ix==-1) {
        nodeable._addProperty( JC_Defs.ATOM_CATEGORY, pNodeType, JC_PropertyType.TYPENAME_STRING );
      } else {
        nodeable._addProperty( JC_Defs.ATOM_CATEGORY, pNodeType.substring( 0, ix ), JC_PropertyType.TYPENAME_STRING );
        final String nt = pNodeType.substring( ix+1 );
        final JC_Propertyable prop = nodeable._addProperty( JC_Defs.UNSTRUCT_NODETYPE, nt, JC_PropertyType.TYPENAME_STRING );
        prop.setNew( true );
      }
      nodeable._addProperty( JC_Defs.ATOM_TITLE, pName, JC_PropertyType.TYPENAME_STRING );

      getOrCreateChildNodes().add(nodeable);
    } catch( Exception e ) {
      throw new JC_Exception( e );
    }
    return nodeable;    
  }

  /** addNodes
   * 
   * @param pName
   * @param pNodeType
   * @param pFolderNodeType
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_Nodeable addNodes( final String pName, final String pNodeType, final String pFolderNodeType ) throws JC_Exception {
    StringTokenizer st = new StringTokenizer( pName, "/" );
    JC_Nodeable result = null, run = this;
    while( st.hasMoreTokens() ) {
      final String nname = st.nextToken();
      if (run.hasNode( nname )) {
        if (st.hasMoreTokens()) {
          run = run.getNode( nname );
        } else {
          result = run.getNode( nname );          
        }
      } else {
        if (st.hasMoreTokens()) {
          run = run.addNode( nname, pFolderNodeType );
          run.save();
        } else {
          result = run.addNode( nname, pNodeType );
          result.save();          
        }
      }
    }
    return result;
  }
  
  /** addNode
   * 
   * @param pName
   * @param pNodeType
   * @param pLinkVia_URL
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Nodeable addNode( final String pName, final String pNodeType, final String pLinkVia_URL ) throws JC_Exception {
    final JC_Nodeable nodeable = addNode(pName, pNodeType);
    ((JC_DefaultNode)nodeable).createUsingCopy(true);
    ((JC_DefaultNode)nodeable).setSourceObjectUrl(pLinkVia_URL);
    return nodeable;
  }

  /** Add a complete node path, which means that the pPath is it path which will be created (if it isn't there),
   *  Every node type will be pFolderNodeType, except for the last node, which will be of type pNodeType
   *
   * @param pPath
   * @param pNodeType
   * @param pFolderNodeType
   * @return
   * @throws JC_Exception
   */
  @Override
  public JC_Nodeable addNodePath( final String pPath, final String pNodeType, final String pFolderNodeType ) throws JC_Exception {
    String paths[] = pPath.split( "/" );
    JC_Nodeable node = _addNodePath( paths, 0, pNodeType, pFolderNodeType );
    return node;
  }

  /** _addNodePath
   *
   * @param pPath
   * @param pPathIndex
   * @param pNodeType
   * @param pFolderNodeType
   * @return
   * @throws JC_Exception
   */
  private JC_Nodeable _addNodePath( final String[] pPath, final int pPathIndex, final String pNodeType, final String pFolderNodeType ) throws JC_Exception {
    if (hasNode( pPath[pPathIndex] )) {
      if (pPathIndex+1==pPath.length) {
        return getNode( pPath[pPathIndex] );
      } else {
        return ((JC_DefaultNode)getNode( pPath[pPathIndex] ))._addNodePath( pPath, pPathIndex+1, pNodeType, pFolderNodeType );
      }
    } else {
      if (pPathIndex+1==pPath.length) {
        JC_Nodeable res = addNode( pPath[pPathIndex], pNodeType );
        res.save();
        return res;
      } else {
        JC_DefaultNode dn = (JC_DefaultNode)addNode( pPath[pPathIndex], pFolderNodeType );
        dn.save();
        return dn._addNodePath( pPath, pPathIndex+1, pNodeType, pFolderNodeType );
      }
    }
  }

  /** addMixin adds a mixin nodetype to the current nodetype.
   * 
   * @param pMixin
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void addMixin( final String pMixin ) throws JC_Exception {
    setProperty( "jcr:mixinTypes", "+" + pMixin );
    return;
  }

  /**  removeMixin removes a mixin nodetype from the current nodetype.
   *
   * @param pMixin
   * @throws JC_Exception
   */
  @Override
  public void removeMixin( final String pMixin ) throws JC_Exception {
    setProperty( "jcr:mixinTypes", "-" + pMixin );
    return;
  }

  /** setProperty
   *
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final boolean pValue ) throws JC_Exception {
    return _setProperty( pName, (Object)pValue );
  }


  /** setProperty
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final JC_Streamable pValue ) throws JC_Exception {
    setProperty( "jcr:mimeType", pValue.getContentType() );
    return _setProperty( "jcr:data", (Object)pValue );    
  }


  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final Calendar pValue ) throws JC_Exception {
    return setProperty( pName, ISO8601.format(pValue) );
  }

  
  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final JC_Streamable pValue ) throws JC_Exception {
    return _setProperty( pName, (Object)pValue );    
  }

  
  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final long pValue ) throws JC_Exception {
    return _setProperty( pName, (Object)pValue );    
  }

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final double pValue ) throws JC_Exception {
    return _setProperty( pName, (Object)pValue );    
  }
  
  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final String pValue ) throws JC_Exception {
    return _setProperty( pName, (String)pValue );
  }

  /** setProperty
   * 
   * @param pName
   * @param pNode
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setProperty( final String pName, final JC_Nodeable pNode ) throws JC_Exception {
    return _setProperty( pName, pNode );
  }

  
  /** setProperty
   * @param pName, property names should have a prefix
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  private JC_Propertyable _setProperty( final String pName, final Object pValue ) throws JC_Exception {
    JC_Propertyable prop = null;
//    try {
//      if (pName.indexOf( ':' )==-1) throw new JC_Exception( "Property name should have a prefix: " + pName );
      if (hasProperty( pName )) {
        prop = getProperty( pName );
      } else {
        prop = new JC_DefaultProperty( this );
        prop.setNew( true );
        if(!this.isNew()) {
          this.setChanged(true);
        }
      }
      if (prop instanceof JC_MultiValueProperty) {
        // **** Is multi value property
        if (pValue instanceof String) {
          final String value = (String)pValue;
          if (value.startsWith( "+" )) {
            prop.addValue( value.substring(1), true );
          } else if (value.startsWith( "-" )) {
            prop.removeValue( value.substring(1) );
          }
          setChanged( true );
        }
      } else if (prop instanceof JC_DefaultProperty) {
        // **** Is normal property?
        if (prop.getValue()==null) {
          if (pValue==null) {
            ((JC_DefaultProperty)prop)._setValue( pName, (String)null );
          } else {
            if(!prop.isNew()) {
              prop.setChanged( true );
              setChanged(true);
            }
            if (pValue instanceof String) 
              ((JC_DefaultProperty)prop)._setValue( pName, (String)pValue );
            else if (pValue instanceof JC_Streamable)
              ((JC_DefaultProperty)prop)._setValue( pName, (JC_Streamable)pValue );            
            else if (pValue instanceof Double)
              ((JC_DefaultProperty)prop)._setValue( pName, ((Double)pValue).doubleValue() );
            else if (pValue instanceof Long)
              ((JC_DefaultProperty)prop)._setValue( pName, ((Long)pValue).longValue() );
            else if (pValue instanceof Boolean)
              ((JC_DefaultProperty)prop)._setValue( pName, ((Boolean)pValue).booleanValue() );
            else if (pValue instanceof JC_Nodeable)
              ((JC_DefaultProperty)prop)._setValue( pName, (JC_Nodeable)pValue );
            else 
              throw JC_Exception.createErrorException( pName + " unknown datatype: " + pValue.getClass() );
          }
        } else if (prop.getValue().equals(pValue)==false) {
          prop.setChanged( true );
          if (pValue instanceof String)
            ((JC_DefaultProperty)prop)._setValue( pName, (String)pValue );
          else if (pValue instanceof JC_Streamable)
            ((JC_DefaultProperty)prop)._setValue( pName, (JC_Streamable)pValue );
          else if (pValue instanceof Long)
            ((JC_DefaultProperty)prop)._setValue( pName, ((Long)pValue).longValue() );
          else if (pValue instanceof Double)
            ((JC_DefaultProperty)prop)._setValue( pName, ((Double)pValue).doubleValue() );
          else if (pValue instanceof Boolean)
            ((JC_DefaultProperty)prop)._setValue( pName, ((Boolean)pValue).booleanValue() );
          else if (pValue instanceof JC_Nodeable)
            ((JC_DefaultProperty)prop)._setValue( pName, (JC_Nodeable)pValue );
          else
            throw JC_Exception.createErrorException( pName + " unknown datatype: " + pValue.getClass() );
          setChanged(true);
        }
      }
      Collection<JC_Propertyable> props = getOrCreateProperties();
      if (prop.isNew()) {
        if (!props.contains( prop )) {
          props.add( prop );
        }
      }
//    } catch( UnsupportedEncodingException uee ) {
//      throw new JC_Exception( uee );
//    }
    return prop;
  }
  
  /** addProperty
   * should only be called for creating local copy of remote JeCARS Node properties
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_Propertyable _addProperty( final String pName, final String pValue, final String pType ) throws JC_Exception, UnsupportedEncodingException {      
    JC_DefaultProperty prop = (JC_DefaultProperty)checkProperty( pName );
    if (prop==null) {
      prop = new JC_DefaultProperty( this );
      prop.setPropertyType( pType );
      if (pType.equals(JC_PropertyType.TYPENAME_LONG)) {
        prop._setValue( pName, Long.parseLong(pValue) );
      } else if (pType.equals(JC_PropertyType.TYPENAME_DOUBLE)) {
        prop._setValue( pName, Double.parseDouble(pValue) );
      } else if (pType.equals(JC_PropertyType.TYPENAME_BOOLEAN)) {
        prop._setValue( pName, Boolean.parseBoolean(pValue) );
      } else {
        prop._setValue( pName, pValue );
      }
      final Collection<JC_Propertyable> props = getOrCreateProperties();
      props.add( prop );
    }
    return prop;
  }

  /** _addMultiProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.UnsupportedEncodingException
   */
  private JC_Propertyable _addMultiProperty( final String pName, final String pValue, final boolean pNullAllowed ) throws JC_Exception, UnsupportedEncodingException {
    JC_DefaultProperty prop = (JC_DefaultProperty)checkProperty( pName );
    if (prop==null) {
      prop = new JC_MultiValueProperty( this );
      prop.setName( pName );
      if (pNullAllowed || (pValue!=null)) {
        prop.addValue( pValue, false );
      }
      Collection<JC_Propertyable> props = getOrCreateProperties();
      props.add( prop );
    } else {
      if (pNullAllowed || (pValue!=null)) {
        prop.addValue( pValue, false );
      }
    }
    return prop;
  }

  /** replaceMultiValueProperty
   * @param pName
   * @param values
   * @return
   */
  @Override
  public JC_Propertyable replaceMultiValueProperty( final String pName, final Collection<String>pValues ) throws JC_Exception {
    final JC_MultiValueProperty property = new JC_MultiValueProperty(this);
    property.setRemoveAllProperties( true );
    property.setName( pName );
    property.setValues( pValues );
    Collection<JC_Propertyable> props = getOrCreateProperties();
    props.add(property);
    return property;
  }

  
  /** replaceMultiValuePropertyL
   * 
   * @param pName
   * @param pValues
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Propertyable replaceMultiValuePropertyL( final String pName, final Collection<Long>pValues ) throws JC_Exception {
    final JC_MultiValueProperty property = new JC_MultiValueProperty(this);
    property.setRemoveAllProperties( true );
    property.setName( pName );
    property.setValuesL( pValues );
    Collection<JC_Propertyable> props = getOrCreateProperties();
    props.add(property);
    return property;
  }


  
  /** setMultiValueProperty
   * @param pName
   * @param values
   * @return
   */
  @Override
  public JC_Propertyable setMultiValueProperty( final String pName, final Collection<String>pValues ) throws JC_Exception {
    final Collection<JC_Propertyable> props = getOrCreateProperties();
    JC_MultiValueProperty property = (JC_MultiValueProperty)getProperty( pName, false );
    if (property!=null) {
      property.removeAllValues();
      props.remove( property );
    }
    property = new JC_MultiValueProperty(this);
    property.setName( pName );
    property.setValues( pValues);
    props.add(property);    
    return property;
  }

  /** setMultiValuePropertyD
   *
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setMultiValuePropertyD( final String pName, final Collection<Double>  pValues) throws JC_Exception {
    final Collection<JC_Propertyable> props = getOrCreateProperties();
    JC_MultiValueProperty property = (JC_MultiValueProperty)getProperty( pName, false );
    if (property!=null) {
      property.removeAllValues();
      props.remove( property );
    }
    property = new JC_MultiValueProperty(this);
    property.setName(pName);
    property.setValuesD( pValues );
    props.add(property);
    return property;
  }

  /** setMultiValuePropertyL
   *
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setMultiValuePropertyL( final String pName, final Collection<Long>    pValues) throws JC_Exception {
    final Collection<JC_Propertyable> props = getOrCreateProperties();
    JC_MultiValueProperty property = (JC_MultiValueProperty)getProperty( pName, false );
    if (property!=null) {
      property.removeAllValues();
      props.remove( property );
    }
    property = new JC_MultiValueProperty(this);
    property.setName(pName);
    property.setValuesL( pValues );
    props.add(property);
    return property;
  }

  /** setMultiValuePropertyB
   *
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Propertyable setMultiValuePropertyB( final String pName, final Collection<Boolean> pValues) throws JC_Exception {
    final Collection<JC_Propertyable> props = getOrCreateProperties();
    JC_MultiValueProperty property = (JC_MultiValueProperty)getProperty( pName, false );
    if (property!=null) {
      property.removeAllValues();
      props.remove( property );
    }
    property = new JC_MultiValueProperty(this);
    property.setName(pName);
    property.setValuesB( pValues );
    props.add(property);
    return property;
  }


  /** addBatch
   * @param pInput
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void addBatch( final InputStream pInput) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /** moveNode
   * Move this node, only this node object is changed
   *
   * @param pNewName
   * @return true if the operation was succesfull
   * @throws JC_Exception
   */
  @Override
  public boolean moveNode( final String pNewName ) throws JC_Exception {
    setProperty( "title", pNewName );
    save();
    setName( pNewName );
    getJCPath().setChild( pNewName );
    return true;
  }

  /** removeChildNode
   *
   * @param pName
   * @throws JC_Exception
   */
  @Override
  public void removeChildNode( final String pName ) throws JC_Exception {
    if (hasNode( pName )) {
      getNode( pName ).removeNode();
    }
    return;
  }

  /** removeNode
   * @throws java.lang.Exception
   */
  @Override
  public void removeNode() throws JC_Exception {
    if (isNew()) {
      destroy();
    } else {
      setRemoved( true );
    }
    return;
  }

  /** removeNodeForced
   *
   * @throws JC_Exception
   */
  @Override
  public void removeNodeForced() throws JC_Exception {
    mNodeProps.add( PROPS.FORCED_REMOVE );
    removeNode();
    return;
  }


  
//  /** _setNodeType
//   * 
//
//   * @param pCategories
//   */
//  private void _setNodeType( List pCategories ) {
//    if (pCategories.isEmpty()==false) {
//      Category cat = (Category)pCategories.get(0);
//      mNodeType = cat.getTerm();
//    }
//    return;
//  }
//  
//  /** _getNodeTypeFromCats
//   * 
//   * @param pCategories
//   * @return
//   */
//  private String _getNodeTypeFromCats( List pCategories ) {
//    if (pCategories.isEmpty()==false) {
//      Category cat = (Category)pCategories.get(0);
//      return cat.getTerm();
//    }
//    return null;
//  }
  
  /** setNodeType
   *
   * @param pNodeType
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.UnsupportedEncodingException
   */
  @Override
  public JC_Nodeable setNodeType( final String pNodeType ) {
    mNodeType = pNodeType;
    return this;
  }


  /** initPropertyMembers
   * 
   * @throws JC_Exception
   */
  protected void initPropertyMembers() throws JC_Exception {
    return;
  }

    /* populateProperties
     * Set node properties originating from its feed
     * 
     * @throws org.jecars.client.JC_Exception
     */
    protected void populateProperties( JC_Params pParams ) throws JC_Exception {      
      if (mProperties==null) {
        getOrCreateProperties();
        final JC_Clientable client = getClient();
        if (pParams==null) {
          pParams = client.createParams( JC_RESTComm.GET );
        }
        JD_Taglist         tags = null;
        final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
        try {
          final JC_RESTComm comm = client.getRESTComm();

          JC_Utils.buildURL( client, url, pParams, null, null );
          final HttpURLConnection conn = comm.createHttpConnection( url.toString() );

          tags = comm.sendMessageGET( client, conn );
          if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {          
            final InputStream      is = JC_RESTComm.getResultStream( tags );
            JC_Factory.ATOM_RETRIEVER.populateNode( this, is );            
//            final WireFeedInput wfinput = new WireFeedInput();
//            wfinput.setXmlHealerOn( false );
//            final Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( is, JC_RESTComm.CHARENCODE ));
//            mID           = atomFeed.getId();
//            mLastModified = atomFeed.getUpdated().getTime();
//            final List lol = atomFeed.getOtherLinks();
//            final Iterator loli = lol.iterator();
//            while( loli.hasNext() ) {            
//              _addLinkInfo( this, (Link)loli.next() );
//            }
//            _addForeignProps( atomFeed );
//            _addProperty( JC_Defs.ATOM_TITLE, atomFeed.getTitle(), JC_PropertyType.TYPENAME_STRING );
//            final List catL = atomFeed.getCategories();
//            _setNodeType( catL );
          } else {
            // **** ERROR
            mNodeProps.add( PROPS.ERROR );
            final JC_HttpException e = JC_Utils.createCommException( tags, "while retrieving object ", url.toString() );
            if (e.getHttpErrorCode().getErrorCode()==HttpURLConnection.HTTP_NOT_FOUND) {
              setSynchronized( true );
            }
            throw e;
          }
          // **** Let the subclasses do their initialisation
          setSynchronized( true );
          initPropertyMembers();
        } catch( JC_HttpException he ) {
          throw he;
//        } catch( FeedException fe ) {
//          throw JC_Exception.createErrorException( fe.getMessage(), fe );
        } catch( ConnectException ce ) {
          final JC_HttpException he = JC_Utils.createCommException( tags, "Cannot connect to ", url.toString() );
          he.setErrorCode( JC_Exception.ERROR_CANNOTCONNECT );
          throw he;
//          throw JC_HttpException.createErrorHttpException( "Cannot connect to " + pUrl, ce );
        } catch( IOException ioe ) {
          throw JC_Exception.createErrorException( ioe.getMessage(), ioe );
        } catch( Exception e ) {
          throw JC_Exception.createErrorException( e.getMessage(), e );
        }
      }
      setSynchronized( true );
      return;
    }

    /** updateInternalStructure
     *
     */
    protected void updateInternalStructure() throws JC_Exception {
      return;
    }

    @Override
    public void save() throws JC_Exception {
      save( null );
      return;
    }

    /** save
     * @throws org.jecars.client.JC_Exception
     */
    @Override
    public void save(  final JC_Params pParams ) throws JC_Exception {

      if (!isDestroyed()) {
        try {
          updateInternalStructure();
          if(isNew()) {
            writeNew();
            setNew( false );
            setChanged( false );
          }

          if (isChanged()) {
            writeUpdates();
            setChanged(false);
          }

          if (isRemoved()) {
            writeRemoval( pParams );
          }

          //3. Write children nodes to JeCARS
          if(hasChildNodes()) {
            final Collection<JC_Nodeable> nodes = getChildNodes();
            final JC_Nodeable[] nodesArr = (JC_Nodeable[])nodes.toArray(TMPL_ARR);
            for( JC_Nodeable node : nodesArr ) {
              node.save();
            }
          }
        } catch (IOException e) {
          throw new JC_Exception(e);
        }
      }
      return;
    }
    
    /** writeNew
     * 
     * @throws org.jecars.client.JC_Exception
     * @throws java.io.IOException
     */
    private void writeNew() throws JC_Exception, IOException {
      
      boolean containsStream = false;
      final JC_Clientable client = getClient();
      final boolean postAsGet = client.getHttpOperation( JC_Clientable.POST_AS_GET );
      final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
      final JC_Params params = client.createParams( JC_RESTComm.POST );
      if (postAsGet) {
        params.setHTTPOverride( JC_RESTComm.POST );
      }        

      final JC_FeedXmlOutput output;
      if (postAsGet) {
        output = createFeedXml( params );
      } else {
        output = createFeedXml( null );
      }

      output.startNewEntry();
//      if (mCreateUsingCopy) {
      if (mNodeProps.contains(PROPS.CREATEUSINGCOPY)) {
        output.addLinkViaURL( client, getSourceObjectUrl());
      } else {
        String name;
        Object value;
        boolean catAdded = false;
        for( JC_Propertyable prop : mProperties ) {
          if (prop.getStream()!=null) {
            containsStream = true;
          }
          name = prop.getName();
          value = prop.getValue();
          if (value!=null) {
            if (value instanceof String) {
              // **** Is string value
              if(name.equals( JC_Defs.ATOM_CATEGORY )) {
                output.addCategory( (String)value );
                catAdded = true;
              } else {
                if (prop.isNew()) {
                  output.addProperty(name, (String)value );
                  prop.setNew( false );
                }
              }
            } else if (value instanceof Double) {
              if (prop.isNew()) {
                output.addProperty(name, ((Double)value).toString() );
                prop.setNew( false );
              }
            } else if (value instanceof Long) {
              if (prop.isNew()) {
                output.addProperty(name, ((Long)value).toString() );
                prop.setNew( false );
              }
            } else if (value instanceof Boolean) {
              if (prop.isNew()) {
                output.addProperty(name, ((Boolean)value).toString() );
                prop.setNew( false );
              }
            } else if (value instanceof JC_Nodeable) {
              // **** Is JC_Nodeable value
              if (prop.isNew()) {
                output.addProperty(name, (JC_Nodeable)value );
                prop.setNew( false );
              }              
            }
          }
        }
        if (!catAdded) {
          output.addCategory( getNodeType() );
        }
        
      }
      output.closeEntry();
      final String postBody = output.getXml();
      final long postBodyLength;
      if (postBody==null) {
        postBodyLength = 0L;
      } else {
        postBodyLength = postBody.length();
      }
//   gLog.info( "\noutput: " +postBody+ "\n" );

      // **** 2. Write feed to JeCARS
      ByteArrayInputStream bais = null;
      try {
        JC_Utils.buildURL( client, url, params, null, null );
        if (postBody!=null) {
          bais = new ByteArrayInputStream( postBody.getBytes() );
        }
        writeToJeCARS( client, url.toString(), bais, JC_RESTComm.POST, JC_RESTComm.CT_ATOM_XML, postBodyLength );
      } finally {
        setSynchronized( false );
        if (bais!=null) {
          bais.close();
        }
      }
      if (containsStream) {
        writeUpdates();
      }
      return;
    }
    
    /** writeUpdates
     * 
     * @throws org.jecars.client.JC_Exception
     * @throws java.io.IOException
     */
    private void writeUpdates() throws JC_Exception, IOException {

      final JC_Clientable client = getClient();
      final boolean putAsGet = client.getHttpOperation( JC_Clientable.PUT_AS_GET );
      StringBuilder url = JC_Utils.getFullNodeURL( client, this );
      JC_Params params = client.createParams( JC_RESTComm.PUT );
      JC_FeedXmlOutput output;
      if (putAsGet) {
        params = JC_Params.createParams();
        params.setHTTPOverride( JC_RESTComm.PUT );
      }              
        
      // **** First check for stream properties
      if (putAsGet) {
        output = createFeedXml( params );
      } else {
        output = createFeedXml( null );
      }
      output.startNewEntry();
      for (JC_Propertyable prop : mProperties) {
        if(prop.isChanged() || prop.isNew()) {
          if (prop.getStream()!=null) {
            final JC_Streamable stream = prop.getStream();
            // **** Is stream
            if (putAsGet) {
              // **** Stream as parameter
              params.addStreamable( stream );
              JC_Utils.buildURL( client, url, params, null, null );
              // **** 2. Write feed to JeCARS
              writeToJeCARS( client, url.toString(),
                        stream.getStream(), JC_RESTComm.PUT, stream.getContentType(), stream.getContentLength() );              
            } else {
              JC_Utils.buildURL( client, url, params, null, null );
              // **** 2. Write feed to JeCARS
              writeToJeCARS( client, url.toString(),
                        stream.getStream(), JC_RESTComm.PUT, stream.getContentType(), stream.getContentLength() );
            }
            prop.setChanged(false);
            prop.setNew(false);
          }
        } 
      }
      output.closeEntry();
      
      url = JC_Utils.getFullNodeURL( client, this );
      if (putAsGet) {
        params = JC_Params.createParams();
//        params = client.createParams( JC_RESTComm.PUT );
        params.setHTTPOverride( JC_RESTComm.PUT );
      }
            
      // **** Part two
      boolean update = false;
      if (putAsGet) {
        output = createFeedXml( params );
      } else {
        output = createFeedXml( null );
      }
      output.startNewEntry();
      for (JC_Propertyable prop : mProperties) {
        if(prop.isChanged() || prop.isNew()) {
          update = true;
          String name = prop.getName();
          if (prop.isMulti()) {

            // **** Is multi property
            String rv = "";
            JC_MultiValueProperty multi = (JC_MultiValueProperty)prop;
            if (multi.removeAllProperties()) {
              rv = "~";
              output.addProperty( name, rv );
            }
            rv = "-";
            List<String>values = multi.getValuesDeleted();
            boolean firstVal = true;
            for (String value : values) {
              if (firstVal) {
                firstVal = false;
                rv += value;
              } else {
                rv += ',' + value;
              }
            }
            if (!firstVal) {
              output.addProperty( name, rv );
            }
            rv = "";
            if (multi.isUnstructured()) {
              if ("Double".equals(multi.getDatatype())) {
                rv = JC_Params.UNSTRUCT_PREFIX_MDOUBLE;
              }
            }
            if (multi.removeAllProperties()) {
              rv += "*";
            } else {
              rv += "+";
            }
            firstVal = true;
            values = multi.getValuesNew();
            for (String value : values) {
              if (firstVal) {
                firstVal = false;
                rv += value;
              } else {
                rv += ',' + value;
              }
            }
            if (!firstVal) {
              output.addProperty( name, rv );
            }
            multi.clearDeletedValues();
            multi.clearNewValues();
            multi.setRemoveAllProperties( false );
          } else {
            // **** Single property
            if (prop.isRemoved()) {
              output.addProperty( name, JC_Params.PREFIX_VALUE_REMOVE );
            } else {
              Object value = prop.getValue();            
              if (value instanceof String) {
                if(name.equals( JC_Defs.ATOM_CATEGORY )) {                                          
                  output.addCategory((String)value);
                } else if (name.equals("author")) {
                  output.addAuthor((String)value);
                } else if (JC_Defs.ATOM_TITLE.equals( name )) {
                  output.addProperty( "jecars:" + name, (String)value );
                } else {         
                  output.addProperty(name, (String)value);
                }
              } else if (value instanceof Long) {
                output.addProperty( name, (Long)value );
              } else if (value instanceof Double) {
                output.addProperty( name, (Double)value );
              } else if (value instanceof Boolean) {
                output.addProperty( name, (Boolean)value );
              } else if (value instanceof JC_Nodeable) {
                output.addProperty( name, (JC_Nodeable)value );
              }
            }
          }
          prop.setChanged(false);
          prop.setNew(false);
        } 
      }
      if (update) {
        // **** There is something to update
        output.closeEntry();
        String putBody = output.getXml();
        long putBodyLength = 0L;
        if (putBody!=null) putBodyLength = putBody.length();

        // **** 2. Write feed to JeCARS
        ByteArrayInputStream bais = null;
        try {
          JC_Utils.buildURL( client, url, params, null, null );
          if (putBody!=null) bais = new ByteArrayInputStream( putBody.getBytes() );
          writeToJeCARS( client, url.toString(), bais, JC_RESTComm.PUT, JC_RESTComm.CT_ATOM_XML, putBodyLength ); 
        } finally {
          if (bais!=null) bais.close();
        }
      }
      return;
    }
    
    /** writeRemoval
     *
     * @throws JC_Exception
     * @throws IOException
     */
    private void writeRemoval( final JC_Params pParams ) throws JC_Exception, IOException {
      final JC_Clientable client = getClient();
      final StringBuilder url = JC_Utils.getFullNodeURL( client, this );
      final JC_Params params;
      if (pParams==null) {
        params = client.createParams( JC_RESTComm.DELETE );
      } else {
        params = pParams;
      }
      if (client.getHttpOperation( JC_Clientable.DELETE_AS_GET )) {
        params.setHTTPOverride( JC_RESTComm.DELETE );
      }
      params.setForce( mNodeProps.contains( PROPS.FORCED_REMOVE ));
      JC_Utils.buildURL( client, url, params, null, null );
      writeToJeCARS( client, url.toString(), null, JC_RESTComm.DELETE, null, -1 );
      destroy();
      return;
    }

    
    /** writeToJeCARS
     * @param url
     * @param body
     * @param operation
     * @throws org.jecars.client.JC_Exception
     */
    private void writeToJeCARS( final JC_Clientable pClient, final String pUrl, final InputStream pBody, final String pOperation, String pContentType, final long pLength ) throws JC_Exception {
      // 2. Write feed to JeCARS
//      ByteArrayInputStream bais = null;
//      int retCode = 0;

      final JC_RESTComm comm = pClient.getRESTComm();
      HttpURLConnection conn = null;
      try {
        conn = comm.createHttpConnection( pUrl );
                      
        if (pContentType==null) {
          pContentType = JC_RESTComm.CT_ATOM_XML;
        }
        
        if(JC_RESTComm.POST.equals( pOperation )) {
          // *********************
          // **** POST OPERATION
          final JD_Taglist tags = comm.sendMessagePOST( pClient, conn, pBody, pContentType, pLength );
          if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_CREATED) {
            final String location = (String)tags.getData( "Location" );
            if (location!=null) {
              final String serverPath = pClient.getServerPath();
              final int length = serverPath.length();
              final String newPath = location.substring(length);
              setPath(newPath);
              final String name = getJCPath().getBaseName();
              setName(name);
              // **** TODO
//              final URI  uri = new URI( serverPath );
//              final URI luri = new URI( location );
//              final int length = uri.getPath().length();
//              final String newPath = luri.getPath().substring(length);
//              setPath(newPath);
//              final String name = getJCPath().getBaseName();
//              setName(name);
              setProperty( JC_Defs.ATOM_TITLE, name);
            }
          } else {
            throw JC_Utils.createCommException( tags, "while creating object ", pUrl );
          }
        } else if (JC_RESTComm.PUT.equals( pOperation )) {
          // *********************
          // **** PUT OPERATION
          final JD_Taglist tags = comm.sendMessagePUT( pClient, conn, pBody, pContentType, pLength );
          if (JC_RESTComm.getResponseCode(tags)!=HttpURLConnection.HTTP_OK) {
            throw JC_Utils.createCommException( tags, "while updating object ", pUrl );
          }
        } else if (JC_RESTComm.DELETE.equals( pOperation )) {
          // *********************
          // **** DELETE OPERATION
          final JD_Taglist tags = comm.sendMessageDELETE( pClient, conn);
          if (JC_RESTComm.getResponseCode(tags)!=HttpURLConnection.HTTP_OK) {
            throw JC_Utils.createCommException( tags, "while deleting object ", pUrl );
          }
        } else {
          throw new IllegalArgumentException("Illegal HTTP operation: "+ pOperation);
        }      
      } catch (Exception e) {
        if (e instanceof JC_Exception) {
          throw (JC_Exception)e;
        }
        throw new JC_Exception( e );
      } finally {
        if (conn!=null) {
          conn.disconnect();
        }
      }
      return;
    }

    /** hasChildNodes
     *
     * @return
     */
    protected boolean hasChildNodes() {      
      if(mChildNodes!=null && !mChildNodes.isEmpty()) {
        return true;
      }
      return false;      
    }
    
    /** getModifiedDate
     * 
     * @return if null then the modified date is unknown
     */
    @Override
    public Calendar getModifiedDate() {
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis( mLastModified );
      return c;
    }

    /** setModifiedDate
     * 
     * @param pDate 
     */
    public void setModifiedDate( final long pDate ) {
      mLastModified = pDate;
      return;
    }
    
    /** setExpireDate
     * 
     * @param pPlusMinutes
     * @throws JC_Exception
     */
    @Override
    public void setExpireDate( final int pPlusMinutes ) throws JC_Exception {
      final Calendar cal = Calendar.getInstance();
      cal.add( Calendar.MINUTE, pPlusMinutes );
      setExpireDate( cal );
      return;
    }


    /** setExpireDate
     * @param pDate
     * @throws org.jecars.client.JC_Exception
     */
    @Override
    public void setExpireDate( Calendar pDate ) throws JC_Exception {
//      setProperty( JC_Defs.EXPIRE_DATE, ISO8601.format(pDate) );
      setProperty( JC_Defs.EXPIRE_DATE, pDate );
      return;
    }
    
    /** setTitle
     * 
     * @param pT
     * @return
     * @throws JC_Exception 
     */
    @Override
    public JC_Propertyable setTitle( final String pT ) throws JC_Exception {
      return setProperty( PROP_TITLE, pT );
    }

    @Override
    public String getTitle() {
      try {
        return getProperty( PROP_TITLE ).getValueString();
      } catch(JC_Exception e) {
        return "";
      }
    }

    @Override
    public String getBody() {
      try {
        return getProperty( PROP_BODY ).getValueString();
      } catch(JC_Exception e) {
        return "";
      }
    }
    
    @Override
    public Calendar getExpireDate() {
      throw new UnsupportedOperationException("Not supported yet.");      
    }

    @Override
    public Calendar getLastAccessedDate() {
      throw new UnsupportedOperationException("Not supported yet.");      
    }

    /** setSynchronized
     *
     * @param pSetSynchronized
     */
    private void setSynchronized( final boolean pSetSynchronized ) {
      if (pSetSynchronized) {
        mNodeProps.add( PROPS.SYNCHRONIZED );
        mNodeProps.remove( PROPS.DESTROYED );
      } else {
        mNodeProps.remove( PROPS.SYNCHRONIZED );
      }
//      mHasSynchronized = pSetSynchronized;
      return;
    }

    /** hasSynchronized
     *
     * @return
     */
//    private boolean hasSynchronized() {

    /** isSyncronized
     *
     * @return
     */
    @Override
    public boolean isSynchronized() {
      return mNodeProps.contains( PROPS.SYNCHRONIZED );
//      return mHasSynchronized;
    }

    /** refresh
     * @throws org.jecars.client.JC_Exception
     */
    @Override
    public void refresh() throws JC_Exception{
      destroy( true );
      mNodeProps.remove( PROPS.GOTNODES );
      return;
    }
    
    @Override
    public boolean gotChildNodes() {
      return mNodeProps.contains( PROPS.GOTNODES );
    }

    /** setNew
     *
     * @param pIsNew
     */
    @Override
    public void setNew( final boolean pIsNew ) {
      super.setNew( pIsNew );
      
      if(!pIsNew) {
        for (JC_Propertyable prop : mProperties) {
          prop.setNew(false);
        }
      }
    }
    
    private void createUsingCopy (boolean copy) {
      if (copy) {
        mNodeProps.add( PROPS.CREATEUSINGCOPY );
      } else {
        mNodeProps.remove( PROPS.CREATEUSINGCOPY );
      }
//      mCreateUsingCopy = copy;
      return;
    }
    
//    private boolean getCreateUsingCopy () {
//      return mCreateUsingCopy;
//    }
    
    private void setSourceObjectUrl (String url) {
      mSourceObjectUrl = url;
      return;
    }
    
    private String getSourceObjectUrl() {
      return mSourceObjectUrl;
    }
    
  /** getNodeType
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public String getNodeType() {
    return mNodeType;
  }
  
  /** getChildNodeDefs
   *  get the child node definition allowed for creation for this node
   * 
   * @return collection of nodetype string names
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Collection<String>getChildNodeDefs() throws JC_Exception {
    JC_Params params = getClient().createParams( JC_RESTComm.GET );
    params.setOutputFormat( JC_Defs.OUTPUTTYPE_PROPERTIES );
    JC_Query q = JC_Query.createQuery();
    q.setChildNodeDefs( true );
    Collection<JC_Nodeable>nodes = getNodes( params, null, q );    
    ArrayList<String>defs = new ArrayList<String>();
    for (JC_Nodeable node : nodes) {
      defs.add( node.getName() );
    }
    return defs;
  }

  /** getUpdateAsHead
   * 
   * get the update for this node as an HTTP HEAD call
   * 
   * @return true if the node has been updated, false if it is still the same
   * @throws org.jecars.client.JC_Exception if an error occurs
   */
  @Override
  public boolean getUpdateAsHead() throws JC_Exception {
    JC_Clientable client = getClient();
    JD_Taglist      tags = null;
    StringBuilder    url = JC_Utils.getFullNodeURL( client, this );
    boolean      updated = false;
    try {
      JC_RESTComm comm = client.getRESTComm();        
      JC_Params params = client.createParams( JC_RESTComm.GET ).cloneParams();
      params.setFilterEventTypes( "READ" );
      JC_Utils.buildURL( client, url, params, null, null );
      HttpURLConnection conn = comm.createHttpConnection( url.toString() );
      tags = comm.sendMessageHEAD( client, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
//        tags.print();
        Long modDate = (Long)tags.getData( "LastModified" );
        if (modDate!=null) {
          if (modDate>mLastModified) updated = true;
          mLastModified = modDate;
        } else {
          mLastModified = 0L;
        }
      } else {
        // **** ERROR
        JC_HttpException e = JC_Utils.createCommException( tags, "while retrieving head of object ", url.toString() );
        if (e.getHttpErrorCode().getErrorCode()==HttpURLConnection.HTTP_NOT_FOUND) {
          setSynchronized( true );
        }
        throw e;        
      }
    } catch (Exception e) {
      if (e instanceof JC_Exception) throw (JC_Exception)e;
        throw new JC_Exception( e );
    }
    return updated;
  }

  /** exportNode
   * 
   * @param pStream
   * @param pDeep
   */
  @Override
  public JC_Streamable exportNode( boolean pDeep ) throws JC_Exception {
    JC_Clientable client = getClient();
    JC_Params params = client.createParams( JC_RESTComm.GET ).cloneParams();
    params.setOutputFormat( JC_Defs.OUTPUTTYPE_BACKUP );
    return client.getNodeAsStream( getPath(), params, null, null );
  }

  /** importNode
   * 
   * @param pStream
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void importNode( JC_Streamable pStream ) throws JC_Exception{
    pStream.setContentType( JC_Defs.BACKUP_MIMETYPE );
    JC_Nodeable thisNode = getClient().getNode( getPath() );
    thisNode.removeNode();
    thisNode.save();
    JC_DefaultNode parentNode = (JC_DefaultNode)getParent();
    parentNode._setProperty( "jcr:data", pStream );    
    parentNode.save();
    return;
  }

  /** isDataFile
   *
   * @return
   */
  @Override
  public boolean isDataFile() {
    try {
      if ("jecars:datafile".equals(getNodeType())) {
        return true;
      }
      if ("jecars:datafolder".equals(getNodeType())) {
        return false;
      }
      return (getNodes().size()==0);
//      return hasProperty( "jcr:mimeType" );
    } catch( JC_Exception je ) {
      return false;
    }
  }

  /** isInErrorState
   *
   * @return
   */
  @Override
  public boolean isInErrorState() {
    return mNodeProps.contains( PROPS.ERROR );
  }

  /** isNull
   *
   * @return
   */
  @Override
  public boolean isNull() {
    return mNodeProps.contains( PROPS.NULL );
  }
  
  private void setDestroyed() {
    mNodeProps.add( PROPS.DESTROYED );
    mNodeProps.remove( PROPS.GOTNODES );
    mNodeProps.remove( PROPS.SYNCHRONIZED );
    return;
  }
  
  /** isDestroyed
   * 
   * @return 
   */
  @Override
  public boolean isDestroyed() {
    return mNodeProps.contains( PROPS.DESTROYED );
  }

  /** setNull
   *
   * @param pSet
   */
  protected void setNull( final boolean pSet ) {
    if (pSet) {
      mNodeProps.add( PROPS.NULL );
    } else {
      mNodeProps.remove( PROPS.NULL );
    }
    return;
  }

  // #########################################################################
  // #### Versioning
  // ####

  /** getVersionLabels
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public List<String> getVersionLabels() throws JC_Exception {
    final List<String> vl = new ArrayList<String>();
    final JC_Clientable client = getClient();
    final JC_Query query = JC_Query.createQuery();
    query.setVersionHistory( true );
    final JC_Streamable vhistory = client.getNodeAsStream( getPath(), null, null, query );
    try {
      final String vhis = JC_Utils.readAsString( vhistory.getStream() );
      final String[] vs = vhis.split( "\n" );
      for( String v : vs) {
        vl.add( v );
      }
    } catch( IOException ie ) {
      throw new JC_Exception(ie);
    }
    return vl;
  }

  /** commitVersion
   *
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void commitVersion( final String pLabel ) throws JC_Exception {
    try {
      JC_Clientable client = getClient();
      final boolean putAsGet = client.getHttpOperation( JC_Clientable.PUT_AS_GET );
      JC_Params params = client.createParams( JC_RESTComm.PUT ).cloneParams();
      if (putAsGet) {
        params.setHTTPOverride( JC_RESTComm.PUT );
      }
      JC_FeedXmlOutput output = createFeedXml( null );
      output.startNewEntry();
      output.closeEntry();

      params.setVCSCommand( "checkin" );
      params.setVCSLabel( pLabel );
      StringBuilder url = JC_Utils.getFullNodeURL( client, this );

      String putBody = output.getXml();
      long putBodyLength = 0L;
      if (putBody!=null) putBodyLength = putBody.length();

      // **** 2. Write feed to JeCARS
      ByteArrayInputStream bais = null;
      try {
        JC_Utils.buildURL( client, url, params, null, null );
        if (putBody!=null) bais = new ByteArrayInputStream( putBody.getBytes() );
        writeToJeCARS( client, url.toString(), bais, JC_RESTComm.PUT, JC_RESTComm.CT_ATOM_XML, putBodyLength );
      } finally {
        if (bais!=null) bais.close();
      }

    } catch(IOException ie) {
      throw new JC_Exception(ie);
    }
    return;
  }

  /** checkout
   *
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void checkout() throws JC_Exception {
    try {
      JC_Clientable client = getClient();
      final boolean putAsGet = client.getHttpOperation( JC_Clientable.PUT_AS_GET );
      JC_Params params = client.createParams( JC_RESTComm.PUT ).cloneParams();
      if (putAsGet) {
        params.setHTTPOverride( JC_RESTComm.PUT );
      }
      JC_FeedXmlOutput output = createFeedXml( null );
      output.startNewEntry();
      output.closeEntry();

      params.setVCSCommand( "checkout" );
      StringBuilder url = JC_Utils.getFullNodeURL( client, this );

      String putBody = output.getXml();
      long putBodyLength = 0L;
      if (putBody!=null) putBodyLength = putBody.length();

      // **** 2. Write feed to JeCARS
      ByteArrayInputStream bais = null;
      try {
        JC_Utils.buildURL( client, url, params, null, null );
        if (putBody!=null) bais = new ByteArrayInputStream( putBody.getBytes() );
        writeToJeCARS( client, url.toString(), bais, JC_RESTComm.PUT, JC_RESTComm.CT_ATOM_XML, putBodyLength );
      } finally {
        if (bais!=null) bais.close();
      }

    } catch(IOException ie) {
      throw new JC_Exception(ie);
    }
  }


  /** restoreVersion
   *
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void restoreVersion( final String pLabel ) throws JC_Exception {
    try {
      JC_Clientable client = getClient();
      final boolean putAsGet = client.getHttpOperation( JC_Clientable.PUT_AS_GET );
      JC_Params params = client.createParams( JC_RESTComm.PUT ).cloneParams();
      if (putAsGet) {
        params.setHTTPOverride( JC_RESTComm.PUT );
      }
      JC_FeedXmlOutput output = createFeedXml( null );
      output.startNewEntry();
      output.closeEntry();

      params.setVCSCommand( "restore" );
      params.setVCSLabel( pLabel );
      StringBuilder url = JC_Utils.getFullNodeURL( client, this );

      String putBody = output.getXml();
      long putBodyLength = 0L;
      if (putBody!=null) putBodyLength = putBody.length();

      // **** 2. Write feed to JeCARS
      ByteArrayInputStream bais = null;
      try {
        JC_Utils.buildURL( client, url, params, null, null );
        if (putBody!=null) bais = new ByteArrayInputStream( putBody.getBytes() );
        writeToJeCARS( client, url.toString(), bais, JC_RESTComm.PUT, JC_RESTComm.CT_ATOM_XML, putBodyLength );
      } finally {
        if (bais!=null) bais.close();
      }

    } catch(IOException ie) {
      throw new JC_Exception(ie);
    }
    return;
  }


  /** removeVersionByLabel
   *
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void removeVersionByLabel( final String pLabel ) throws JC_Exception {
    try {
      JC_Clientable client = getClient();
      final boolean putAsGet = client.getHttpOperation( JC_Clientable.PUT_AS_GET );
      JC_Params params = client.createParams( JC_RESTComm.PUT ).cloneParams();
      if (putAsGet) {
        params.setHTTPOverride( JC_RESTComm.PUT );
      }
      JC_FeedXmlOutput output = createFeedXml( null );
      output.startNewEntry();
      output.closeEntry();

      params.setVCSCommand( "removeByLabel" );
      params.setVCSLabel( pLabel );
      StringBuilder url = JC_Utils.getFullNodeURL( client, this );

      String putBody = output.getXml();
      long putBodyLength = 0L;
      if (putBody!=null) putBodyLength = putBody.length();

      // **** 2. Write feed to JeCARS
      ByteArrayInputStream bais = null;
      try {
        JC_Utils.buildURL( client, url, params, null, null );
        if (putBody!=null) bais = new ByteArrayInputStream( putBody.getBytes() );
        writeToJeCARS( client, url.toString(), bais, JC_RESTComm.PUT, JC_RESTComm.CT_ATOM_XML, putBodyLength );
      } finally {
        if (bais!=null) bais.close();
      }

    } catch(IOException ie) {
      throw new JC_Exception(ie);
    }
    return;
  }

  @Override
  public String toString() {
    String s = mPath.getPath();
    if (mNodeProps.contains(PROPS.DESTROYED)) {
      s += " (DESTROYED)";
    }
    return s;
  }
  
  



  // #########################################################################
  // #### JC_WebDAVable
  // ####


  /** Dav_enable
   *
   */
  @Override
  public void Dav_enable() throws JC_Exception {
    addMixin( "jecars:Dav_deftypes" );
    return;
  }

  /** Dav_disable
   *
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void Dav_disable() throws JC_Exception {
    // TODO
    throw new JC_Exception( new UnsupportedOperationException( "Dav_disable" ) );
  }



  /** Dav_setDefaultFileType
   *
   * @param pFileType
   */
  @Override
  public void   Dav_setDefaultFileType( final String pFileType ) throws JC_Exception {
    setProperty( "jecars:Dav_DefaultFileType", pFileType );
    return;
  }

  /** Dav_getDefaultFileType
   *
   * @return
   */
  @Override
  public String Dav_getDefaultFileType() {
    try {
      return getProperty( "jecars:Dav_DefaultFileType" ).getValueString();
    } catch (JC_Exception ex) {
      return null;
    }
  }

  /** Dav_setDefaultFolderType
   *
   * @param pFileType
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void   Dav_setDefaultFolderType( String pFileType ) throws JC_Exception {
    setProperty( "jecars:Dav_DefaultFolderType", pFileType );
    return;
  }

  /** Dav_getDefaultFolderType
   *
   * @return
   */
  @Override
  public String Dav_getDefaultFolderType() {
    try {
      return getProperty( "jecars:Dav_DefaultFolderType" ).getValueString();
    } catch (JC_Exception ex) {
      return null;
    }
  }



  
}
