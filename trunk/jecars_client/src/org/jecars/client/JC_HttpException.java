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

/**
 * JC_HttpException
 *
 * @version $Id: JC_HttpException.java,v 1.3 2009/02/09 10:27:48 weertj Exp $
 */
public class JC_HttpException extends JC_Exception {

  private JC_HttpErrorCode mHttpErrorCode = null;

  /** createErrorHttpException
   *
   * @param pMessage
   * @return
   */
  static protected JC_HttpException createErrorHttpException( final String pMessage ) {
    return new JC_HttpException( "[ERROR] " + pMessage );
  }

  /** createErrorHttpException
   *
   * @param pMessage
   * @param pE
   * @return
   */
  static protected JC_HttpException createErrorHttpException( final String pMessage, final Exception pE ) {
    return new JC_HttpException( "[ERROR] " + pMessage, pE );
  }

  /** createErrorHttpException
   *
   * @param pCode
   * @param pMessage
   * @param pE
   * @return
   */
  static protected JC_HttpException createErrorHttpException( final int pCode, final String pMessage, final Exception pE ) {
    final JC_HttpException e = new JC_HttpException( "[ERROR " + pCode + "] " + pMessage, pE );
    e.mHttpErrorCode = JC_HttpErrorCode.createHttpErrorCode( pCode );
    return e;
  }

  /** createErrorHttpException
   * 
   * @param pCode
   * @param pMessage
   * @return
   */
  static protected JC_HttpException createErrorHttpException( final int pCode, final String pMessage ) {
    final JC_HttpException e = new JC_HttpException( "[ERROR " + pCode + "] " + pMessage );
    e.mHttpErrorCode = JC_HttpErrorCode.createHttpErrorCode( pCode );
    return e;
  }

  /** createErrorHttpException
   * 
   * @param pCode
   * @param pMessage
   * @param pServerMessage
   * @return
   */
  static protected JC_HttpException createErrorHttpException( final int pCode, final String pMessage, final String pServerMessage ) {
    final JC_HttpException e = createErrorHttpException( pCode, pMessage + ": " + pServerMessage );
    e.mServerMessage = pServerMessage;
    return e;
  }

  /**
   * Creates a new instance of <code>JC_Exception</code> without detail message.
   */
  public JC_HttpException( final Exception pE ) {
    super(pE);
  }

  /**
   * Creates a new instance of <code>JC_Exception</code> without detail message.
   */
  public JC_HttpException( final String pE ) {
    super(pE);
  }

  /**
   * Creates a new instance of <code>JC_Exception</code> without detail message.
   */
  public JC_HttpException( final String pS, final Exception pE ) {
    super(pS,pE);
  }
  
  /** getHttpErrorCode
   * @return
   */
  public JC_HttpErrorCode getHttpErrorCode() {
    return mHttpErrorCode;
  }

  /** setHttpErrorCode
   * 
   */
  public void setHttpErrorCode( final JC_HttpErrorCode pHEC ) {
    mHttpErrorCode = pHEC;
    return;
  }
  
  /** setErrorCode
   *
   * @param pErrorCode
   */
  public void setErrorCode( final int pErrorCode ) {
    if (mHttpErrorCode!=null) {
      if (mHttpErrorCode.getErrorCode()!=pErrorCode) {
        mHttpErrorCode = new JC_HttpErrorCode( pErrorCode, mHttpErrorCode.getErrorText() );
      }
    } else {
      mHttpErrorCode = JC_HttpErrorCode.createHttpErrorCode( pErrorCode );
    }
    return;
  }
    
}
