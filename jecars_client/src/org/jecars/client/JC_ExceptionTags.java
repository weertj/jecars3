/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client;

import nl.msd.jdots.JD_Taglist;

/**
 *
 * @author weert
 */
public class JC_ExceptionTags extends JC_HttpException {
  
  static final public int   JECARSERROR_UNKNOWN                 = 1;
  static final public int   JECARSERROR_PATHNOTFOUND            = 2;
  static final public int   JECARSERROR_AUTHENTICATION_NEEDED   = 3;
    
  static final public String ERRORTAG_CODE          = "ET_CODE";
  static final public String ERRORTAG_OBJECTURL     = "ET_OBJECTURL";
  static final public String ERRORTAG_XMLBODY       = "ET_XMLBODY";
  static final public String ERRORTAG_SERVERERROR   = "ET_SERVERERROR";
  static final public String ERRORTAG_PATH          = "ET_PATH";
  
  private final JD_Taglist mErrorTags       = new JD_Taglist();
  private       int        mJeCARSErrorCode = JECARSERROR_UNKNOWN;

  public JC_ExceptionTags( final String pE ) {
    super( pE );
  }
  
  public int getJeCARSErrorCode() {
    return mJeCARSErrorCode;
  }
  
  public void setJeCARSErrorCode( final int pCode ) {
    mJeCARSErrorCode = pCode;
    return;
  }
  
  public void putErrorTags( final String pTag, final Object pValue ) {
    mErrorTags.putData( pTag, pValue );
    return;
  }
  
  public JD_Taglist getErrorTags() {
    return mErrorTags;
  }
  
}
