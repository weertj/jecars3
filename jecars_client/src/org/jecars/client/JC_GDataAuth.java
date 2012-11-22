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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import nl.msd.jdots.JD_Taglist;

/**
 * JC_GDataAuth
 *
 * @version $Id: JC_GDataAuth.java,v 1.8 2009/01/29 16:00:37 weertj Exp $
 */
public class JC_GDataAuth implements Serializable {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.client" );

  private String mAuth = null;

  static final public String AUTH = "GOOGLELOGIN_AUTH";
  
  /** create
   * @param pAuth
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_GDataAuth create( final String pAuth ) throws JC_Exception {
    final JC_GDataAuth auth = new JC_GDataAuth();
    auth.setAuth( pAuth );
    return auth;
  }
  
  /** getAuth
   * @return
   */
  public String getAuth() {
    return mAuth;
  }
  
  /** setAuth
   * @param pAuth
   */
  protected void setAuth( final String pAuth ) {
//    gLog.log( Level.INFO, "Auth key set: " + pAuth );
    mAuth = pAuth;
    mAuth = mAuth.trim();
    while( mAuth.endsWith( "\n" ) ) {
      mAuth = mAuth.substring( 0, mAuth.length()-1 );
    }
    return;
  }
  
  /** setAuth
   * @param pClient
   * @throws org.jecars.client.JC_Exception
   */
  protected void setAuth( final JC_Clientable pClient, final long pKeyValidInMinutes, final String pSource, final long pValidationExtension ) throws JC_Exception {
    try {
      final JC_RESTComm comm = pClient.getRESTComm();
      final String url = pClient.getServerPath() + "/accounts/ClientLogin?X-HTTP-Method-Override=POST&Email=" +
                   comm.getUsername();
      final HttpURLConnection conn;
      if (pKeyValidInMinutes>0) {
        conn = comm.createHttpConnection( url +
            "&Passwd=" + String.valueOf(comm.getPassword()) +
            "&Source=" + pSource +
            "&KeyValidInMinutesInitial=" + pKeyValidInMinutes +
            "&ExtendValidationInMinutes=" + pValidationExtension );
      } else {
        conn = comm.createHttpConnection( url +
            "&Passwd=" + String.valueOf(comm.getPassword()) +
            "&Source=" + pSource );
      }
      
      final JD_Taglist tags = comm.sendMessageGET( pClient, conn );
      if (JC_RESTComm.getResponseCode(tags)==HttpURLConnection.HTTP_OK) {
        final InputStream is = JC_RESTComm.getResultStream( tags );
        final String auth = JC_Utils.readAsString( is );
        if (auth.startsWith( "Auth=" )) {
          setAuth( auth.substring( "Auth=".length() ) );
        } else {
          throw JC_Utils.createCommException( tags, "Invalid Auth key returned: " + auth, url );
        }
      } else {
        throw JC_Utils.createCommException( tags, "while updating object ", url );
      }    
    } catch( MalformedURLException mue ) {
      throw JC_Exception.createErrorException( mue.getMessage(), mue );      
    } catch( IOException ie ) {
      throw JC_Exception.createErrorException( ie.getMessage(), ie );      
    }
//    } catch( Exception e ) {
//      throw new JC_Exception( e );
//    }
    return;
  }
  
  /** How long is the current login session valid
   * @return time in millis
   * @throws org.jecars.client.JC_Exception
   */
  public long getLoginValidTime() throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
