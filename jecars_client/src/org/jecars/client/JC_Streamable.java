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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JC_Streamable
 *
 * @version $Id: JC_Streamable.java,v 1.3 2008/10/21 10:13:05 weertj Exp $
 */
public interface JC_Streamable {

  void destroy() throws IOException;
    
  InputStream getStream();
  void setStream( InputStream pStream );

  String getContentEncoding();
  void   setContentEncoding( final String pEncoding );

  long getContentLength();  
  void setContentLength( long pLength );

  String getContentType();
  void   setContentType( String pType );

  byte[] readAsByteArray() throws IOException;

  void writeToStream( final OutputStream pOutput ) throws IOException;
  void writeToStreamNoClosing( final OutputStream pOutput ) throws IOException;

}
