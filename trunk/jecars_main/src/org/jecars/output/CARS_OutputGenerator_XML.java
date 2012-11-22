/*
 * Copyright 2011-2012 NLR - National Aerospace Laboratory
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
import org.jecars.CARS_Factory;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_XML
 *
 */
public class CARS_OutputGenerator_XML extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {

  static private String gFeedHeader = null;
    
  /** Creates a new instance of CARS_OutputGenerator_HTML */
  public CARS_OutputGenerator_XML() {
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
                      "<feed\n";
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
    pContext.setContentType( "application/xml" );
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
    pMessage.append( "<published>" ).append( pContext.getPublishedDate(pThisNode) ).append( "</published>\n" );
    pMessage.append( "<updated>" ).append( pContext.getUpdatedDate(pThisNode) ).append( "</updated>\n" );
    pMessage.append( "<id>" ).append( pContext.getAtomID(pThisNode) ).append( "</id>\n" );
    pMessage.append( "<category>" ).append( pThisNode.getPrimaryNodeType().getName() ).append( "</category>\n" );
    if (pThisNode.hasProperty( CARS_ActionContext.gDefBody )) {
      pMessage.append( "<content type=\"html\"><![CDATA[<p>" );
      pMessage.append( pThisNode.getProperty( CARS_ActionContext.gDefBody ).getString() );
      pMessage.append( "</p>]]></content>\n" );
    }
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
      
    pMessage.append( "<entry xmlns:jecars=\"http://jecars.org/\">\n" );
    pMessage.append( "<title>" ).append( CARS_Utils.convertNodeName(pChildNode) ).append( "</title>\n" );   
    pMessage.append( "<published>" ).append( pContext.getPublishedDate(pChildNode) ).append( "</published>\n" );
    pMessage.append( "<updated>" ).append( pContext.getUpdatedDate(pChildNode) ).append( "</updated>\n" );
    pMessage.append( "<id>" ).append( pContext.getAtomID(pChildNode) ).append( "</id>\n" );
    pMessage.append( "<category>" ).append( pChildNode.getPrimaryNodeType().getName() ).append( "</category>\n" );
    final JD_Taglist tags = pContext.getQueryPartsAsTaglist();
    final String gap = (String)tags.getData( "jecars:getAllProperties" );
    if ((gap!=null) && (gap.equals( "true" ))) {
      PropertyIterator pi = pChildNode.getProperties();
      Property p;
      while (pi.hasNext()) {
        p = pi.nextProperty();
        if (p.getDefinition().isMultiple()) {
          // **** TODO: multiple properties not supported
        } else {
          final String name = p.getName().replace( ":", "_" );
          pMessage.append( '<' ).append( name ).append( ">" ).append(
                  StringUtil.xmlEscape(p.getValue().getString()) ).append(
                           "</" ).append( name ).append( ">\n" );
        }
      }
    }
    String path = pContext.getBaseContextURL() + CARS_Utils.getEncodedPath( pChildNode );
    pMessage.append( "<jecars_self>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars_self>" );
    path = CARS_Utils.getEncodedPath( pChildNode );
    pMessage.append( "<jecars_path>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars_path>" );
    path = pChildNode.getName();
    pMessage.append( "<jecars_name>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars_name>" );

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
     return;
   }

  /** _addPropertyVString
   * 
   * @param pSB
   * @param pProp
   * @param pValue
   */
  private void _addPropertyVString( StringBuilder pSB, final String pProp, final Value pValue ) throws ValueFormatException, IllegalStateException, RepositoryException {
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
  private void _addPropertyBinString( StringBuilder pSB, final String pProp, final Value pValue ) throws ValueFormatException, Exception {
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
  private void addPropertyValue( CARS_ActionContext pContext, StringBuilder pBuilder, String pPropName, Node pNode, Value pValue ) throws Exception {
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

  
}
