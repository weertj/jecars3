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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import org.jecars.client.nt.JC_PrefsNode;
import org.jecars.client.nt.JC_UserNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_DefaultNodeTest
 *
 * @version $Id: JC_DefaultNodeTest.java,v 1.14 2009/03/30 14:09:37 weertj Exp $
 */
public class JC_DefaultNodeTest {

    private JC_Clientable mClient_Admin = null;
    
    public JC_DefaultNodeTest() {
    }


    @Before
    public void setUp() {
      try {
        mClient_Admin = JC_ClientTarget.getClient();
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }


    /**
     * Test of setProperty method, of class JC_DefaultNode.
     */
    @Test
    public void testSetProperty_JC_Streamable() throws JC_Exception, FileNotFoundException, IOException {
      final JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Data" );
      if (node.hasNode( "testSetProperty_JC_Streamable" )) {
        node.getNode( "testSetProperty_JC_Streamable" ).removeNode();
        node.save();
      }
      final JC_Nodeable newNode = node.addNode( "testSetProperty_JC_Streamable", "jecars:datafile" );
//      node.save();
      final File f = new File( "test/testSetProperty_JC_Streamable.txt" );
      final JC_Streamable stream = JC_DefaultStream.createStream( f, "text/plain" );
      newNode.setProperty( stream );
      newNode.save();
      return;
    }



    @Test
    public void testSetProperty_String_long() throws Exception {
        JC_Nodeable root = mClient_Admin.getRootNode();
        root = root.getNode( "JeCARS/default/Data" );
        if (root.hasNode("testSameNodeNamesLongPropertiesTest")) {
            System.out.println("removing testSameNodeNamesLongPropertiesTest");
            root.getNode("testSameNodeNamesLongPropertiesTest").removeNode();
            root.save();
        }

        JC_Nodeable testnode = root.addNode("testSameNodeNamesLongPropertiesTest", "jecars:unstructured");
        testnode.save();

        JC_Nodeable test1 = testnode.addNode("abc", "jecars:unstructured");
        test1.setProperty("jecars:mdescr","test1");
        test1.setProperty("jecars:columnLen", 123);
        test1.save();

        JC_Nodeable test2 = testnode.addNode("abc", "jecars:unstructured");
        test2.setProperty("jecars:mdescr","test2");
        test2.setProperty("jecars:columnLen", 456);
        test2.save();

        test1.refresh();
        assertEquals( 123L, test1.getProperty( "jecars:columnLen").getValueAsLong() );
        test2.refresh();
        assertEquals( 456L, test2.getProperty( "jecars:columnLen").getValueAsLong() );

        for (JC_Nodeable result : testnode.getNodes()) {
            System.out.println("result "+ result.getPath()+
                    "  mdescr="+result.getProperty("jecars:mdescr").getValue()+
                    "  columnLen="+result.getProperty("jecars:columnLen").getValue());
        }
        return;
    }




    /**
     * Test of setProperty method, of class JC_DefaultNode.
     */
    @Test
    public void testSetProperty_String_String() throws Exception {
      System.out.println("setProperty");
      String pName = "jecars:Body";
      String pValue = "blabla";
      JC_Nodeable df = mClient_Admin.getNode( "/JeCARS/default/Data" );
      JC_Propertyable result = df.setProperty(pName, pValue);
      result.save();
      result = df.setProperty( "jecars:Title",  "type ");
      result.save();
      return;
    }

    /**
     * Test of setProperty method, of class JC_DefaultNode.
     */
    @Test
    public void testSetProperty_String_Boolean() throws Exception {
      JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Data" );
      if (node.hasNode( "testSetProperty_Boolean" )==true) {
        node.getNode( "testSetProperty_Boolean" ).removeNode();
        node.save();
      }
      JC_Nodeable newNode = node.addNode( "testSetProperty_Boolean", "jecars:unstructured" );
      newNode.setProperty( "jecars:testBoolean1", true );
      newNode.setProperty( "jecars:testBoolean2", false );
      newNode.save();
      newNode.setProperty( "jecars:testBoolean3", true );
      newNode.setProperty( "jecars:testBoolean4", false );
      newNode.save();
      newNode.refresh();
      assertTrue(  (Boolean)newNode.getProperty( "jecars:testBoolean1" ).getValueAs( Boolean.class ) );
      assertFalse( (Boolean)newNode.getProperty( "jecars:testBoolean2" ).getValueAs( Boolean.class ) );
      assertTrue(  (Boolean)newNode.getProperty( "jecars:testBoolean3" ).getValueAs( Boolean.class ) );
      assertFalse( (Boolean)newNode.getProperty( "jecars:testBoolean4" ).getValueAs( Boolean.class ) );
      return;
    }


    /**
     * Test of setMultiValueProperty method, of class JC_DefaultNode.
     */
    @Test
    public void testSetMultiValueProperty() throws JC_Exception {
      JC_Nodeable df = mClient_Admin.getNode( "/JeCARS/default/Data" );
      mClient_Admin.setCredentials( mClient_Admin.retrieveGDataAuth() );
      if (df.hasNode( "testSetMultiValueProperty" )==true) {
        df.getNode( "testSetMultiValueProperty" ).removeNode();
        df.save();
      }
      JC_Nodeable smp = df.addNode( "testSetMultiValueProperty", "jecars:TestNode" );
      df.save();
      Collection<String> longs = new ArrayList<String>();
      longs.add( "1234" );
      longs.add( "5678" );
      longs.add( "90" );
      smp.setMultiValueProperty( "jecars:Longs", longs );
      Collection<String> doubles = new ArrayList<String>();
      doubles.add( "12.34" );
      doubles.add( "56.78" );
      doubles.add( "9.0" );
      smp.setMultiValueProperty( "jecars:Doubles", doubles );
      smp.save();
      Collection<Boolean> bools = new ArrayList<Boolean>();
      bools.add( true );
      bools.add( false );
      bools.add( true );
      smp.setMultiValuePropertyB( "jecars:Booleans", bools );
      smp.save();
      Collection<Double> ds = new ArrayList<Double>();
      ds.add( 212.34 );
      ds.add( 256.78 );
      ds.add( 29.0 );
      smp.setMultiValuePropertyD( "jecars:Doubles", ds );
      Collection<Long> ls = new ArrayList<Long>();
      ls.add( 21234L );
      ls.add( 25678L );
      ls.add( 290L );
      smp.setMultiValuePropertyL( "jecars:Longs", ls );
      Collection<Boolean> bs = new ArrayList<Boolean>();
      bs.add( false );
      bs.add( false );
      bs.add( true );
      smp.setMultiValuePropertyB( "jecars:Booleans", bs );
      smp.save();
      JC_MultiValueProperty mvp = (JC_MultiValueProperty)smp.getProperty( "jecars:Longs" );
      mvp.removeValue( "1234" );
      mvp.save();
      smp.refresh();
      mvp = (JC_MultiValueProperty)smp.getProperty( "jecars:Longs" );
      assertEquals( 5, mvp.getSize() );
      mvp = (JC_MultiValueProperty)smp.getProperty( "jecars:Longs" );
      mvp.removeAllValues();
      mvp.save();
      smp.refresh();
      assertFalse( smp.hasProperty( "jecars.Longs" ));

//      df.getNode( "testSetMultiValueProperty" ).removeNode();
//      df.save();
      return;
    }


    /**
     * Test of getChildNodeDefs method, of class JC_DefaultNode.
     */
    @Test
    public void testGetChildNodeDefs() throws Exception {
      System.out.println("getChildNodeDefs");
      JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default" );
      Collection<String> result = node.getChildNodeDefs();
      assertTrue( result.size()==8 );
      assertTrue( result.contains( "jecars:Permission" ));
      assertTrue( result.contains( "jecars:Queries" ));
      assertTrue( result.contains( "jecars:EventsFolder" ));
      assertTrue( result.contains( "jecars:datafolder" ));
      assertTrue( result.contains( "jecars:Groups" ));
      assertTrue( result.contains( "jecars:Users" ));
      assertTrue( result.contains( "jecars:permissionable" ));
      assertTrue( result.contains( "jecars:datafolder" ));
      return;
    }

    /**
     * Test of getUpdateAsHead method, of class JC_DefaultNode.
     */
    @Test
    public void testGetUpdateAsHead() throws JC_Exception {
      System.out.println("getUpdateAsHead");
      final JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Users/Administrator" );
      boolean update = node.getUpdateAsHead();
      assertFalse( update );
      node.setProperty( "jecars:Fullname", "Administrator " + System.currentTimeMillis() );
      node.save();              
      update = node.getUpdateAsHead();
      assertTrue( update );
      update = node.getUpdateAsHead();
      assertFalse( update );
      return;
    }

    /**
     * Test of testExportNode_Boolean method, of class JC_DefaultNode.
     */
    @Ignore("Do not export" )
    @Test
    public void testExportNode_Boolean() throws JC_Exception, FileNotFoundException, IOException {      
      System.out.println("exportNode_Boolean");
      JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Events/System/jecars:EventsREAD" );
      File f = new File( "test/testExportNode_OutputStream_Boolean.txt" );
      FileOutputStream fos = new FileOutputStream(f);
      JC_Streamable stream = node.exportNode( true );
      String result = JC_Utils.readAsString( stream.getStream() );
      fos.write( result.getBytes() );
      fos.close();
      stream.destroy();
      return;
    }
     
    /**
     * Test of importNode_InputStream method, of class JC_DefaultNode.
     */
    @Ignore("TODO do not restore event types")
    @Test
    public void testImportNode_InputStream() throws JC_Exception, FileNotFoundException, IOException {
      System.out.println("importNode_InputStream");
      JC_Nodeable nodeR = mClient_Admin.getNode( "/JeCARS/default/Events/System/jecars:EventsREAD" );
      Collection<JC_Nodeable>nodes = nodeR.getNodes();
      for (JC_Nodeable nodeRR : nodes) {
        nodeRR.removeNode();
      }
      nodeR.save();
      JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Events/System/jecars:EventsREAD" );
      File f = new File( "test/testEventsREADImport.txt" );     
      JC_Streamable stream = JC_DefaultStream.createStream( f, "text/plain" );
      node.importNode( stream );
      stream.destroy();
      return;
    }

    /** testSerializableNode
     *
     * @throws org.jecars.client.JC_Exception
     * @throws java.io.IOException
     */
    @Ignore( "no testSerializableNode")
    @Test
    public void testSerializableNode() throws JC_Exception, IOException {
      System.out.println( "testSerializableNode" );
      JC_Nodeable node = mClient_Admin.getNode( "/JeCARS/default/Users/Administrator" );
      ObjectOutputStream oos = new ObjectOutputStream( System.out );
      oos.writeObject( node );
      oos.close();
      return;
    }

    /** testGetRights
     *
     * @throws org.jecars.client.JC_Exception
     */
    @Test
    public void testGetRights() throws JC_Exception {
      System.out.println( "testGetRights" );
      JC_Rights r = mClient_Admin.getRights( "/JeCARS/default" );
      assertTrue( r.hasRight( JC_Rights.R_REMOVE ) );

      final JC_Nodeable n = mClient_Admin.getNode( "/JeCARS/default/Users/Administrator" );
      r = n.getRights( "/JeCARS/default/Users/Administrator" );
      assertTrue( r.hasRight( JC_Rights.R_REMOVE ) );
      assertTrue( r.hasRight( JC_Rights.R_ADDNODE ) );
      assertTrue( r.hasRight( JC_Rights.R_SETPROPERTY ) );
      return;
    }

    @Test
    public void testPrefs() throws JC_Exception {
      JC_UserNode user = (JC_UserNode)mClient_Admin.getUserNode().morphToNodeType();
      JC_PrefsNode prefs = user.getPrefsNode();
      prefs.setPrefValue( "jecars/test/testPrefs", "jecars:test1", "testValue" );
      prefs.save();
      user.refresh();
      prefs = user.getPrefsNode();
      String result = prefs.getPrefValue( "jecars/test/testPrefs", "jecars:test1", true );
      assertEquals( result, "testValue" );
      return;
    }

    @Test
    public void testPrefs2() throws JC_Exception {
      final JC_UserNode user = (JC_UserNode)mClient_Admin.getUserNode().morphToNodeType();
      JC_PrefsNode prefs = user.getPrefsNode();
      prefs.setPrefValue( "jecars/test/testPrefs", "jecars:test2", 1234.5678 );
      prefs.setPrefValue( "jecars/test/testPrefs", "test2", 5678 );
      prefs.save();
      user.refresh();
      prefs = user.getPrefsNode();
      final double result = prefs.getPrefValueDouble( "jecars/test/testPrefs", "jecars:test2", true );
      assertEquals( result, 1234.5678, 0.0 );
      return;
    }

    @Test
    public void testMoveNode() throws JC_Exception {
      final JC_Nodeable n = mClient_Admin.getNode( "/JeCARS/default/Data" );
      JC_Nodeable newn = null;
      if (n.hasNode( "testMoveNodeTest_moved" )) {
        n.getNode( "testMoveNodeTest_moved" ).removeNode();
        n.save();
      }
      if (!n.hasNode( "testMoveNodeTest" )) {
        newn = n.addNode( "testMoveNodeTest", "jecars:datafile" );
      } else {
        newn = n.getNode( "testMoveNodeTest" );
      }
      newn.save();
      newn.moveNode( "testMoveNodeTest_moved" );
      assertTrue( n.hasNode( "testMoveNodeTest_moved" ) );
      assertFalse( n.hasNode( "testMoveNodeTest" ) );
      n.refresh();
      assertTrue( n.hasNode( "testMoveNodeTest_moved" ) );
      assertFalse( n.hasNode( "testMoveNodeTest" ) );
      return;
    }

    @Test
    public void expireNodeTest() throws JC_Exception, InterruptedException {
      final JC_Nodeable n = mClient_Admin.getNode( "/JeCARS/default/Data" );
      if (n.hasNode( "testexpireNodeTest1" )) {
        n.getNode( "testexpireNodeTest1" ).removeNode();
        n.save();
      }
      if (n.hasNode( "testexpireNodeTest2" )) {
        n.getNode( "testexpireNodeTest2" ).removeNode();
        n.save();
      }
      if (n.hasNode( "testexpireNodeTest3" )) {
        n.getNode( "testexpireNodeTest3" ).removeNode();
        n.save();
      }
      if (n.hasNode( "testexpireNodeTest4" )) {
        n.getNode( "testexpireNodeTest4" ).removeNode();
        n.save();
      }
      if (n.hasNode( "testexpireNodeTest5" )) {
        n.getNode( "testexpireNodeTest5" ).removeNode();
        n.save();
      }
      if (n.hasNode( "testexpireNodeTest6" )) {
        n.getNode( "testexpireNodeTest6" ).removeNode();
        n.save();
      }
      JC_Nodeable newn = n.addNode( "testexpireNodeTest1", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      newn = n.addNode( "testexpireNodeTest2", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      newn = n.addNode( "testexpireNodeTest3", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      newn = n.addNode( "testexpireNodeTest4", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      newn = n.addNode( "testexpireNodeTest5", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      newn = n.addNode( "testexpireNodeTest6", "jecars:datafile" );
      newn.setExpireDate( 1 );
      newn.save();
      System.out.println("waiting for expired node");
      Thread.sleep( 2* 60 * 1000 );
      n.refresh();
      assertFalse( n.hasNode( "testexpireNodeTest2" ) );
      return;
    }


}