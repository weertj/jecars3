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
public class JC_DoSTest extends JC_ClientTarget {

    public JC_DoSTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws JC_Exception, FileNotFoundException, IOException {
      final JC_Clientable client = getClient();
      File f = new File("test/file.txt");
      FileInputStream fis = new FileInputStream(f);
      byte[] buffer = new byte[20000000];
      fis.read(buffer);
      JC_Nodeable dataNode = client.getNode("/JeCARS/default/Data");
      String nodeName = "file_bigfile";
      if (!dataNode.hasNode( nodeName )) {
        System.out.println( "create " + nodeName );
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        JC_Nodeable newNode = dataNode.addNode(nodeName, "jecars:datafile");
        JC_Streamable stream = JC_DefaultStream.createStream(bais, "text/plain");
        stream.setContentLength(buffer.length);
        newNode.setProperty(stream);
        newNode.save();
      }
      return;
    }

    @After
    public void tearDown() throws JC_Exception {
      final JC_Clientable client = getClient();
      JC_Nodeable dataNode = client.getNode("/JeCARS/default/Data");
      String nodeName = "file_bigfile";
      if (dataNode.hasNode( nodeName )) {
        dataNode.getNode( nodeName ).removeNode();
//        dataNode.save();
      }
      return;
    }

    @Test
    public void testDoS1() throws JC_Exception, IOException {

      final JC_Clientable client = getClient();
      JC_Streamable bigFileNode = client.getNodeAsStream("/JeCARS/default/Data/file_bigfile");
      bigFileNode.getStream();
      System.out.println("start reading big file");
      byte[] buffer = bigFileNode.readAsByteArray();
      System.out.println("READY reading big file " + buffer.length + " : " );
      return;
    }

    @Test
    public void testDoS2() throws JC_Exception, IOException, InterruptedException {
//      dosThread[] dost = new dosThread[10];
      final int runs = 2;
      for (int i = 0; i < runs; i++) {
        System.out.println("Start dos " + i );
        dosThread dt = new dosThread();
        Thread t = new Thread( dt );
        t.start();
     }
 
      int lastrun = -1;
      while( readyCount.intValue()<runs ) {
        if (lastrun==readyCount.intValue()) {
          System.out.println("Current reads ready = " + readyCount.intValue() );
          throw new InterruptedException( "readyCount != 0" );
        }
        lastrun = readyCount.intValue();
        Thread.sleep( 4000 );
        System.out.println("Current reads ready = " + readyCount.intValue() );
      }
      return;
    }

    static volatile private AtomicInteger readyCount = new AtomicInteger( 0 );

    static private class dosThread implements Runnable {

      public dosThread() {
      }

      @Override
      public void run() {
        try {
          final JC_Clientable client = getClient();
          JC_Streamable bigFileNode = client.getNodeAsStream("/JeCARS/default/Data/file_bigfile");
          bigFileNode.getStream();
          System.out.println("start reading big file");
          byte[] buffer = bigFileNode.readAsByteArray();
          System.out.println("READY reading big file " + buffer.length + " : " +  + readyCount.incrementAndGet() );
        } catch( Throwable e ) {
          e.printStackTrace();
        }
        return;
      }

    }


}