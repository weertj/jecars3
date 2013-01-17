/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumSet;
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
public class JC_RangedData {
    
    public JC_RangedData() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testRunTool() throws Exception {
//      JC_Clientable client = JC_ClientTarget.getClient();
//      client.setCredentials("Administrator", "admin".toCharArray());
//      File f = new File("test/rangedfile.txt");
//      FileInputStream fis = new FileInputStream(f);
//      {
//        JC_Nodeable dataNode = client.getNode("/JeCARS/default/Data");
//        JC_Nodeable newNode = dataNode.getOrAddNode( f.getName(), "jecars:datafile");
//        JC_Streamable stream = JC_DefaultStream.createStream( fis, "text/plain");
//         stream.setContentLength( f.length() );
//        newNode.setProperty(stream);
//        newNode.save();
//        stream.destroy();
//      }
//
//      // **** Reading parts of the file
//      {
//        JC_Nodeable dataNode = client.getNode("/JeCARS/default/Data").getNode( f.getName() );
//        final JC_Streamable str = dataNode.getPropertyStream( "jcr:data", JC_StreamProp.FRAGMENTED, 100, 42 );
//        InputStream is = str.getStream();
////        byte[] data = new byte[42];
//        String data = JC_Utils.readAsString( is );
////        is.read( data, 0, data.length );
//        System.out.println("part is: " + new String(data) );
//        str.destroy();
//      }
      
      
      
      return;
    }

    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
