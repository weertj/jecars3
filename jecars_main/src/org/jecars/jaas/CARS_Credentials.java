/**
 * CARS_Credentials
 *
 */

package org.jecars.jaas;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.Credentials;
import org.jecars.CARS_ActionContext;

/** CARS_Credentials
 *
 * @author weert
 */
public class CARS_Credentials implements Credentials {

    private final String                mUserID;
    private final char[]                mPassword;
    private final Map<String, Object>   mAttributes = new HashMap<String, Object>();
    private final CARS_ActionContext    mContext;

    /** CARS_Credentials
     * 
     * @param pUserID
     * @param pPassword
     */
    public CARS_Credentials( final String pUserID, final char[] pPassword, final CARS_ActionContext pContext ) {
      mUserID   = pUserID;
      if (pPassword==null) {
        mPassword = "".toCharArray();        
      } else {
        mPassword = (char[])pPassword.clone();
      }
      mContext  = pContext;
      return;
    }

    /** getContext
     *
     * @return
     */
    public CARS_ActionContext getContext() {
      return mContext;
    }
    
    public char[] getPassword() {
      return mPassword;
    }

    public String getUserID() {
      return mUserID;
    }

    public void setAttribute( final String pName, final Object pValue) {
      if (pName == null) {
        throw new IllegalArgumentException("name cannot be null");
      }
      if (pValue == null) {
         removeAttribute( pName );
         return;
      }
      synchronized( mAttributes ) {
        mAttributes.put( pName, pValue );
      }
      return;
    }

    public Object getAttribute( final String pName ) {
      synchronized( mAttributes ) {
        return (mAttributes.get(pName));
      }
    }

    public void removeAttribute( final String pName ) {
      synchronized( mAttributes ) {
	mAttributes.remove( pName );
      }
    }

    public String[] getAttributeNames() {
      synchronized( mAttributes ) {
        return (String[])mAttributes.keySet().toArray(new String[mAttributes.keySet().size()]);
      }
    }
    
}
