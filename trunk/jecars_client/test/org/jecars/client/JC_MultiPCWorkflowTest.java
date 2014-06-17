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
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_WorkflowNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_ToolTest
 *
 * @version $Id: JC_ToolTest.java,v 1.9 2009/07/30 12:11:21 weertj Exp $
 */
public class JC_MultiPCWorkflowTest {

    private JC_Clientable mClient_Admin = null;

    public JC_MultiPCWorkflowTest() {
    }

    private JC_Clientable getClient() throws JC_Exception {
      return JC_ClientTarget.getClient();
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
        mClient_Admin = getClient();
        final JC_GroupsNode groups = mClient_Admin.getGroupsNode();
        if (!groups.hasGroup( "toolGroup" )) {
          final JC_GroupNode dwgn = mClient_Admin.getGroupsNode().addGroup( "toolGroup" );
          dwgn.save();
          final JC_GroupNode drg = groups.getGroup( "DefaultReadGroup" );
          drg.addGroup( dwgn );
          drg.save();
          final JC_UserNode  dwu  = mClient_Admin.getUsersNode().addUser( "toolRunner", "toolRunner", "toolRunner".toCharArray(), JC_PermissionNode.RS_READACCESS );
          dwu.save();
          dwgn.addUser( dwu );
          dwgn.save();
          final JC_Nodeable dataNode = mClient_Admin.getSingleNode( "/JeCARS/default/jecars:Tools" );
          final JC_PermissionNode perm = dataNode.addPermissionNode( "P_toolRunner", dwgn, JC_PermissionNode.RS_ALLRIGHTS );
          perm.save();
        }
        // **** Send version info
        mClient_Admin.sendVersionInfo( getClass().getCanonicalName(), "$Id: JC_Workflow1Test $" );
        
        
        final JC_Clientable client = getClient();
        client.setCredentials("toolRunner", "toolRunner".toCharArray());
        final JC_Nodeable toolsNode = client.getSingleNode("/JeCARS/default/jecars:Tools");
        final JC_UserNode user = (JC_UserNode)client.getUserNode().morphToNodeType();

        {
          // **** Refresh workflowSleep by XML
          if (toolsNode.hasNode( "workflowSleep" )) {
            toolsNode.getNode( "workflowSleep" ).removeNode();
            toolsNode.save();
          }        
          JC_Nodeable workflowXML = toolsNode.addNode( "workflowSleep", "jecars:CARS_Interface");
          workflowXML.setProperty("jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowsXMLInterfaceApp");
          workflowXML.save();
          File file = new File("test/sleepWorkflowTemplate.xml");
          FileInputStream fis = new FileInputStream(file);
          JC_Streamable stream = JC_DefaultStream.createStream(fis, "text/xml" ); 
          stream.setContentLength(file.length());
          workflowXML.setProperty(stream);
          workflowXML.save();
        }
        
        {
          // **** Refresh masterQ by XML
          if (toolsNode.hasNode( "masterQ" )) {
            toolsNode.getNode( "masterQ" ).removeNode();
            toolsNode.save();
          }        
          JC_Nodeable workflowXML = toolsNode.addNode( "masterQ", "jecars:CARS_Interface");
          workflowXML.setProperty("jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowsXMLInterfaceApp");
          workflowXML.save();
          File file = new File("test/Wfl_MasterQ.xml");
          FileInputStream fis = new FileInputStream(file);
          JC_Streamable stream = JC_DefaultStream.createStream(fis, "text/xml" ); 
          stream.setContentLength(file.length());
          workflowXML.setProperty(stream);
          workflowXML.save();
        }
        
        
      } catch( Exception e ) {
        e.printStackTrace();
        fail( e.getMessage() );
      }
    }
    
    @After
    public void tearDown() {
//    if (1==1) return;
      try {
        try {
          final JC_Clientable client = getClient();
          client.setCredentials("toolRunner", "toolRunner".toCharArray());
          JC_Nodeable permNode = client.getSingleNode( "/JeCARS/default/jecars:Tools/P_toolRunner" );
//          permNode.removeNode();
//          permNode.save();
        } catch( Exception e ) {};
//        JC_GroupsNode gn = mClient_Admin.getGroupsNode();
//        gn.removeGroup( "toolGroup" );
//        JC_UsersNode un = mClient_Admin.getUsersNode();
//        un.removeUser( "toolRunner" );
      } catch( Exception e ) {
      }
    }

 
    /** testSleepWorkflow
     *
     * @throws org.jecars.client.JC_Exception
     */
    @Test
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void testSleepWorkflow() throws JC_Exception, Exception, InterruptedException, UnsupportedEncodingException {

     
      
      final JC_Nodeable masterQtemplatesNode = getClient().getSingleNode("/JeCARS/default/jecars:Tools/masterQ/MasterQ");
      final JC_Nodeable templatesNode = getClient().getSingleNode("/JeCARS/default/jecars:Tools/workflowSleep/sleepWorkflowTemplate");
      final JC_Nodeable toolsNode  = getClient().getSingleNode("/JeCARS/default/jecars:Tools" );

      /*
    // ----  
    
      final String masterQInstance = "masterQInstance";
      if (toolsNode.hasNode( masterQInstance )) {
        toolsNode.getNode( masterQInstance ).removeNode();
        toolsNode.save();
      }

      // **** Run the master Q
      final JC_WorkflowNode masterQ = JC_WorkflowNode.createWorkflowInterface(
                toolsNode, masterQtemplatesNode.getPath(),
                masterQInstance, null, 0, "jecars:Workflow" );
      masterQ.start();

  // ------
              */
      
      final JC_WorkflowNode masterQ = (JC_WorkflowNode)toolsNode.getNode( "masterQInstance" ).morphToNodeType();
  
      
      final String testInstance = "testWorkflowInstance";
      if (toolsNode.hasNode( testInstance )) {
        toolsNode.getNode( testInstance ).removeNode();
        toolsNode.save();
      }

      // **** Run the tool
      final JC_WorkflowNode runWorkflow = JC_WorkflowNode.createWorkflowInterface(
                toolsNode, templatesNode.getPath(),
                testInstance, null, 0, "jecars:Workflow" );
      runWorkflow.addParameterData( "SleepTimeInSecs" ).addParameter( "2");

      
      JC_Nodeable newwf = masterQ.getNode( "runners/Main/context/Q/AddWorkflows" ).addNode( runWorkflow.getName(), "jecars:Workflow", runWorkflow.getPath() );
      newwf.save();
      System.out.println("New workflow: " + newwf.getName() );
        
      Thread.sleep( 1000 );
      JC_Nodeable runwf = masterQ.getNode( "runners/Main/context/Q/RunningWorkflows" ).addNode( newwf.getName(), "jecars:Workflow" );
    System.out.println("runwf " + runwf.getPath() );
      runwf.save();
      
      
      runWorkflow.start();
      Thread.sleep( 1000 );
      while( runWorkflow.isRunning() ) {
        Thread.sleep( 1000 );
      }

      
      return;
    }



}