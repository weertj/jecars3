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
package org.jecars.output;

import com.google.gdata.util.common.base.StringUtil;
import com.google.gdata.util.common.html.HtmlToText;
import java.security.AccessControlException;
import java.util.Iterator;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_HTML
 *
 * @version $Id: CARS_OutputGenerator_Atom.java,v 1.15 2009/06/21 20:59:41 weertj Exp $
 */
public class CARS_OutputGenerator_Atom extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {

  static private String gFeedHeader = null;
    
  /** Creates a new instance of CARS_OutputGenerator_HTML */
  public CARS_OutputGenerator_Atom() {
  }

  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {

    try {
      if (gFeedHeader==null) {
        gFeedHeader = "<?xml version=\"1.0\" encoding='utf-8'?>\n" +
                      "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:openSearch=\"http://a9.com/-/spec/opensearchrss/1.0/\"\n";
//        Iterator it = CARS_ActionContext.gIncludeNS.iterator();
        Session appSession = CARS_Factory.getSystemApplicationSession();
        synchronized(appSession) {
          String nms[] = appSession.getNamespacePrefixes();
          for( int i=0; i<nms.length; i++ ) {
            if (CARS_ActionContext.gIncludeNS.contains( nms[i] )) {
              gFeedHeader += " xmlns:" + nms[i] + "=\"" + appSession.getNamespaceURI( nms[i] ) + "\"";
            }
          }
        }
        gFeedHeader += ">\n";
      }
    } catch (Exception e) {
      pMessage.append( e.getMessage() );
    }
    pMessage.append( gFeedHeader );
    pContext.setContentType( "application/atom+xml" );
    return;
  }
  
