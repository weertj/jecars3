/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.client.scripts;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Factory;

/**
 *
 * @author weert
 */
public class JCS_defaultScript {
  
  static final protected Logger LOG = Logger.getLogger( JCS_defaultScript.class.getName() );
  
  public String mJeCARSServer = "";
  public String mUsername = "";
  public String mPassword = "";

  public String mUserGroup       = "";
  public String mUser            = "";
  public String mUserPassword    = "";
  public String mGroup           = "";
  public String mFolder          = "";
  public String mPath            = "";
  public String mDescr           = "";
  public String mFile            = "";
  public String mName            = "";
  public String mTempDir         = "";
  public String mCodeBase        = "";
  public String mBoolOption      = "";
  public String mHelpOption      = "";
  public boolean mShowLogin      = false;

  public PrintStream mConfigOutput = System.out;
  public PrintStream mStdOutput    = System.out;
  public PrintStream mErrOutput    = System.err;

  /** JCS_defaultScript
   * 
   * @param args
   */
  public JCS_defaultScript() {
//    parseArguments( args );
    return;
  }

  /** getClient
   * 
   * @return
   * @throws java.lang.Exception
   */
  public JC_Clientable getClient() throws Exception {
    if (mConfigOutput!=null) {
      mConfigOutput.println( "Connect to: " + mJeCARSServer + " with " + mUsername + " (" + mPassword + ")" );
    }
    final JC_Clientable client = JC_Factory.createClient( mJeCARSServer );
    client.setCredentials( mUsername, mPassword.toCharArray() );
    return client;
  }
          
  /** parseArguments
   *
   * @param args
   */
  public void parseArguments( final String[] args ) {
    for (String arg : args) {
      if ("-h".equals( arg )) {
        mHelpOption = "-h";
      }
      if (arg.startsWith( "-s=" )) {
        mJeCARSServer = arg.substring( 3 );
      }
      if (arg.startsWith( "-u=" )) {
        mUsername = arg.substring( 3 );
      }
      if (arg.startsWith( "-p=" )) {
        mPassword = arg.substring( 3 );
      }
      if (arg.startsWith( "-usergroup=" )) {
        mUserGroup = arg.substring( 11 );
      }
      if (arg.startsWith( "-user=" )) {
        mUser = arg.substring( 6 );
      }
      if (arg.startsWith( "-userpassword=" )) {
        mUserPassword = arg.substring( 14 );
      }
      if (arg.startsWith( "-group=" )) {
        mGroup = arg.substring( 7 );
      }
      if (arg.startsWith( "-name=" )) {
        mName = arg.substring( 6 );
      }
      if (arg.startsWith( "-folder=" )) {
        mFolder = arg.substring( 8 );
      }
      if (arg.startsWith( "-path=" )) {
        mPath = arg.substring( 6 );
      }
      if (arg.startsWith( "-descr=" )) {
        mDescr = arg.substring( 7 );
      }
      if (arg.startsWith( "-file=" )) {
        mFile = arg.substring( 6 );
      }
      if (arg.startsWith( "-tempdir=" )) {
        mTempDir = arg.substring( 9 );
      }
      if (arg.startsWith( "-codebase=" )) {
        mCodeBase = arg.substring( 10 );
      }
      if (arg.startsWith( "-bo=" )) {
        mBoolOption = arg.substring( 4 );
      }
      if (arg.startsWith( "-showlogin=" )) {
        mShowLogin = Boolean.parseBoolean( arg.substring( 11 ) );
      }
    }
    return;
  }

  
}
