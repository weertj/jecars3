/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.apps;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import javax.jcr.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Main;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * CARS_LoggerApp
 *
 * @version $Id: CARS_LoggerApp.java,v 1.2 2008/09/26 13:33:56 weertj Exp $
 */
public class CARS_LoggerApp extends CARS_DefaultInterface implements CARS_Interface {
    
  /** Creates a new instance of CARS_TestApp */
  public CARS_LoggerApp() {
  }
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Definitions.VERSION_ID + " $Id: CARS_LoggerApp.java,v 1.2 2008/09/26 13:33:56 weertj Exp $";
  }

  
  public static String readAsString( InputStream pInput ) throws IOException {
    try {
      InputStreamReader isr = new InputStreamReader(pInput);
      BufferedReader br = new BufferedReader(isr);
      StringBuilder buf = new StringBuilder();
      String line;
      while((line = br.readLine()) != null) 
        buf.append(line).append('\n');
      return buf.toString();
    } finally {
    }
  }
  
  @Override
  public void getNodes( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception {
    System.out.println( "Must put the nodes under: " + pParentNode.getPath() );
    System.out.println( "The leaf is (fullpath): " + pLeaf );
    
    if (pLeaf.equals( "/Logger" )) {
      // **** Hey!.... it the root....
      if (pParentNode.hasNode( "Test" )==false) {
        pParentNode.addNode( "Test", "nt:unstructured" );
        pParentNode.save();
      }
    } else {
      if (pParentNode.isNodeType( "jecars:LoggerSource" )) {
        // **** Is logger source node
        System.out.println( " generaiitiait: " + pParentNode.getProperty( "jecars:LogFile" ).getString() );
        FileInputStream fis = new FileInputStream( pParentNode.getProperty( "jecars:LogFile" ).getString() );        
        try {
          String contents = readAsString( fis );
          if (contents.endsWith( "</log>\n")==false) contents += "</log>\n";
          DocumentBuilder  builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();          
          builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity( String pPublicId, String pSystemId) throws SAXException, java.io.IOException
                {
                  if ((pSystemId!=null) && (pSystemId.endsWith(".dtd")))
                    return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                  else return null;
                  }
               });            
          org.w3c.dom.Document doc = builder.parse( new ByteArrayInputStream(contents.getBytes()) );
          org.w3c.dom.NodeList nl = doc.getElementsByTagName( "record" );
          for( int i=0; i<nl.getLength(); i++ ) {
            org.w3c.dom.Node no = nl.item( i );
            org.w3c.dom.NodeList cnl = no.getChildNodes();
            Date time = null;
            String message = "", level = "INFO";
            for( int ii=0; ii<cnl.getLength(); ii++ ) {
              org.w3c.dom.Node cno = cnl.item( ii );
              if (cno.getNodeType()==cno.ELEMENT_NODE) {
//                System.out.println( " --- " + cno.getNodeName() + " = " + cno.getTextContent() );
                if (cno.getNodeName().equals( "millis"  )) time    = new Date( Long.parseLong(cno.getTextContent()));
                if (cno.getNodeName().equals( "message" )) message = cno.getTextContent();
                if (cno.getNodeName().equals( "level" ))   level = cno.getTextContent();
              }
            }
            Calendar c = Calendar.getInstance();
            c.setTime( time );
            if (pParentNode.hasNode( String.valueOf(time.getTime()) )==false) {
              Node logEntry = pParentNode.addNode( String.valueOf(time.getTime()), "jecars:LogEntry" );
//              logEntry.setProperty( "jecars:Created", Calendar.getInstance() );
              logEntry.setProperty( "jecars:Published", c );
              c = Calendar.getInstance();
              c.setTime( time );
              c.add( Calendar.HOUR_OF_DAY, 1 );
              logEntry.setProperty( "jecars:ExpireDate", c );
              logEntry.setProperty( "jecars:Level",   level );
              logEntry.setProperty( "jecars:Title", message );
            }
          }
          pParentNode.save();
        } finally {
          fis.close();
        }
      }
    }
    
    return;
  }

  
}
