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
import static org.junit.Assert.*;
import org.junit.*;

/** test of org.jecars.tools.workflow.xml package, CARS_WorkflowXMLInterfaceApp, and alt=workflowXml
 * i.e. test downloading and uploading XML
 * 
 */
public class JC_WorkflowXmlTest {

    public JC_WorkflowXmlTest() {
    }


    JC_Clientable mClient = null;
    private JC_Clientable getClient() throws JC_Exception {
        if (mClient == null) {
            mClient = JC_ClientTarget.getClient();
        }
      return mClient;
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            deleteXmlTestNode(getClient());
            JC_Nodeable testXmlToplevelNode = getXmlTestNode(getClient());
            System.out.println("setup: using toplevel test node = "+testXmlToplevelNode.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            fail("setUp failed to create/get XML test node, reason:"+e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            deleteXmlTestNode(getClient());
            System.out.println("Successfully cleaned up XML test node");
        } catch (Exception e) {
            e.printStackTrace();
            fail("tearDown failed to tear down: delete xml test node, reason:"+e.getMessage());
        }
    }

    private static String XMLTESTNODEPARENTPATH="/JeCARS/default/jecars:Tools";
    private static String XMLTESTNODENAME="wfxmlTest";
    private static String XMLBINTESTNODENAME="wfxmlBinaryTest";
    /** return the XML test node - create it if not yet existing
     * 
     * @param client
     * @throws JC_Exception 
     */
    private static JC_Nodeable getXmlTestNode(JC_Clientable client) throws JC_Exception {
        JC_Nodeable parent = client.getNode(XMLTESTNODEPARENTPATH);
        JC_Nodeable result;
        if (parent.hasNode(XMLTESTNODENAME)) {
            result = parent.getNode(XMLTESTNODENAME);
        } else {
            result = parent.addNode(XMLTESTNODENAME, "jecars:CARS_Interface");
            result.setProperty("jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowsXMLInterfaceApp");
            parent.save();
            System.out.println("Added "+result.getPath());
            JC_Nodeable binDataNode = addBinaryTestDatafile(parent, XMLBINTESTNODENAME, "This cat should not be here, he should not be about! He should not be here when your mother is out!");            
            System.out.println("Added "+binDataNode.getPath());
        }
        return result;
    }
    
    /** create parent.name jecars:data file with contents asciicontents */
    private static JC_Nodeable addBinaryTestDatafile(JC_Nodeable parent, String name, String asciicontents) throws JC_Exception {
        JC_Nodeable result = parent.addNode(name, "jecars:datafile");
        // upload the input
        ByteArrayInputStream bais = new ByteArrayInputStream(asciicontents.getBytes());
        JC_Streamable stream = JC_DefaultStream.createStream(bais, "text/plain");
        stream.setContentLength(asciicontents.length());
        result.setProperty(stream);
        result.save();
        return result;
    }
    
    /** remove the XML test node
     * 
     * @param client
     * @throws JC_Exception 
     */
    private static void deleteXmlTestNode(JC_Clientable client) throws JC_Exception {
        JC_Nodeable parent = client.getNode(XMLTESTNODEPARENTPATH);
        if (parent.hasNode(XMLTESTNODENAME)) {
            parent.removeChildNode(XMLTESTNODENAME);
            parent.save();
            System.out.println("Removed "+XMLTESTNODENAME+" from parent="+parent.getPath());
        }
        if (parent.hasNode(XMLBINTESTNODENAME)) {
            parent.removeChildNode(XMLBINTESTNODENAME);
            parent.save();            
            System.out.println("Removed "+XMLBINTESTNODENAME+" from parent="+parent.getPath());
        }
    }
    
    /** test reading XML (i.e. read XML and create JeCARS nodes)
     * and writing XML (i.e. from JeCARS to XML),
     * @throws Exception 
     */
    @Test 
    public void testXmlReaderAndWriter() throws Exception {
        System.out.println("============ testXmlReaderAndWriter ===========");
        System.out.println("---- Step 1: creating JeCARS nodes from XML ----");
        JC_Clientable client = getClient();
        JC_Nodeable xmlTestNode = getXmlTestNode(client);
        
        // the input file
        File file = new File("test/input4JC_WorkflowXmlTest.xml");
        assertTrue(file.canRead());
        System.out.println("Using XML input file="+file.getAbsolutePath());
        FileInputStream fis = new FileInputStream(file);
        String contentType = "application/xml"; 
        
        // upload the input file
        JC_Streamable stream = JC_DefaultStream.createStream(fis, contentType); 
        stream.setContentLength(file.length());
        xmlTestNode.setProperty(stream);
        xmlTestNode.save();
        
        // xmlTestNode should have at least 1 child now ...
        System.out.println("uploaded input to "+xmlTestNode.getPath());
        assertTrue(xmlTestNode.getPath()+" should have at least one child node now, but found nodeList==empty.", !xmlTestNode.getNodeList().isEmpty());
        
        System.out.println("---- Step 2: getting XML from created JeCARS nodes  ----");
        JC_Params params = client.createParams( JC_RESTComm.GET ).cloneParams();
        params.setOutputFormat( "wfxml" );
        JC_Streamable xmloutputStream = client.getNodeAsStream( 
                 getXmlTestNode(client).getPath(),
                 params, 
                 null, null );
         String result = JC_Utils.readAsString(xmloutputStream.getStream());
         System.out.println("result="+result);
    }
}