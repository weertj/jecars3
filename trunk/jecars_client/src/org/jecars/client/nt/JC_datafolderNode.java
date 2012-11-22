/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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

package org.jecars.client.nt;

import java.io.File;
import java.io.IOException;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_DefaultStream;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Streamable;

/**
 *
 * @author weert
 */
public class JC_datafolderNode extends JC_DefaultNode {
 
  public void addFile( final File pFile, final String pMimeType ) throws JC_Exception, IOException {
    JC_Nodeable newNode = addNode( pFile.getName(), "jecars:datafile" );
    JC_Streamable stream = JC_DefaultStream.createStream( pFile, pMimeType );
    stream.setContentLength( pFile.length() );
    newNode.setProperty(stream);
    newNode.save();
    return;
  }

  public void addFile( final File pFile, final String pMimeType, final String pNodeType ) throws JC_Exception, IOException {
    JC_Nodeable newNode = addNode( pFile.getName(), pNodeType );
    JC_Streamable stream = JC_DefaultStream.createStream( pFile, pMimeType );
    stream.setContentLength( pFile.length() );
    newNode.setProperty(stream);
    newNode.save();
    return;
  }
    
}
