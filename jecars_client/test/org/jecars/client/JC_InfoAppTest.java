/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

import org.junit.Test;
import static org.junit.Assert.*;

/** JC_UtilsTest
 *
 * @version $Id: JC_UtilsTest.java,v 1.6 2009/07/22 09:02:16 weertj Exp $
 */
public class JC_InfoAppTest {

    public JC_InfoAppTest() {
      super();
    }

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
    }

    @Test
    public void testWhoAmI1() throws JC_Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      final JC_Nodeable whoAmI = c.getRootNode().getNode( "JeCARS/ApplicationSources/InfoApp/WhoAmI" );
      assertEquals( "UserManager", whoAmI.getProperty( "jecars:Username" ).getValueString() );
      return;
    }

    @Test
    public void testWhoAmI2() throws JC_Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      c.setCredentials( c.retrieveGDataAuth() );
      final JC_Nodeable whoAmI = c.getRootNode().getNode( "JeCARS/ApplicationSources/InfoApp/WhoAmI" );
      assertEquals( "UserManager", whoAmI.getProperty( "jecars:Username" ).getValueString() );
      return;
    }

    @Test
    public void testWhoAmI3() throws JC_Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      final JC_GDataAuth auth = c.retrieveGDataAuth();
      final JC_Clientable c2 = getClient();
      c2.setCredentials( JC_GDataAuth.create( auth.getAuth() ) );
      final JC_Nodeable whoAmI = c2.getRootNode().getNode( "JeCARS/ApplicationSources/InfoApp/WhoAmI" );
      assertEquals( "UserManager", whoAmI.getProperty( "jecars:Username" ).getValueString() );
      return;
    }

    @Test
    public void testWhoAmI4() throws JC_Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      final JC_GDataAuth auth = c.retrieveGDataAuth();
      final JC_Clientable c2 = getClient();
      c2.setCredentials( JC_GDataAuth.create( auth.getAuth() ) );
      c2.setCredentials( "UserManager", new char[0] );
      assertEquals( "/JeCARS/default/Users/UserManager", c2.getUserNode().getPath() );
      return;
    }

    /**
     * Test of whoAmI method, of class JC_InfoApp.
     */
    @Test
    public void testWhoAmI() throws Exception {
      final JC_Clientable c = getClient();
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      c.setCredentials( c.retrieveGDataAuth() );
      final JC_InfoApp info = new JC_InfoApp( c );
      assertEquals( "UserManager", info.whoAmI() );
    }


}