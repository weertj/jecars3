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

import java.util.Collection;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.nt.JC_UserNode;

/**
 *
 * @author weert
 */
public class JCS_listUsers extends JCS_defaultScript {

  public JCS_listUsers( String[] args ) {
    super();
    parseArguments( args );
  }

  /** create
   * 
   * 
   * @throws java.lang.Exception
   */
  public void list() throws Exception {
    final JC_Clientable client = getClient();
    final JC_Nodeable usersN  = client.getNode( "/JeCARS/default/Users" );

    JC_Filter filter = JC_Filter.createFilter();
    filter.addCategory( "User" );
    Collection<JC_Nodeable> users = usersN.getNodes( null, filter, null );
    for (JC_Nodeable user : users) {
      JC_UserNode un = (JC_UserNode)user;
      if (un.getSuspended()) {
        mStdOutput.println( "User: " + un.getName() + " (SUSPENDED)" );
      } else {
        mStdOutput.println( "User: " + un.getName()  );
      }
    }

    return;
  }
  

}
