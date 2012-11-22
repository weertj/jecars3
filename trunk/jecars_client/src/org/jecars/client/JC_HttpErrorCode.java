/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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

import java.io.Serializable;

/**
 * JC_HttpErrorCode
 *
 * @version $Id: JC_HttpErrorCode.java,v 1.4 2008/08/28 13:49:06 weertj Exp $
 */
public class JC_HttpErrorCode implements Serializable {

  private static final long serialVersionUID = 200912251214L;

  private final int    mCode;
  private final String mErrorText;

  /** JC_HttpErrorCode
   *
   * @param pCode
   */
  public JC_HttpErrorCode( final int pCode ) {
    mCode = pCode;
    mErrorText = "";
    return;
  }

  /** JC_HttpErrorCode
   *
   * @param pCode
   * @param pMessage
   */
  public JC_HttpErrorCode( final int pCode, final String pMessage ) {
    mCode = pCode;
    mErrorText = pMessage;
    return;
  }


  /** createHttpErrorCode
   * 
   * @param pCode
   * @return
   */
  static public JC_HttpErrorCode createHttpErrorCode( final int pCode ) {
    return new JC_HttpErrorCode( pCode );
  }
    
  public String getErrorText() {
    return mErrorText;
  }
    
//  public void setErrorText( String pErrorText ) {
//    mErrorText = pErrorText;
//    return;
//  }
  
  public int getErrorCode() {
    return mCode;
  }
  
//  public void setErrorCode( int pCode ) {
//    mCode = pCode;
//    return;
//  }
    
}
