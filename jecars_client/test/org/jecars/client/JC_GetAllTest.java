/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author weert
 */
public class JC_GetAllTest extends JC_ClientTarget {

    public JC_GetAllTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws JC_Exception, FileNotFoundException, IOException {
      return;
    }

    @After
    public void tearDown() throws JC_Exception {
      return;
    }

    @Test
    public void testGetAll1() throws Exception {

      final JC_Clientable client = getClient();
//        JC_Nodeable rootNode = client.getSingleNode("/JeCARS/default");
    for( int i=0; i<100; i++ ) {
      
      JC_Nodeable rootNode = client.getSingleNode("/");
      JC_Params p = client.createParams( JC_RESTComm.GET ).cloneParams();
      p.setDeep( true );
      p.setAllProperties( true );
//      p.setOutputFormat( "properties" );
//        p.setOutputFormat("properties");
      Collection<JC_Nodeable> nodes = rootNode.getNodes( p, null, null );
        System.out.println("nodes = " + nodes.size());
      for( JC_Nodeable node : nodes ) {
//        System.out.println("node = " + node.getPath() );
      }
        System.out.println("leodol " + i);
    } 
      return;
    }



}