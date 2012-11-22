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

/**
 * JC_Exception
 *
 * @version $Id: JC_Exception.java,v 1.6 2008/08/28 13:49:05 weertj Exp $
 */
public class JC_Exception extends Exception {

  final static public int ERROR_PROPERTYNOTFOUND = 99001;
  final static public int ERROR_CANNOTCONNECT    = 99002;
    
  protected int         mErrorCode = 0;
  protected JC_Itemable mErrorItem = null;
  protected String      mServerMessage = null;

  /** createErrorException
   * 
   * @param pMessage
   * @return 
   */
  static protected JC_Exception createErrorException( String pMessage ) {
    JC_Exception e = new JC_Exception( "[ERROR] " + pMessage );
    return e;
  }

  static public JC_Exception createErrorException( String pMessage, Exception pE ) {
    JC_Exception e = new JC_Exception( "[ERROR] " + pMessage, pE );    
    return e;
  }

  static protected JC_Exception createErrorException( int pCode, String pMessage ) {
    JC_Exception e = new JC_Exception( "[ERROR " + pCode + "] " + pMessage );
    e.mErrorCode = pCode;
    return e;
  }

  static protected JC_Exception createErrorException( int pCode, String pMessage, String pServerMessage ) {
    JC_Exception e = createErrorException( pCode, pMessage + ": " + pServerMessage );
    e.mServerMessage = pServerMessage;
    return e;
  }
  
  public JC_Exception() {
    super();
  }
    
  /**
   * Creates a new instance of <code>JC_Exception</code> without detail message.
   */
  public JC_Exception( Exception pE ) {
    super(pE);
  }

  /** JC_Exception
   * 
   * @param msg
   * @param pE
   */
  public JC_Exception( String msg, Exception pE ) {
    super(msg, pE);
  }

  /**
   * Constructs an instance of <code>JC_Exception</code> with the specified detail message.
   * @param msg the detail message.
   */
  public JC_Exception(String msg) {
    super(msg);
  }
  
  /** setErrorItemable
   * @param pItem
   */
  public void setErrorItemable( JC_Itemable pItem ) {
    mErrorItem = pItem;
    return;
  }
  
  /** getErrorItemable
   * @return If not null the item is the main reason which caused the error
   */
  public JC_Itemable getErrorItemable() {
    return mErrorItem;
  }
 
  public String getServerMessage() {
    return mServerMessage;
  }
  
  /** getErrorCode
   * 
   * @return
   */
  public int getErrorCode() {
    return mErrorCode;
  }
  
}
