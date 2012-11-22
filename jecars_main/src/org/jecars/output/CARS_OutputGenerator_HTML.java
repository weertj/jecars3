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

import com.google.gdata.util.common.html.HtmlToText;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import org.jecars.CARS_ActionContext;
import org.jecars.support.CARS_Buffer;

/**
 * CARS_OutputGenerator_HTML
 *
 * @version $Id: CARS_OutputGenerator_HTML.java,v 1.8 2009/03/24 12:34:29 weertj Exp $
 */
public class CARS_OutputGenerator_HTML extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
    
  /** Creates a new instance of CARS_OutputGenerator_HTML */
  public CARS_OutputGenerator_HTML() {
  }

  
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */   
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "<html><head></head><body>\n" );
    pMessage.append( getStyle() );
    pContext.setContentType( "text/html" );
    return;
  }

  /** Create the footer of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override   
  public void createFooter( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pMessage.append( "</body></html>\n" );
  }

  /** Add node entry, the current (this) node
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pThisNode the this node
   * @param pFromNode
   * @param pToNode
   */   
  @Override
  public void addThisNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pThisNode,
                                long pFromNode, long pToNode ) throws RepositoryException, Exception {
    
    pMessage.append( generateNodesHTML( pContext, pThisNode ));
    return;
  }

  /** Add child node entry
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   * @param pChildNode the this node
   */
  @Override
  public void addChildNodeEntry( CARS_ActionContext pContext, final CARS_Buffer pMessage, Node pChildNode, long pNodeNo ) throws RepositoryException, Exception {
      
    pMessage.append( generateNodesHTML( pContext, pChildNode ));
      
    return;
  }
  
  
  
   protected String getStyle() {
    return "<style>\n" +
           "A.cars_nodelink:link, A.cars_nodelink:active, A.cars_nodelink:hover" +
           "{" +
           "font-size: 18px;" +
           "font-family: Lucida Sans, Tahoma, Arial, Sans-Serif;" +
           "font-weight: bold;" +
           "line-height: 24px;" +
           "text-decoration: none;" +
           "color: Navy;" +
           "}" +
           "A.cars_nodelink:visited" +
           "{" +
           "font-size: 18px;" +
           "font-family: Lucida Sans, Tahoma, Arial, Sans-Serif;" +
           "text-decoration: none;" +
           "font-weight: bold;" +
           "color: Purple;" +
           "line-height: 24px;" +
           "}" +
            
           "A.cars_nodelinkhtml:link, A.cars_nodelinkhtml:active, A.cars_nodelinkhtml:hover" +
           "{" +
           "font-size: 9px;" +
           "font-family: Lucida Sans, Tahoma, Arial, Sans-Serif;" +
           "font-weight: plain;" +
           "text-decoration: none;" +
           "color: Green;" +
           "}" +
           "A.cars_nodelinkhtml:visited" +
           "{" +
           "font-size: 9px;" +
           "font-family: Lucida Sans, Tahoma, Arial, Sans-Serif;" +
           "text-decoration: none;" +
           "font-weight: plain;" +
           "color: Purple;" +
           "}" +
            
           ".cars_node" +
           "{" +
           "border: 2px solid lightsteelblue;" +
           "background-color: #eeeeee;" +
           "padding: 2px;" +
           "margin-top: 2px;" +
           "margin-bottom: 2px;" +
           "font-size: 9pt;" +           
           "</style>\n";
      
  }
  
/** Generate HTML output
   */
  protected String generateNodesHTML( CARS_ActionContext pContext, Node pNode ) throws RepositoryException, Exception {

    StringBuilder sb = new StringBuilder();

    // *****************************
    // **** HTML simple
    String title;
    if (pNode.hasProperty( CARS_ActionContext.gDefTitle )==true) {
      title = pNode.getProperty( CARS_ActionContext.gDefTitle ).getString();
    } else {
      title = pNode.getName();
    }
    sb.append( "\n<div class=cars_node>\n");
    sb.append( "<div><a class=cars_nodelink href=\"" ).append( pContext.getBaseContextURL() + pNode.getPath() ).append( "\">" );
    sb.append( title ).append( "</a><font size=-2></font><font size=-1> </font></div>");
    sb.append( "<div><pre>\n" );
//    sb.append( "Published date = " ).append( pContext.getPublishedDate( pNode ) );
//    sb.append( "\nUpdated   date = " ).append( pContext.getUpdatedDate( pNode ) );
//    sb.append( "\nCategory       = " ).append( pNode.getPrimaryNodeType().getName() );

    PropertyIterator pi = pNode.getProperties();
    Property prop;
    while( pi.hasNext() ) {
      prop = pi.nextProperty();
      if (prop.getName().equals("jcr:data") && pNode.hasProperty("jcr:mimeType") && (!pNode.getProperty("jcr:mimeType").getValue().getString().startsWith("text/"))) {
        // **** displaying arbitrary binary content using val.getString caused problem below.
        // **** so, do not display jcr:data if node has mimeType, property and mimeType does not start with "text":
        //   java.lang.OutOfMemoryError: Java heap space
        //   java.lang.StringCoding$StringDecoder.decode(StringCoding.java:133)
        //   java.lang.StringCoding.decode(StringCoding.java:173)
        //   java.lang.String.<init>(String.java:444)
        //   java.lang.String.<init>(String.java:516)
        //   org.apache.jackrabbit.value.BinaryValue.getInternalString(BinaryValue.java:144)
        //   org.apache.jackrabbit.value.BaseValue.getString(BaseValue.java:207)
        sb.append( "jcr:data\t\t<i>&lt;Value of mimeType="+pNode.getProperty("jcr:mimeType").getValue().getString()+"&gt;</i>\n" );
      } else try {
        if (prop.getDefinition().isMultiple()==true) {
          sb.append( prop.getName() ).append( " (Multivalue)" );
          Value[] vals = prop.getValues();
          for (Value val : vals) {
            sb.append( "\t\t" ).append( val.getString() ).append( "\n" );
          }
        } else {
          sb.append( prop.getName() ).append( "\t\t" ).append( HtmlToText.htmlToPlainText(prop.getValue().getString()) ).append( "\n" );
        }
      } catch( Exception e ) {
      }
    }

    sb.append( "</pre></div>\n" );
    sb.append( "<div><span>\n<a class=cars_nodelinkhtml href=\"" );
    sb.append( pContext.getBaseContextURL() + pNode.getPath() + "?alt=html" ).append( "\">").append( pContext.getBaseContextURL() + pNode.getPath() + "?alt=html" ).append( "</a>");
    sb.append( " - ??k - </span><nobr>" );
    sb.append( "</div></div>\n");
    return sb.toString();
  }
  
  /** encodeForHTML
   * HTML encoder: only replaces <,>,& to ensure that values containing "<",">" 
   * (e.g. when mime type text/xml) are (more or less) shown properly
   * @param s encode this String
   * @return encoded string
   */
  @Deprecated
  public static String encodeForHTML(String s) {
    char [] htmlChars = s.toCharArray();
    StringBuilder encoded = new StringBuilder();
    for (int i=0; i<htmlChars.length; i++) {
      switch(htmlChars[i]) {
        case '<':  
          encoded.append("&lt;");
          break;
        case '>':
          encoded.append("&gt;");
          break;
        case '&':
          encoded.append("&amp;");
          break;
        default:
          encoded.append(htmlChars[i]);
          break;
      }
    }
    return encoded.toString();
  }
}
