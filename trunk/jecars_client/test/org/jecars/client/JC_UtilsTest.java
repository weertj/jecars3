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
public class JC_UtilsTest {

    private JC_Clientable mClient_Admin = null;

    public JC_UtilsTest() {
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
      try {
        mClient_Admin = getClient();
        JC_GroupsNode groups = mClient_Admin.getGroupsNode();
        if (groups.hasGroup( "dataWriteGroup" )==false) {
          JC_GroupNode dwgn = groups.addGroup( "dataWriteGroup" );
          dwgn.save();
          JC_GroupNode drg = groups.getGroup( "DefaultReadGroup" );
          drg.addGroup( dwgn );
          drg.save();
          JC_UserNode  dwu  = mClient_Admin.getUsersNode().addUser( "dataWriter", "dataWriter", "dataWriter".toCharArray(), JC_PermissionNode.RS_READACCESS );
          dwu.save();
          dwgn.addUser( dwu );
          dwgn.save();
          JC_Nodeable dataNode = mClient_Admin.getNode( "/JeCARS/default/Data" );
          JC_PermissionNode perm = dataNode.addPermissionNode( "P_dataWriter", dwgn, JC_PermissionNode.RS_ALLRIGHTS );
          perm.save();
        }
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
      try {
        JC_Nodeable permNode = mClient_Admin.getNode( "/JeCARS/default/Data/P_dataWriter" );
        permNode.removeNode();
        permNode.save();
        JC_GroupsNode gn = mClient_Admin.getGroupsNode();
        gn.removeGroup( "dataWriteGroup" );
        JC_UsersNode un = mClient_Admin.getUsersNode();
        un.removeUser( "dataWriter" );
      } catch( Exception e ) {
      }
    }

    /**
     * Test of readAsString method, of class JC_Utils.
     */
    @Ignore( "Not ready to run" )
    @Test
    public void testReadAsString() throws Exception {
        System.out.println("readAsString");
        InputStream pInput = null;
        String expResult = "";
        String result = JC_Utils.readAsString(pInput);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createCommException method, of class JC_Utils.
     */
    @Ignore( "Not ready to run" )
    @Test
    public void testCreateCommException() {
        System.out.println("createCommException");
        JD_Taglist pTags = null;
        String pMessage = "";
        String pURL = "";
        JC_HttpException expResult = null;
        JC_HttpException result = JC_Utils.createCommException(pTags, pMessage, pURL);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of urlencode method, of class JC_Utils.
     */
    @Ignore( "Not ready to run" )
    @Test
    public void testUrlencode() {
        System.out.println("urlencode");
        String pURL = "";
        String expResult = "";
        String result = JC_Utils.urlencode(pURL);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of urldecode method, of class JC_Utils.
     */
    @Ignore( "Not ready to run" )
    @Test
    public void testUrldecode() {
        System.out.println("urldecode");
        String pURL = "";
        String expResult = "";
        String result = JC_Utils.urldecode(pURL);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFullNodeURL method, of class JC_Utils.
     */
    @Ignore( "Not ready to run" )
    @Test
    public void testGetFullNodeURL() throws Exception {
        System.out.println("getFullNodeURL");
        JC_Clientable pClient = null;
        JC_Nodeable pNode = null;
        StringBuilder expResult = null;
        StringBuilder result = JC_Utils.getFullNodeURL(pClient, pNode);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /** _readAllNodes
     *
     * @param pNode
     * @throws org.jecars.client.JC_Exception
     */
    private void _readAllNodes( JC_Nodeable pNode ) throws JC_Exception {
      Collection<JC_Nodeable> nodes = pNode.getNodes();
      for (JC_Nodeable node : nodes) {
        _readAllNodes( node );
      }
      return;
    }

    /** testReadPerformance
     *
     * Jackrabbit 1.4
     *
     * Clear accessmanager
     * time=1957
     * time=3314
     * time=2891
     * time=2434
     * time=3088
     *
     * Cached accessmanager
     * time=1649
     * time=1976
     * time=1955
     * time=2129
     * time=2256
     *
     * @throws org.jecars.client.JC_Exception
     */
    /*
    @Test
    public void testReadPerformance() throws JC_Exception {
      JC_Clientable c = JC_Factory.createClient( "http://localhost:8080/cars" );
      c.setCredentials( "UserManager", "jecars".toCharArray() );
      JC_Params p = c.createParams( JC_RESTComm.GET );
      p.setFilterEventTypes( "READ" );
      c.setDefaultParams( JC_RESTComm.GET, p );
      JC_Nodeable root = c.getRootNode();
      root = root.getNode( "JeCARS" );
      System.out.println("testReadPerformance start");
      long time = System.currentTimeMillis();
      _readAllNodes( root );
      System.out.println("time=" + (System.currentTimeMillis()-time));
      System.out.println("testReadPerformance end");
      return;
    }
     */

//    @Test
//    public void testCreateTextLoad() throws Exception {
//        String prefix = UUID.randomUUID().toString().substring(0, 5);
//        File f = new File("test/file.txt");
//        FileInputStream fis = new FileInputStream(f);
//        byte[] buffer = new byte[(int) f.length()];
//        fis.read(buffer);
//        JC_Clientable client = getClient();
//        client.setCredentials("dataWriter", "dataWriter".toCharArray());
//        JC_Nodeable dataNode = client.getNode("/JeCARS/default/Data");
//        for (int n = 0; n < 100; n++) {
//          String nodeName = prefix + "file_b" + n;
//          System.out.println( "create " + nodeName );
//          ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
//          JC_Nodeable newNode = dataNode.addNode(nodeName, "jecars:datafile");
////          newNode.setProperty( "jecars:Title", "testing 1.2.3....." );
//          JC_Streamable stream = JC_DefaultStream.createStream(bais, "text/plain");
//          stream.setContentLength(buffer.length);
//          newNode.setProperty(stream);
//          newNode.save();
//
//          client.createParams( JC_RESTComm.GET ).setIncludeBinary( true );
//          JC_Nodeable readNode = client.getNode( newNode.getPath() );
//          System.out.println("readNode = " + readNode.getPath() );
//          System.out.println("readData = " + readNode.getProperty("jcr:data").getValueString() );
//
//        }
//
//        // **** Remove all
//        JC_Filter filter = JC_Filter.createFilter();
//        filter.addCategory( "jecars:datafile" );
//        Collection<JC_Nodeable> nodes = dataNode.getNodes( null, filter, null );
//        for (JC_Nodeable node : nodes) {
//          System.out.println( "mark for delete " + node.getPath() );
//          node.removeNode();
//          node.save();
//        }
//        dataNode.save();
// 
//    }




}