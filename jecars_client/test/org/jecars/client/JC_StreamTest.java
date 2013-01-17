/*
 * Copyright 2009 NLR - National Aerospace Laboratory
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import nl.msd.jdots.JD_Taglist;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_UtilsTest
 *
 * @version $Id: JC_UtilsTest.java,v 1.6 2009/07/22 09:02:16 weertj Exp $
 */
public class JC_StreamTest {

    private JC_Clientable mClient_Admin = null;

    public JC_StreamTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readAsString method, of class JC_Utils.
     */
    @Test
    public void testReadAsStream() throws Exception {
      JC_Clientable c = getClient();       
      final JC_Streamable toolResultStream = c.getNodeAsStream( "/JeCARS/default/jecars:Tools/remoteTestToolWithOutputAsLink/remoteTestToolWithOutputAsLink.out.txt" );
      String o = JC_Utils.readAsString(toolResultStream.getStream());
      System.out.println(o);
    }



}