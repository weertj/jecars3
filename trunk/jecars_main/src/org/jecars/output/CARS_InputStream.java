/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/** CARS_InputStream
 *
 */
public class CARS_InputStream extends InputStream {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.output" );

  static final private List<CARS_InputStream> OPENSTREAMS = new ArrayList<CARS_InputStream>();

  private final transient Node        mNode;
  private final transient File        mFile;
  private       transient long        mBytesReaden = 0L;
//  private final transient InputStream mStream;
  private final transient RandomAccessFile mStream;


  /** CARS_InputStream
   *
   * @param pFile
   * @throws FileNotFoundException
   */
  public CARS_InputStream( final Node pNode, final File pFile ) throws FileNotFoundException, RepositoryException {
    super();
//    LOG.info( "[OPEN CARS_Inputstream] for: " + pFile.getAbsolutePath() + " (" + pNode.getPath() + ") Partial=" + pNode.getProperty( "jecars:Partial" ).getString() );
    mNode = pNode;
    mFile = pFile;
    mStream = new RandomAccessFile( pFile, "r" );
 
//    mStream = new FileInputStream( pFile );
    OPENSTREAMS.add( this );
    return;
  }

  /** getOpenStreams
   * 
   * @return
   */
  static public List<CARS_InputStream> getOpenStreams() {
    return Collections.unmodifiableList( OPENSTREAMS );
  }

  /** getFile
   * 
   * @return
   */
  public File getFile() {
    return mFile;
  }

  /** getNode
   * 
   * @return
   */
  public Node getNode() {
    return mNode;
  }

  /** getBytesReaden
   * 
   * @return
   */
  public long getBytesReaden() {
    return mBytesReaden;
  }

  /** read
   *
   * @param pBuffer
   * @param pOff
   * @param pLen
   * @return
   * @throws IOException
   */
  @Override
  public int read( final byte pBuffer[], final int pOff, final int pLen ) throws IOException {
    if ((pOff < 0) || (pOff > pBuffer.length) || (pLen < 0) ||
        ((pOff + pLen) > pBuffer.length) || ((pOff + pLen) < 0)) {
        throw new IndexOutOfBoundsException();
    } else if (pLen == 0) {
      return 0;
    }

    int len = mStream.read( pBuffer, pOff, pLen );
    if (len==-1) {
      // **** READ BEYOND BUFFER... wait
      for( int waitLoop = 30; waitLoop>0; waitLoop-- ) {
        try {
          Thread.sleep(1000);
          if (!mNode.getProperty( "jecars:Partial" ).getBoolean()) {
            // **** Extra length check
            if (mNode.getProperty( "jecars:ContentLength" ).getLong()==mBytesReaden) {
              // **** File is completely readen
              return -1;
            }
          }
        } catch (RepositoryException re) {
          LOG.log( Level.WARNING, re.getMessage(), re );
        } catch (InterruptedException ex) {
          LOG.log( Level.WARNING, ex.getMessage(), ex );
        }
        len = mStream.read( pBuffer, pOff, pLen );
//    System.out.println(" DELAY READ RES = " + len + " tot=" + mBytesReaden );
        if (len>0) {
          break;
        }
      }
    }
    if (len>0) {
      mBytesReaden += len;
    }
    return len;
  }


  @Override
  public int read() throws IOException {
    throw new UnsupportedOperationException( "read() not supported" );
  }

  /** close
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    super.close();
    if (OPENSTREAMS.remove( this )) {
//      LOG.log( Level.INFO, "[CLOSE CARS_Inputstream] for: " + mFile.getAbsolutePath() + ", " + mBytesReaden + " bytes readen " );
      mStream.close();
    }
    return;
  }



}
