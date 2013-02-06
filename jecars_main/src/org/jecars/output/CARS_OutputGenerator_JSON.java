/*
 * Copyright 2009-2012 NLR - National Aerospace Laboratory
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
//import com.google.gson.Gson;
import java.util.Iterator;
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
 * CARS_OutputGenerator_JSON
 *
 * @version $Id: CARS_OutputGenerator_JSON.java,v 1.15 2009/06/21 20:59:41 weertj Exp $
 */
public class CARS_OutputGenerator_JSON extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {

  static private String gFeedHeader = null;
    
  /** Creates a new instance of CARS_OutputGenerator_JSON
   */
  public CARS_OutputGenerator_JSON() {
  }

  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "[\n" );
    pContext.setContentType( "application/json" );
//    pContext.setContentType( "text/plain" );
    return;
  }
  
 /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "\n]\n" );
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
//    Gson gson = new Gson();
//    pMessage.append( "\t\"title\": " ).append( gson.toJson( pThisNode.getName() )).append( ",\n" );
//    pMessage.append( "\t\"title\": \"" ).append( CARS_Utils.convertNodeName(pThisNode) ).append( "\",\n" );
//    if (pThisNode.hasProperty( CARS_ActionContext.gDefTitle )) {
//      pMessage.append( "\t\"subtitle\": \"" ).append(
//              pThisNode.getProperty( CARS_ActionContext.gDefTitle ).getString() ).append( "\",\n" );
//    }
//    pMessage.append( "\t\"nodes\": [\n" );
    /*
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
    */
    return;
  }

  /** Add child node entry
   *
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   * @param pNodeNo first node = 1
   * @throws RepositoryException
   * @throws Exception
   */
  @Override
  public void addChildNodeEntry( final CARS_ActionContext pContext, final CARS_Buffer pMessage, final Node pChildNode, final long pNodeNo ) throws RepositoryException, Exception {
    if (pNodeNo!=1) {
      pMessage.append( "\t,\n" );
    }
    pMessage.append( "\t{\n" );
    pMessage.append( "\t\t\"type\": \"User\",\n" );
    pMessage.append( "\t\t\"id\": " ).append( System.nanoTime() ).append( ",\n" );
    pMessage.append( "\t\t\"name\": \"" ).append( CARS_Utils.convertNodeName(pChildNode) ).append( "\"\n" );
    pMessage.append( "\t}\n" );
/*
    pMessage.append( "<published>" ).append( pContext.getPublishedDate(pChildNode) ).append( "</published>\n" );
    pMessage.append( "<updated>" ).append( pContext.getUpdatedDate(pChildNode) ).append( "</updated>\n" );
    pMessage.append( "<id>" ).append( pContext.getAtomID(pChildNode) ).append( "</id>\n" );
    pMessage.append( "<category term=\"" ).append( pChildNode.getPrimaryNodeType().getName() ).append( "\"/>\n" );
    if (pChildNode.hasProperty( CARS_ActionContext.gDefBody )==true) {
      pMessage.append( "<content type=\"html\"><![CDATA[" );
      pMessage.append( pChildNode.getProperty( CARS_ActionContext.gDefBody ).getString() );
      pMessage.append( "]]></content>\n" );
    }
    pMessage.append( "</entry>\n" );
 */
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
  private void addMultiPropertyEntry( CARS_ActionContext pContext, StringBuilder pReply, Node pThisNode, PropertyIterator pIt  ) throws Exception {
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
          String prefix = p.getName().substring( 0, p.getName().indexOf(':'));
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
         }
       } else if (po instanceof JD_Taglist) {
         JD_Taglist tags = (JD_Taglist)po;
         Iterator it = tags.getIterator();
         String key;
         while( it.hasNext() ) {
           key = (String)it.next();
           pReply.append( "<" ).append( tags.getData( key ) ).append( ">" ).append( key
                   ).append( "</" ).append( tags.getData( key )).append( ">\n" );
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