 /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "</feed>\n" );
    return;
  }

  /** Add node entry, the current (this) node
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pThisNode the this node
   * @param pFromNode from node number
   * @param pToNode to node number
   */   
  @Override
  public void addThisNodeEntry( final CARS_ActionContext pContext, final CARS_Buffer pMessage, final Node pThisNode,
                                final long pFromNode, final long pToNode ) throws RepositoryException, Exception {
    
    pMessage.append( "<title>" ).append( CARS_Utils.convertNodeName(pThisNode) ).append( "</title>\n" );
    if (pThisNode.hasProperty( CARS_ActionContext.gDefTitle )) {
//      pMessage.append( "<subtitle>" ).append( CARS_ActionContext.transportString(pThisNode.getProperty( CARS_ActionContext.gDefTitle ).getString())).append( "</subtitle>\n" );
      pMessage.append( "<subtitle>" ).append(
              StringUtil.xmlContentEscape( pThisNode.getProperty( CARS_ActionContext.gDefTitle ).getString() )).append(
              "</subtitle>\n" );
    }
    pMessage.append( "<author><name>" + CARS_Definitions.DEFAULTAUTHOR + "</name></author>\n");
    pMessage.append( "<rights>" + CARS_Definitions.DEFAULTRIGHTS + "</rights>\n");
    addThisNode_addlinks( pContext, pMessage, pThisNode, pFromNode, pToNode );

    pMessage.append( "<published>" ).append( pContext.getPublishedDate(pThisNode) ).append( "</published>\n" );
    pMessage.append( "<updated>" ).append( pContext.getUpdatedDate(pThisNode) ).append( "</updated>\n" );
    pMessage.append( "<id>" ).append( pContext.getAtomID(pThisNode) ).append( "</id>\n" );
    pMessage.append( "<category term=\"" ).append( pThisNode.getPrimaryNodeType().getName() ).append( "\"/>\n" );
    if (pThisNode.hasProperty( CARS_ActionContext.gDefBody )) {
      pMessage.append( "<content type=\"html\"><![CDATA[<p>" );
      pMessage.append( pThisNode.getProperty( CARS_ActionContext.gDefBody ).getString() );
      pMessage.append( "</p>]]></content>\n" );
    }
    addNodeEntry_opensearch( pMessage, pThisNode, pFromNode, pToNode );
    // **** Add the properties as result
    final PropertyIterator pi = pThisNode.getProperties();
    addMultiPropertyEntry( pContext, pMessage, pThisNode, pi );
    
    return;
  }

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   */   
  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {
      
    pMessage.append( "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:jecars=\"http://jecars.org/\">\n" );
    pMessage.append( "<title type=\"text\">" ).append( CARS_Utils.convertNodeName(pChildNode) ).append( "</title>\n" );
    pMessage.append( "<author><name>" + CARS_Definitions.DEFAULTAUTHOR + "</name></author>\n");
    pMessage.append( "<rights>" + CARS_Definitions.DEFAULTRIGHTS + "</rights>\n");
    addNodeEntry_addlinks( pContext, pMessage, pChildNode );
    
    pMessage.append( "<published>" ).append( pContext.getPublishedDate(pChildNode) ).append( "</published>\n" );
    pMessage.append( "<updated>" ).append( pContext.getUpdatedDate(pChildNode) ).append( "</updated>\n" );
    pMessage.append( "<id>" ).append( pContext.getAtomID(pChildNode) ).append( "</id>\n" );
    pMessage.append( "<category term=\"" ).append( pChildNode.getPrimaryNodeType().getName() ).append( "\"/>\n" );
    if (pChildNode.hasProperty( CARS_ActionContext.gDefBody )==true) {
      pMessage.append( "<content type=\"html\"><![CDATA[" );
      pMessage.append( pChildNode.getProperty( CARS_ActionContext.gDefBody ).getString() );
      pMessage.append( "]]></content>\n" );
    }
    
    JD_Taglist tags = pContext.getQueryPartsAsTaglist();
    String gap = (String)tags.getData( "jecars:getAllProperties" );
    if ((gap!=null) && (gap.equals( "true" ))) {
      // **** Add the properties as result
      final PropertyIterator pi = pChildNode.getProperties();
      addMultiPropertyEntry( pContext, pMessage, pChildNode, pi );
    }    
    
    pMessage.append( "</entry>\n" );
    return;
  }
  

  
  /** Add a multi property entry
   * 
   * @param pContext
   * @param pReply
   * @param pThisNode
   * @param pIt
   * @throws java.lang.Exception
   */
  private void addMultiPropertyEntry( CARS_ActionContext pContext, final CARS_Buffer pReply, Node pThisNode, PropertyIterator pIt  ) throws Exception {
    JD_Taglist params = pContext.getQueryPartsAsTaglist();
    boolean includeBinary = false;
    if (params.getData( CARS_ActionContext.gIncludeBinary )!=null) {
      String bv = (String)params.getData( CARS_ActionContext.gIncludeBinary );
      if (bv.equalsIgnoreCase( "true" )==true) includeBinary = true;
    }

    // **** Add the properties as result
    Property p;
    Object po;
    while( pIt.hasNext() ) {
      po = pIt.next();
      if (po instanceof Property) {
        p = (Property)po;
        if (p.getName().indexOf(':')!=-1) {
          // **** Property with namespace
          final String prefix = p.getName().substring( 0, p.getName().indexOf(':'));
          if (CARS_ActionContext.gIncludeNS.contains( prefix )) {
             if ((p.getType()!=PropertyType.BINARY) || (includeBinary==true)) {
               if (p.getDefinition().isMultiple()) {
                 pReply.append( "<" ) .append( p.getName() ).append( " multi=\"true\">\n" );
                 try {
                   Value[] vals = p.getValues();
                   for( int val=0; val<vals.length; val++ ) {
                     addPropertyValue( pContext, pReply, p.getName(), pThisNode, vals[val] );
                   }
                 } finally {
                   pReply.append( "</" ).append( p.getName() ).append( ">\n" );
                 }
               } else {
                 addPropertyValue( pContext, pReply, p.getName(), pThisNode, p.getValue() );
               }
             }
           }          
        } else {
          // **** Property without namespace
          if ((p.getType()!=PropertyType.BINARY) || (includeBinary)) {
            if (p.getDefinition().isMultiple()) {
              pReply.append( "<" ) .append( p.getName() ).append( " multi=\"true\">\n" );
              try {
                Value[] vals = p.getValues();
                for( int val=0; val<vals.length; val++ ) {
                  addPropertyValue( pContext, pReply, p.getName(), pThisNode, vals[val] );
                }
              } finally {
                pReply.append( "</" ).append( p.getName() ).append( ">\n" );
              }
            } else {
              addPropertyValue( pContext, pReply, p.getName(), pThisNode, p.getValue() );
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

  /** _addPropertyVString
   * 
   * @param pSB
   * @param pProp
   * @param pValue
   */
  private void _addPropertyVString( final CARS_Buffer pSB, final String pProp, final Value pValue ) throws ValueFormatException, IllegalStateException, RepositoryException {
    pSB.append( '<' );
    pSB.append( pProp );
    pSB.append( " type='" + PropertyType.nameFromValue(pValue.getType()) + "'>" );
//    pSB.append( pValue );
    pSB.append( StringUtil.xmlContentEscape(pValue.getString()) );
    pSB.append( "</" );
    pSB.append( pProp );
    pSB.append( ">\n" );    
    return;
  }

  /** _addPropertyBinString
   * 
   * @param pSB
   * @param pProp
   * @param pValue
   * @throws javax.jcr.ValueFormatException
   * @throws java.lang.Exception
   */
  private void _addPropertyBinString( final CARS_Buffer pSB, final String pProp, final Value pValue ) throws ValueFormatException, Exception {
    pSB.append( '<' );
    pSB.append( pProp );
    pSB.append( " type='" + PropertyType.nameFromValue(pValue.getType()) + "'>" );
    pSB.append( CARS_ActionContext.transportString( pValue.getString() ) );
    pSB.append( "</" );
    pSB.append( pProp );
    pSB.append( ">\n" );
    return;
  }
  
  /** addPropertyValue
   * @param pContext
   * @param pBuilder
   * @param pPropName
   * @param pNode
   * @param pValue
   * @throws java.lang.Exception
   */
  private void addPropertyValue( CARS_ActionContext pContext, final CARS_Buffer pBuilder, String pPropName, Node pNode, Value pValue ) throws Exception {
    switch( pValue.getType() ) {
      case PropertyType.BINARY: {
        _addPropertyBinString( pBuilder, pPropName, pValue );
//        pBuilder.append( "<" + pPropName + ">" + CARS_ActionContext.transportString( pValue.getString() ) + "</" + pPropName + ">\n"  );
        break;
      }
      case PropertyType.BOOLEAN:
      case PropertyType.DOUBLE:
      case PropertyType.DATE:
      case PropertyType.STRING:
      case PropertyType.LONG: {
        _addPropertyVString( pBuilder, pPropName, pValue );
//        pBuilder.append( "<" + pPropName + ">" + pValue.getBoolean() + "</" + pPropName + ">\n" );
        break;
      }
//      case PropertyType.DATE: {
//        _addPropertyVString( pBuilder, pPropName, pValue.getString() );
////        pBuilder.append( "<" + pPropName + ">" + pValue.getString() + "</" + pPropName + ">\n" );
//        break;
//      }
//      case PropertyType.DOUBLE: {
//        _addPropertyVString( pBuilder, pPropName, pValue.getString() );
////        pBuilder.append( "<" + pPropName + ">" + pValue.getDouble() + "</" + pPropName + ">\n" );
//        break;
//      }
//      case PropertyType.LONG: {
//        _addPropertyVString( pBuilder, pPropName, pValue.getString() );
////        pBuilder.append( "<" + pPropName + ">" + pValue.getLong() + "</" + pPropName + ">\n" );
//        break;
//      }
      case PropertyType.NAME:
      case PropertyType.PATH: {
        _addPropertyVString( pBuilder, pPropName, pValue );
//        _addPropertyVString( pBuilder, pPropName, CARS_ActionContext.transportString( pValue.getString() ) );
//        pBuilder.append( "<" + pPropName + ">" + CARS_ActionContext.transportString( pValue.getString() ) + "</" + pPropName + ">\n"  );
        break;
      }
//      case PropertyType.PATH: {
//        pBuilder.append( "<" + pPropName + ">" + CARS_ActionContext.transportString( pValue.getString() ) + "</" + pPropName + ">\n"  );
//        break;
//      }
      case PropertyType.REFERENCE: {
        try {
          pBuilder.append( "<" + pPropName + " uuid=\"" + CARS_ActionContext.transportString( pValue.getString() ) + "\">" +
//                                 pContext.getBaseContextURL() + pNode.getSession().getNodeByUUID(pValue.getString()).getPath() +
                                 pNode.getSession().getNodeByUUID(pValue.getString()).getPath() +
                               "</" + pPropName + ">\n"  );
        } catch (ItemNotFoundException infe) {
          // **** No right for item. skip it
        }
        break;
      }
//      case PropertyType.STRING: {
//        pBuilder.append( "<" + pPropName + ">" + pValue.getString() + "</" + pPropName + ">\n" );
//        break;
//      }
    }
    return;
  }

  
  /** Opensearch items
   */
  private void addNodeEntry_opensearch( final CARS_Buffer pMessage, Node pNode, long pFromNode, long pToNode ) {
    if (pFromNode>0) {
      long totresults = (pToNode-pFromNode)+1;
      pMessage.append( "<openSearch:totalResults>" ).append( totresults ).append( "</openSearch:totalResults>\n" );
      pMessage.append( "<openSearch:startIndex>" ).append( pFromNode ).append( "</openSearch:startIndex>\n" );
    }
    return;
  }

  /** Add node links
   */
  private void addNodeEntry_addlinks( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pNode ) throws RepositoryException {  
    String path;
    path = pContext.getBaseContextURL() + CARS_Utils.getEncodedPath( pNode );
    pMessage.append( "<link rel=\"alternate\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
    pMessage.append( "<link rel=\"self\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
    try {
//      pNode.getSession().checkPermission( pNode.getPath(), "set_property" ); // TODO
      pMessage.append( "<link rel=\"edit\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
    } catch (AccessControlException ace) {              
    }
    return;
  }
  
  /** Add node links
   */
  private void addThisNode_addlinks( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pNode,
                                      long pFromNode, long pToNode ) throws RepositoryException {
    String path;
    path = pContext.getBaseContextURL();
    if (pNode!=null) path += CARS_Utils.getEncodedPath( pNode );
    if (pFromNode>1) {
      pMessage.append( "<link rel=\"previous\" type=\"application/atom+xml\" href=\"" ).append( path );
      long si = pFromNode-(pToNode-pFromNode);
      if (si<1) si = 1;
      pMessage.append( "?max-results=" ).append(pToNode-pFromNode).append( "%26start-index=" ).append( si ); // TODO
      pMessage.append( "\"/>\n" );
      pMessage.append( "<link rel=\"next\" type=\"application/atom+xml\" href=\"" + path );
      pMessage.append( "?max-results=" ).append(pToNode-pFromNode).append( "%26start-index=" ).append(pToNode+1); // TODO
      pMessage.append( "\"/>\n" );
    }
    pMessage.append( "<link rel=\"self\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
    pMessage.append( "<link rel=\"alternate\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
    try {
      if (pNode!=null) {
//        pNode.getSession().checkPermission( pNode.getPath(), "set_property" ); // TODO
        pMessage.append( "<link rel=\"edit\" type=\"application/atom+xml\" href=\"" ).append( path ).append( "\"/>\n" );
      }
    } catch (AccessControlException ace) {           
    }
    return;
  }

  
  
  
  
  
  
  
}
