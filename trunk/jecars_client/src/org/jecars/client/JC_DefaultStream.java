/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * JC_DefaultStream
 *
 * @version $Id: JC_DefaultStream.java,v 1.4 2009/03/05 15:59:52 weertj Exp $
 */

public class JC_DefaultStream implements JC_Streamable {

  private InputStream mStream          = null;
  private String      mContentType     = null;
  private String      mContentEncoding = null;
  private long        mContentLength   = -1L;

  /** createStream
   * 
   * @param pString
   * @param pContentType
   * @return
   */
  static public JC_Streamable createStream( final String pString, final String pContentType ) {
    final JC_DefaultStream ds = new JC_DefaultStream();
    ds.setStream( new ByteArrayInputStream( pString.getBytes() ) );
    ds.setContentType( pContentType );
    ds.setContentLength( pString.length() );
    return ds;
  }

  /** createStream
   *
   * @param pData
   * @param pContentType
   * @return
   */
  static public JC_Streamable createStream( final byte[] pData, final String pContentType ) {
    final JC_DefaultStream ds = new JC_DefaultStream();
    ds.setStream( new ByteArrayInputStream( pData ) );
    ds.setContentType( pContentType );
    ds.setContentLength( pData.length );
    return ds;
  }

  /** createStream
   * @param pStream
   * @return
   */
  static public JC_Streamable createStream( final InputStream pStream, final String pContentType ) {
    return createStream( pStream, pContentType, null );
  }

  /** createStream
   * 
   * @param pFile
   * @param pContentType
   * @return
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */
  static public JC_Streamable createStream( final File pFile, final String pContentType ) throws FileNotFoundException, IOException {
    JC_DefaultStream ds;
    final FileInputStream fis = new FileInputStream( pFile );
//    try {
      ds = new JC_DefaultStream();
      ds.setContentLength( pFile.length() );
      ds.setStream( fis );
      ds.setContentType( pContentType );
//    } finally {
//    }
    return ds;
  }

  /** createStream
   *
   * @param pStream
   * @param pContentType
   * @param pContentEncoding
   * @return
   */
  static public JC_Streamable createStream( final InputStream pStream, final String pContentType, final String pContentEncoding ) {
    final JC_DefaultStream ds = new JC_DefaultStream();
    ds.setStream( pStream );
    ds.setContentType( pContentType );
    ds.setContentEncoding( pContentEncoding );
    return ds;
  }


  @Override
  public void destroy() throws IOException {
    if (mStream!=null) {
      mStream.close();
      mStream = null;
    }
    return;
  }

  @Override
  public InputStream getStream() {
    return mStream;
  }
  
  @Override
  public void setStream( final InputStream pStream ) {
    mStream = pStream;
    return;
  }
  
  @Override
  public long getContentLength() {
    return mContentLength;
  }
  
  @Override
  public void setContentLength( final long pLength ) {
    mContentLength = pLength;
    return;
  }
  
  @Override
  public String getContentType() {
    return mContentType;
  }
  
  @Override
  public void setContentType( final String pType ) {
    mContentType = pType;
    return;
  }

  /** getContentEncoding
   * 
   * @return
   */
  @Override
  public String getContentEncoding() {
    return mContentEncoding;
  }

  /** setContentEncoding
   *
   * @param pEncoding
   */
  @Override
  public void setContentEncoding( final String pEncoding ) {
    mContentEncoding = pEncoding;
    return;
  }

  /** readAsByteArray
   *
   * @return
   * @throws IOException
   */
  @Override
  public byte[] readAsByteArray() throws IOException {
    final InputStream          is = getStream();
    final BufferedInputStream bis = new BufferedInputStream( is );
    byte[] result;
    try {
      final int bufLen = 20000;
      final byte[] buf = new byte[bufLen];
       byte[] tmp = null;
       int len = 0;
       final List<byte []> data  = new ArrayList<byte[]>();
       while((len = bis.read(buf,0,bufLen)) != -1){
          tmp = new byte[len];
          System.arraycopy( buf, 0, tmp, 0, len );
          data.add(tmp);
       }
       len = 0;
       if (data.size() == 1) {
         result = data.get(0);
       } else {
         for (int i=0;i<data.size();i++) {
           len += ((byte[]) data.get(i)).length;
         }
         result = new byte[len];
         len = 0;
         for (byte[] bs : data) {
           System.arraycopy( bs, 0, result, len, bs.length );
           len += bs.length;
         }
       }
    } finally {
      if (bis!=null) {
        bis.close();
      }
      if (is!=null) {
        is.close();
      }
    }
    return result;
  }


  /** writeToStream
   *
   * @param pOutput
   * @throws IOException
   */
  @Override
  public void writeToStream( final OutputStream pOutput ) throws IOException {
    JC_Utils.sendInputStreamToOutputStream( 20000, getStream(), pOutput );
    return;
  }

  @Override
  public void writeToStreamNoClosing( final OutputStream pOutput ) throws IOException {
    JC_Utils.sendInputStreamToOutputStreamNoClosing( 20000, getStream(), pOutput );
    return;
  }


}
