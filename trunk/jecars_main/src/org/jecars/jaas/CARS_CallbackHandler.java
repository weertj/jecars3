/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.jaas;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * CARS_CallbackHandler
 *
 * @version $Id: CARS_CallbackHandler.java,v 1.1 2007/09/26 14:14:37 weertj Exp $
 */
public class CARS_CallbackHandler implements CallbackHandler {

  private String mUsername = null;
  private char[] mPassword = null;
    
  /** Creates a new instance of CARS_CallbackHandler */
  public CARS_CallbackHandler() {
  }

  /** Creates a new instance of CARS_CallbackHandler */
  public CARS_CallbackHandler( String pUsername, char[] pPassword ) {
    mUsername = pUsername;
    mPassword = pPassword;
    return;
  }
    
  
  @Override
  public void handle( Callback[] pCallbacks ) throws IOException, UnsupportedCallbackException {
    for (int i=0; i<pCallbacks.length; i++ ) {
      Callback cb = pCallbacks[i];
      if (cb instanceof NameCallback) {
        NameCallback ncb = (NameCallback)cb;
        ncb.setName( mUsername );
      } else if (cb instanceof PasswordCallback ) {
	PasswordCallback pcb = (PasswordCallback)cb;
        pcb.setPassword( mPassword );
      } else {
        throw new UnsupportedCallbackException( cb );
      }
    }
    return;
  }
  
  
}
