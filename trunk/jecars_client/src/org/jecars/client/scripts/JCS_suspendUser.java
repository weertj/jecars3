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

import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;

/**
 *
 * @author weert
 */
public class JCS_suspendUser extends JCS_defaultScript {

  public JCS_suspendUser( final String[] args ) {
    super();
    parseArguments( args );
  }

  /** create
   * 
   * 
   * @throws java.lang.Exception
   */
  public void suspend() throws Exception {
    final JC_Clientable client = getClient();
    final JC_Nodeable usersN  = client.getNode( "/JeCARS/default/Users" );
    final JC_UsersNode  users  = (JC_UsersNode)usersN.morphToNodeType();

    // **** Suspend/unsuspend user
    if (users.hasUser( mUser )) {
      final JC_UserNode user = users.getUser( mUser );
      user.setSuspended( Boolean.parseBoolean(mBoolOption) );
      user.save();
    } else {
      mErrOutput.println( "User: " + users.getPath() + "/" + mUser + " not found" );
    }
    return;
  }
  

}
