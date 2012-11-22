/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client.atom;

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import nl.msd.jdots.JD_Taglist;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jecars.client.IJC_Retriever;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_DefaultProperty;
import org.jecars.client.JC_Defs;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_MultiValueProperty;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Params;
import org.jecars.client.JC_Path;
import org.jecars.client.JC_PropertyType;
import org.jecars.client.JC_Propertyable;
import org.jecars.client.JC_RESTComm;
import org.jecars.client.JC_Rights;
import org.jecars.client.JC_Utils;

/**
 *
 * @author weert
 */
public class JC_AtomRetriever implements IJC_Retriever {
   
  @Override
  public void populateNode( final JC_DefaultNode pDNode, final InputStream pIS ) throws JC_Exception {
    try {
      final WireFeedInput wfinput = new WireFeedInput();
      wfinput.setXmlHealerOn( false );
      
//  try {
//  String xmlll = JC_Utils.readAsString(pIS);
//      System.out.println(xmlll);
//  } catch( IOException e ) {
//      e.printStackTrace();
//  }
  
      
      final Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( pIS, JC_RESTComm.CHARENCODE ));
      pDNode.setID( atomFeed.getId() );
      pDNode.setModifiedDate( atomFeed.getUpdated().getTime() );
      pDNode.setName( atomFeed.getTitle() );
      final List lol = atomFeed.getOtherLinks();
      final Iterator loli = lol.iterator();
      while( loli.hasNext() ) {            
        _addLinkInfo( pDNode, (Link)loli.next() );
      }
      _addForeignProps( pDNode, atomFeed, null );
      pDNode._addProperty( JC_Defs.ATOM_TITLE, atomFeed.getTitle(), JC_PropertyType.TYPENAME_STRING );
      final List catL = atomFeed.getCategories();
      _setNodeType( pDNode, catL );
    } catch( DataConversionException de ) {
      throw new JC_Exception(de);
    } catch( UnsupportedEncodingException ie ) {
      throw new JC_Exception(ie);
    } catch( FeedException fe ) {
      throw new JC_Exception(fe);
    }
    return;
  }


  /** getRights
   * 
   * @param pDNode
   * @param pPrincipal
   * @return
   * @throws JC_Exception 
   */
  @Override
  public JC_Rights getRights( final JC_DefaultNode pDNode, final InputStream pIS ) throws JC_Exception {
    final     JC_Rights rights = new JC_Rights();
    final WireFeedInput wfinput = new WireFeedInput();
    wfinput.setXmlHealerOn( false );
    try {
      final Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( pIS, JC_RESTComm.CHARENCODE ));
      final List fmc = (List)atomFeed.getForeignMarkup();
      final Iterator fmi = fmc.iterator();
      while (fmi.hasNext()) {
        final Element fmcEntry = (Element)fmi.next();
        final String name = fmcEntry.getNamespacePrefix() + ":" + JC_Utils.urlencode( fmcEntry.getName() );
        if ("jecars:Actions".equals( name )) {
          rights.addRight( fmcEntry.getValue() );
        }
      }
    } catch( FeedException fe ) {
      throw new JC_Exception( fe );
    } catch( IllegalArgumentException ie ) {
      throw new JC_Exception( ie );
    } catch( UnsupportedEncodingException ue ) {
      throw new JC_Exception( ue );
    }
    return rights;
  }    
    
  /** parseChildNodes
   * 
   * @param pDNode
   * @param pIS
   * @param pClient
   * @param pNodes 
   */
  @Override
  public void parseChildNodes( final JC_DefaultNode pDNode, final InputStream pIS, final JC_Clientable pClient, final Collection<JC_Nodeable>pNodes ) throws JC_Exception {
    try {
      _parseChildNodes_Atom( pDNode, pIS, pClient, pNodes );
    } catch( FeedException fe ) {
      throw JC_Exception.createErrorException( fe.getMessage(), fe );
    } catch( DataConversionException e) {
      throw new JC_Exception(e);
    } catch( UnsupportedEncodingException e) {
      throw new JC_Exception(e);
    }
    return;
  }
   
    
    
  /** _addMultiProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.UnsupportedEncodingException
   */
  private JC_Propertyable _addMultiProperty( final JC_DefaultNode pDNode, final String pName, final String pValue, final boolean pNullAllowed ) throws JC_Exception, UnsupportedEncodingException {
    JC_DefaultProperty prop = (JC_DefaultProperty)pDNode.checkProperty( pName );
    if (prop==null) {
      prop = new JC_MultiValueProperty( pDNode );
      prop.setName( pName );
      if (pNullAllowed || (pValue!=null)) {
        prop.addValue( pValue, false );
      }
      Collection<JC_Propertyable> props = pDNode.getOrCreateProperties();
      props.add( prop );
    } else {
      if (pNullAllowed || (pValue!=null)) {
        prop.addValue( pValue, false );
      }
    }
    return prop;
  }

  /** addForeignProps
   * 
   * @param pAtomFeed
   * @throws java.lang.Exception
   */
  private void _addForeignProps( final JC_DefaultNode pDNode, Feed pAtomFeed, Entry pEntry ) throws JC_Exception, UnsupportedEncodingException, DataConversionException {
   
    final List<Element> fmc;
    if (pAtomFeed==null) {
      fmc = (List<Element>)pEntry.getForeignMarkup();
    } else {
      fmc = (List<Element>)pAtomFeed.getForeignMarkup();
    }
    for( final Element fmcEntry : fmc ) {
//    Iterator fmi = fmc.iterator();
//    while (fmi.hasNext()) {
//      Element fmcEntry = (Element) fmi.next();
      String name = fmcEntry.getNamespacePrefix() + ':' + JC_Utils.urlencode( fmcEntry.getName() );
//   System.out.println( "a=--0d -0ko " + name );
      final Attribute typeAttr = fmcEntry.getAttribute( "type" );
      Attribute multiAttr = fmcEntry.getAttribute( "multi" );
      if (multiAttr!=null) {
        if (!multiAttr.getBooleanValue()) multiAttr = null;
      }
      if (multiAttr!=null) {
        // **** Multi value property
        final List<Element> chs = (List<Element>)fmcEntry.getChildren();
        if (chs.isEmpty()) {
          _addMultiProperty( pDNode, name, null, false );
        } else {
          for (Element citEntry : chs) {
            name = citEntry.getNamespacePrefix() + ':' + JC_Utils.urlencode( citEntry.getName() );
            _addMultiProperty( pDNode, name, citEntry.getValue(), true );
          }
        }
/*
        Iterator cit = chs.iterator();
        //ArrayList<StringValue> vals = new ArrayList<StringValue>();
        while (cit.hasNext()) {
          Element citEntry = (Element) cit.next();
          name = citEntry.getNamespacePrefix() + ":" + JC_Utils.urlencode( citEntry.getName() );              
//       System.out.println( "MVP  " + name + " = " +JC_Utils.urldecode( citEntry.getValue() )  );
//          _addMultiProperty( name, JC_Utils.urldecode( citEntry.getValue() ) );
          _addMultiProperty( name, citEntry.getValue() );
        }
 */
      } else {
        if (typeAttr!=null) {
          pDNode._addProperty( name, fmcEntry.getValue(), typeAttr.getValue() );          
        } else {
          pDNode._addProperty( name, fmcEntry.getValue(), JC_PropertyType.TYPENAME_UNDEFINED );
        }
//        _addProperty( name, JC_Utils.urldecode(fmcEntry.getValue()) );
      }
    }
    return;
  }
  
    
    
 /** _parseChildNodes_Atom
   * 
   * @param pStream
   * @param pClient
   * @param pNodes
   * @return
   * @throws com.sun.syndication.io.FeedException
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.UnsupportedEncodingException
   */
  private Collection<JC_Nodeable> _parseChildNodes_Atom( final JC_DefaultNode pDNode, InputStream pStream, JC_Clientable pClient, Collection<JC_Nodeable>pNodes ) throws FeedException, JC_Exception, UnsupportedEncodingException, DataConversionException {
    WireFeedInput wfinput = new WireFeedInput();
    wfinput.setXmlHealerOn( false );
    Feed atomFeed = (Feed)wfinput.build( new InputStreamReader( pStream, JC_RESTComm.CHARENCODE ) );
    _addChildNodes( pDNode, atomFeed, pClient, pNodes );
    return pNodes;
  }

  /** _setNodeType
   * 
   * @param pCategories
   */
  private void _setNodeType( final JC_DefaultNode pDNode, final List pCategories ) {
    if (!pCategories.isEmpty()) {
      Category cat = (Category)pCategories.get(0);
      pDNode.setNodeType( cat.getTerm() );
    }
    return;
  }
  
  /** _getNodeTypeFromCats
   * 
   * @param pCategories
   * @return
   */
  private String _getNodeTypeFromCats( final List pCategories ) {
    if (pCategories.isEmpty()) {
      return null;
    } else {
      Category cat = (Category)pCategories.get(0);
      return cat.getTerm();
    }
  }
  
 /** _addLinkInfo
   * 
   * @param pNode
   * @param pLink
   */
  private void _addLinkInfo( final JC_DefaultNode pNode, final Link pLink ) {
    if (pLink.getRel().equals( "self" )) {
      JC_Path selfPath = new JC_Path( pLink.getHref() );
      selfPath.ensureDecode();
      pNode.setSelfLink( selfPath.toString() );
    }
    return;
  }

    
  /** addChildNodes
   * 
   * @param pAtomFeed
   * @param pClient
   * @param pChildNodes
   * @throws org.jecars.client.JC_Exception
   */
  private void _addChildNodes( final JC_DefaultNode pDNode, Feed pAtomFeed, JC_Clientable pClient, Collection<JC_Nodeable>pChildNodes ) throws JC_Exception, UnsupportedEncodingException, DataConversionException {

    final String selfurl = JC_Utils.getFullNodeURL( pClient, pDNode ).toString();
 
    String nt;
    Iterator entries = pAtomFeed.getEntries().iterator();
    while (entries.hasNext()) {
      Entry entry = (Entry)entries.next();
      nt = _getNodeTypeFromCats( entry.getCategories() );
      JC_DefaultNode nodeable = (JC_DefaultNode)pClient.createNodeClass( nt );
      nodeable.setNodeType( nt );
//      nodeable._setNodeType( entry.getCategories() );
      
      List<Link> l = entry.getOtherLinks();
      for (Link olink : l) {
        if (olink.getRel().equals( "self" )) {
          _addLinkInfo( nodeable, olink );
          if (nodeable.getSelfLink().startsWith( selfurl )) {
            String sl = nodeable.getSelfLink().substring( selfurl.length()+1 );
            if (sl.indexOf('/')==-1) {
              // **** Is a real child
              nodeable.setParent( pDNode );
              JC_Path pathBuffer = new JC_Path( pDNode.getPath() );
              pathBuffer.addChildPath( entry.getTitle() );
              nodeable.setParent( pDNode );
              nodeable.setName( entry.getTitle() );
              nodeable.setID(   entry.getId() );
              nodeable.setJCPath( pathBuffer );
              _addForeignProps( nodeable, null, entry );
            } else {
              // **** This node is not a child... probably result of a query
              nodeable.setParent( pDNode );
              nodeable.setName( entry.getTitle() );
              nodeable.setID(   entry.getId() );
              nodeable.setPath( nodeable.getSelfLink().substring( pClient.getServerPath().length() ) );
              List<Content> conlist = entry.getContents();
              for (Content cl : conlist) {
                nodeable._addProperty( "jecars:Body", cl.getValue(), JC_PropertyType.TYPENAME_STRING );
              }
              _addForeignProps( nodeable, null, entry );
            }
            break;
          }
        }
      }

      // **** Set Node properties
//      nodeable.populateProperties(entry);
      nodeable.setModifiedDate( entry.getUpdated().getTime() );
      
      pChildNodes.add(nodeable);
    }
    return;
  }

    
}
