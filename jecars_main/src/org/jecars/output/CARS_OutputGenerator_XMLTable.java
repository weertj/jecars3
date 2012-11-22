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
import java.util.StringTokenizer;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_XML
 *
 */
public class CARS_OutputGenerator_XMLTable extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {

  /** Creates a new instance of CARS_OutputGenerator_HTML */
  public CARS_OutputGenerator_XMLTable() {
  }

  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {

    try {
      pMessage.append( "<recordset><metadata>\n" );
      final JD_Taglist tags = pContext.getQueryPartsAsTaglist();
      final String items = (String)tags.getData( "jecars:getProperties" );
      StringTokenizer st = new StringTokenizer( items, "|" );
      int i = 0;
      while( st.hasMoreTokens() ) {
        String item = st.nextToken();
        pMessage.append( "<column id=\"" ).append( i ).append( "\" label=\"").append(
                item ).append( "\" name=\"" ).append( item ).append( "\" type=\"string\"/>\n" );
        i++;
      }
    } catch( Exception e ) {
      pMessage.append( e.getMessage() );
    } finally {
      pMessage.append( "</metadata>\n<records xmlns:jecars='http://jecars.org/'>\n" );
    }
    pContext.setContentType( "application/xml" );
    return;
  }
  
 /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "</records>\n</recordset>\n" );
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
    
    
    return;
  }

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   */   
  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {

    final JD_Taglist tags = pContext.getQueryPartsAsTaglist();
    final String items = (String)tags.getData( "jecars:getProperties" );
      
    pMessage.append( "<r>" );
    if (items!=null) {
//      pMessage.append( "<title>" ).append( CARS_Utils.convertNodeName(pChildNode) ).append( "</title>" );   
      PropertyIterator pi = pChildNode.getProperties();
      Property p;
      while (pi.hasNext()) {
        p = pi.nextProperty();
        if (items.contains( "|" + p.getName() + "|" )) {
          if (!p.getDefinition().isMultiple()) {
            final String name = p.getName();
            pMessage.append( '<' ).append( name ).append( ">" ).append(
                  StringUtil.xmlEscape(p.getValue().getString()) ).append(
                           "</" ).append( name ).append( ">" );
          }
        }
      }
      
      if (items.contains( "|jecars:self|" )) {
        String path = pContext.getBaseContextURL() + CARS_Utils.getEncodedPath( pChildNode );
        pMessage.append( "<jecars:self>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars:self>" );
      }
      if (items.contains( "|jecars:path|" )) {
        String path = CARS_Utils.getEncodedPath( pChildNode );
        pMessage.append( "<jecars:path>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars:path>" );
      }
      if (items.contains( "|jecars:name|" )) {
        String path = pChildNode.getName();
        pMessage.append( "<jecars:name>" ).append(
                  StringUtil.xmlEscape( path )).append( "</jecars:name>" );
      }

      
    }

    pMessage.append( "</r>\n" );
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
