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
import java.util.Map;
import javax.jcr.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.jecars.CARS_Factory;

/**
 *
 * @author weert
 */
public class CARS_LoginModule implements LoginModule {
  
  private Subject           mSubject;
  private CallbackHandler   mHandler;
//  private Map               mSharedState;
//  private Map               mOptions;
  private String            mUsername = null;

  private boolean           mLoginOk = false;  
  private CARS_Principal    mUsernamePrincipal;
  private Object            mPassword;

     
  @Override
  public void initialize( Subject pSubject, CallbackHandler pCallbackHandler,  Map pSharedState, Map pOptions ) {
    mSubject     = pSubject;
    mHandler     = pCallbackHandler;
//    mSharedState = pSharedState;
//    mOptions     = pOptions;
    return;
  }

     
  @Override
  public boolean login() throws LoginException {
    NameCallback     nc = new NameCallback( "username" );
    PasswordCallback pc = new PasswordCallback( "password", false );
    try {
      mHandler.handle( new Callback[]{ nc, pc }  );
      System.out.println( "checking username: " + nc.getName() );
      System.out.println( "  password: " + new String(pc.getPassword()) );
      mUsername = nc.getName();
      mPassword = new String(pc.getPassword());
      Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {
        Node users = appSession.getRootNode().getNode( "default/Users" ), user;
        NodeIterator ni = users.getNodes();
        while( ni.hasNext() ) {
          user = ni.nextNode();
          if (user.getProperty( "jecars:UserId" ).getString().equals( nc.getName() )) {
            // **** User found
            PropertyIterator pi = user.getProperties();
            while( pi.hasNext() ) {
              Property p = pi.nextProperty();
//            System.out.println( "a---  " + p.getPath() + " = " + p.getValue().getString() );
              mLoginOk = true;
            }
//            CARS_Main main = factory.createMain( null, "" );
//            CARS_Path path = new CARS_Path( user.getProperty( "jecars:UserSource" ).getString() );
//            Node userSource = main.getUserSource( path.getChild() );
          }
        }
      }
    } catch ( PathNotFoundException pnfe ) {
      pnfe.printStackTrace();
    } catch ( RepositoryException re ) {
      re.printStackTrace();
    } catch ( UnsupportedCallbackException uce ) {
      uce.printStackTrace();
    } catch ( IOException ioe ) {
      ioe.printStackTrace();
    } catch ( Exception e) {
      e.printStackTrace();
    } finally {
    }
    return mLoginOk;
  }

  public boolean commit() throws LoginException {
    if (mLoginOk==false) return false;
    mUsernamePrincipal = new CARS_Principal( mUsername );
    mPassword = "na";
    mSubject.getPrincipals().add( mUsernamePrincipal );
    mSubject.getPublicCredentials().add( mPassword );
    mUsername = null;
    return true;
  }

  public boolean abort() throws LoginException {
    return false;
  }

  public boolean logout() throws LoginException {
    return false;
  }

    
}
