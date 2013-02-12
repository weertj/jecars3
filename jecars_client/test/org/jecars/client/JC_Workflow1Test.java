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

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jecars.client.nt.EJC_ContextParameter;
import org.jecars.client.nt.EJC_TaskModifier;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_ParameterDataNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_ToolEventNode;
import org.jecars.client.nt.JC_ToolNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.jecars.client.nt.JC_WorkflowNode;
import org.jecars.client.nt.JC_WorkflowTaskNode;
import org.jecars.client.nt.JC_WorkflowTaskPortNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_ToolTest
 *
 * @version $Id: JC_ToolTest.java,v 1.9 2009/07/30 12:11:21 weertj Exp $
 */
public class JC_Workflow1Test {

    private JC_Clientable mClient_Admin = null;

    public JC_Workflow1Test() {
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

        String templateName = "jecarsTest1WorkflowTemplate";
        if (toolsNode.hasNode( templateName )) {
          // **** Remove template tool
          toolsNode.getNode( templateName ).removeNode();
          toolsNode.save();
        }

        // *****************************
        // **** Create template workflow (1)
        JC_WorkflowNode templateWorkflow = JC_WorkflowNode.createTemplateWorkflow( toolsNode, templateName, "org.jecars.tools.CARS_DefaultWorkflow", true );
        // **** Don't expire the workflow
        JC_Nodeable config = templateWorkflow.addConfig();
        config.setProperty( "jecars:RunningExpireMinutes", JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
        config.setProperty( "jecars:ClosedExpireMinutes",  JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
        templateWorkflow.save();
        JC_WorkflowTaskNode entrytask = templateWorkflow.addStart( "Entry-1" );
        entrytask.setExpireContextAfterMinutes( 15 );
        JC_WorkflowTaskNode exittask = templateWorkflow.addEnd( "Exit-1" );
        entrytask.setExpireContextAfterMinutes( 15 );
        templateWorkflow.save();
        JC_WorkflowTaskPortNode tentryout = entrytask.addOutputPort( "Entry", null, ".*", ".*" );
        JC_WorkflowTaskPortNode texitout  = exittask.addInputPort(  "Exit", null, ".*", ".*" );
        templateWorkflow.save();
        entrytask.addParameterData( EJC_ContextParameter.WFP_OUTPUT_FOLDER ).addParameter( "testoutput" );
        
        // ****************************************************
        // **** Sleep
        JC_WorkflowTaskNode sleep0 = templateWorkflow.addJavaTask( "Sleep0", "org.jecars.wfplugin.tools.WFPT_Sleep" );
        templateWorkflow.save();
        JC_ParameterDataNode pdn = sleep0.addParameterData( "SleepTimeInSecs" );
        pdn.addParameter( "3" );
        JC_WorkflowTaskPortNode sleep_in0  = sleep0.addInputPort(  "In",  null, ".*", ".*" );
        JC_WorkflowTaskPortNode sleep_out0 = sleep0.addOutputPort( "Out", null, ".*", ".*" );
        templateWorkflow.save();        
        JC_WorkflowTaskNode sleep1 = templateWorkflow.addJavaTask( "Sleep1", "org.jecars.wfplugin.tools.WFPT_Sleep" );
        templateWorkflow.save();
        pdn = sleep1.addParameterData( "SleepTimeInSecs" );
        pdn.addParameter( "3" );
        JC_WorkflowTaskPortNode sleep_in1  = sleep1.addInputPort(  "In",  null, ".*", ".*" );
        JC_WorkflowTaskPortNode sleep_out1 = sleep1.addOutputPort( "Out", null, ".*", ".*" );
        templateWorkflow.save();        
        templateWorkflow.addLink( "ToSleep0",   entrytask, tentryout, sleep0, sleep_in0, 0 );
        templateWorkflow.addLink( "FromSleep0", sleep0, sleep_out0, sleep1, sleep_in1, 0 );        
        templateWorkflow.addLink( "FromSleep1", sleep1, sleep_in1, exittask, texitout, 0 );        

        
        templateName = "jecarsTest2WorkflowTemplate";
        if (toolsNode.hasNode( templateName )) {
          // **** Remove template tool
          toolsNode.getNode( templateName ).removeNode();
          toolsNode.save();
        }
        
        // *********************************************
        // **** Create template workflow (External tool)
        templateWorkflow = JC_WorkflowNode.createTemplateWorkflow( toolsNode, templateName, "org.jecars.tools.CARS_DefaultWorkflow", true );
        // **** Don't expire the workflow
        config = templateWorkflow.addConfig();
        config.setProperty( "jecars:RunningExpireMinutes", JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
        config.setProperty( "jecars:ClosedExpireMinutes",  JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
        templateWorkflow.save();
        entrytask = templateWorkflow.addStart( "Entry-1" );
        entrytask.setExpireContextAfterMinutes( 15 );
        exittask = templateWorkflow.addEnd( "Exit-1" );
        entrytask.setExpireContextAfterMinutes( 15 );
        templateWorkflow.save();
        tentryout = entrytask.addOutputPort( "Entry", null, ".*", ".*" );
        texitout  = exittask.addInputPort(  "Exit", null, ".*", ".*" );
        templateWorkflow.save();
        
        // ****************************************************
        // **** Sleep
        sleep0 = templateWorkflow.addJavaTask( "Sleep0", "org.jecars.wfplugin.tools.WFPT_Sleep" );
        templateWorkflow.save();
        pdn = sleep0.addParameterData( "SleepTimeInSecs" );
        pdn.addParameter( "3" );
        sleep_in0  = sleep0.addInputPort(  "In",  null, ".*", ".*" );
        sleep_out0 = sleep0.addOutputPort( "Out", null, ".*", ".*" );
        templateWorkflow.save();
        
        templateName = "jecarsExternalTool1";
        if (toolsNode.hasNode( templateName )) {
          // **** Remove template tool
          toolsNode.getNode( templateName ).removeNode();
          toolsNode.save();
        }
        JC_ToolNode tool = JC_ToolNode.createTemplateTool( toolsNode, templateName, "org.jecars.tools.CARS_ExternalTool", true );
        tool.addConfigExternalTool(
                        "DirectoryDoesNotExist",
                        "ToolDoesNotExist.exe",
                        true );
        JC_WorkflowTaskNode etool = templateWorkflow.addTask( "ExternalTool1" );        
        etool.setTool( tool );
//        etool.addModifier( EJC_TaskModifier.ALLOWERROR );
        etool.save();
        templateWorkflow.save();
        JC_WorkflowTaskPortNode etool_in1  = etool.addInputPort(  "In",  null, ".*", ".*" );
        JC_WorkflowTaskPortNode etool_out1 = etool.addOutputPort( "Out", null, ".*", ".*" );
        templateWorkflow.save();        
        templateWorkflow.addLink( "ToSleep0",   entrytask, tentryout, sleep0, sleep_in0, 0 );
        templateWorkflow.addLink( "FromSleep0", sleep0, sleep_out0, etool, etool_in1, 0 );        
        templateWorkflow.addLink( "FromSleep1", etool, etool_out1, exittask, texitout, 0 );
        
        
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
          JC_Nodeable permNode = mClient_Admin.getSingleNode( "/JeCARS/default/jecars:Tools/P_toolRunner" );
          permNode.removeNode();
          permNode.save();
        } catch( Exception e ) {};
        JC_GroupsNode gn = mClient_Admin.getGroupsNode();
        gn.removeGroup( "toolGroup" );
        JC_UsersNode un = mClient_Admin.getUsersNode();
        un.removeUser( "toolRunner" );
      } catch( Exception e ) {
      }
    }

 
    /** testSleepTool
     *
     * @throws org.jecars.client.JC_Exception
     */
//    @Test
    public void testSleepTool() throws JC_Exception, Exception, InterruptedException, UnsupportedEncodingException {
      
      final JC_Nodeable templatesNode = getClient().getSingleNode("/JeCARS/default/jecars:Tools");
      final JC_Nodeable toolsNode  = getClient().getSingleNode("/JeCARS/default/jecars:Tools" );
      final String testInstance = "testWorkflowInstance";
      if (toolsNode.hasNode( testInstance )) {
        toolsNode.getNode( testInstance ).removeNode();
        toolsNode.save();
      }
      // **** Run the tool
      final JC_WorkflowNode runWorkflow = JC_WorkflowNode.createWorkflowInterface(
                toolsNode, templatesNode.getNode( "jecarsTest1WorkflowTemplate" ).morphToNodeType().getPath(),
                testInstance, null, 0, "jecars:Workflow" );
      for( int i = 0; i<10; i++ ) {
        runWorkflow.addInput( "rangedfile_" + i + ".txt", "jecars:inputresource", "text/plain", new FileInputStream("test/rangedfile.txt") );
      }
      runWorkflow.start();
      while( !runWorkflow.isRunning() ) {
        Thread.sleep( 1000 );
      }
      while( runWorkflow.isRunning() ) {
        Thread.sleep( 1000 );
        List<JC_ToolEventNode> events = runWorkflow.getToolEvents();
        for (int eventNo = 0; eventNo < events.size(); eventNo++) {
           JC_ToolEventNode event = events.get(eventNo);
           System.out.println("--- " + event.getPath() );
        }

      }
      
      return;
    }


    @Test
    public void testExternal1Tool() throws JC_Exception, Exception, InterruptedException, UnsupportedEncodingException {
      
      final JC_Nodeable templatesNode = getClient().getSingleNode("/JeCARS/default/jecars:Tools");
      final JC_Nodeable toolsNode  = getClient().getSingleNode("/JeCARS/default/jecars:Tools" );
      final String testInstance = "jecarsTest2WorkflowInstance";
      if (toolsNode.hasNode( testInstance )) {
        toolsNode.getNode( testInstance ).removeNode();
        toolsNode.save();
      }
      // **** Run the tool
      final JC_WorkflowNode runWorkflow = JC_WorkflowNode.createWorkflowInterface(
                toolsNode, templatesNode.getNode( "jecarsTest2WorkflowTemplate" ).morphToNodeType().getPath(),
                testInstance, null, 0, "jecars:Workflow" );
      for( int i = 0; i<10; i++ ) {
        runWorkflow.addInput( "rangedfile_" + i + ".txt", "jecars:inputresource", "text/plain", new FileInputStream("test/rangedfile.txt") );
      }
      runWorkflow.start();
      while( !runWorkflow.isRunning() ) {
        Thread.sleep( 1000 );
      }
      while( runWorkflow.isRunning() ) {
        Thread.sleep( 1000 );
        List<JC_ToolEventNode> events = runWorkflow.getToolEvents();
        for (int eventNo = 0; eventNo < events.size(); eventNo++) {
           JC_ToolEventNode event = events.get(eventNo);
           System.out.println("--- " + event.getPath() );
        }

      }
      
      return;
    }


}