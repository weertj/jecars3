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
package org.jecars.backup;

import java.io.IOException;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;

/**
 * JB_Session
 * 
 * @version $Id: JB_Session.java,v 1.1 2007/10/02 13:03:48 weertj Exp $
 */
public class JB_Session {

  private Session mSession = null;
  
  private String mRepDirectory = "";
  private String mRepConfig    = "repository.xml";
  private String mUsername     = "backup";
  private String mPassword     = "backup";
  
  public void setRepDirectory( String pRep ) {
    mRepDirectory = pRep;
    return;
  }
  
  public void setRepConfig( String pConfig ) {
    mRepConfig = pConfig;
    return;
  }
  
  public Session getSession() throws IOException, LoginException, RepositoryException {
    if (mSession==null) {
      Repository rep = new TransientRepository( mRepConfig, mRepDirectory );
      mSession = rep.login( new SimpleCredentials( mUsername, mPassword.toCharArray() ) );
    }
    return mSession;
  }

    
}
