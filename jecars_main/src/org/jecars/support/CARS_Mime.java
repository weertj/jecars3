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
package org.jecars.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * CARS_Mime
 *
 * @version $Id: CARS_Mime.java,v 1.3 2009/06/19 11:57:36 weertj Exp $
 */
public class CARS_Mime {
  
  static final private Logger gLog = Logger.getLogger( "org.jecars.support" );

  static final public String BACKUP_MIMETYPE = "text/jecars-backup";

    
  /** Creates a new instance of CARS_Mime */
  public CARS_Mime() {

  } 
  
  /** Check for mime type
   * @param pName the name, must(!) converted to lowercase
   */
  static public String getMIMEType( final String pName, final InputStream pStream ) { //byte[] pContents ) {
    String mime = null;
    if (pName!=null) mime = URLConnection.guessContentTypeFromName( pName );
    if (mime==null) {
      if (pStream!=null) {
        try {
          mime = URLConnection.guessContentTypeFromStream( pStream );
        } catch (IOException e) {            
        }
      }
      if (mime==null) {
        if (pName.endsWith( ".bat" )) mime = "application/bat";
        else if (pName.endsWith( ".dat" )) mime = "application/dat";
        else if (pName.endsWith( ".pdf" )) mime = "application/pdf";
        else if (pName.endsWith( ".doc" )) mime = "application/msword";
        else if (pName.endsWith( ".odg" )) mime = "application/vnd.oasis.opendocument.graphics";
        else if (pName.endsWith( ".html" ))  mime = "text/html";
        else if (pName.endsWith( ".htm"  ))  mime = "text/html";
        else if (pName.endsWith( ".jnlp"  )) mime = "application/x-java-jnlp-file";
      }
    }
    if (mime==null) mime = "text/plain";
    return mime;
  }
  
}
