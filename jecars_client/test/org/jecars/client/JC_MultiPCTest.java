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

import java.io.IOException;
import java.util.List;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_ParameterDataNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_ToolNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
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
public class JC_MultiPCTest {

    private JC_Clientable mClient_Admin = null;

    public JC_MultiPCTest() {
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
          final JC_Nodeable dataNode = mClient_Admin.getNode( "/JeCARS/default/jecars:Tools" );
          final JC_PermissionNode perm = dataNode.addPermissionNode( "P_toolRunner", dwgn, JC_PermissionNode.RS_ALLRIGHTS );
          perm.save();
        }
        // **** Send version info
        mClient_Admin.sendVersionInfo( getClass().getCanonicalName(), "$Id: JC_ToolTest.java,v 1.9 2009/07/30 12:11:21 weertj Exp $" );
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
//    if (1==1) return;
      try {
        try {
          JC_Nodeable permNode = mClient_Admin.getNode( "/JeCARS/default/jecars:Tools/P_toolRunner" );
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

    /** runTool
     * 
     * @throws JC_Exception
     * @throws InterruptedException
     * @throws IOException 
     */
    private void runTool() throws JC_Exception, InterruptedException, IOException {
    
      final JC_Clientable client = getClient();
      client.setCredentials("toolRunner", "toolRunner".toCharArray());
      final JC_Nodeable toolsNode = client.getNode("/JeCARS/default/jecars:Tools");
      final JC_UserNode user = (JC_UserNode)client.getUserNode().morphToNodeType();

      if (toolsNode.hasNode( "netstatToolTemplate" )) {
        // **** Remove template tool
        toolsNode.getNode( "netstatToolTemplate" ).removeNode();
        toolsNode.save();
      }
      if (toolsNode.hasNode( "netstatTool" )) {
        // **** Remove tool
        toolsNode.getNode( "netstatTool" ).removeNode();
        toolsNode.save();
      }
      // **** Create template tool
//      final JC_ToolNode templateTool = JC_ToolNode.createTemplateTool( toolsNode, "netstatToolTemplate", "org.jecars.tools.CARS_ExternalTool", true );
//      final JC_ToolNode templateTool = JC_ToolNode.createTemplateTool( toolsNode, "netstatToolTemplate", "org.jecars.tools.CARS_MultiExternalTool", true );
      final JC_ToolNode templateTool = JC_ToolNode.createTemplateTool( toolsNode, "netstatToolTemplate", "org.jecars.tools.CARS_SharedExternalOverthereTool", true );
//      templateTool.addConfigExternalTool( null, "c:/WINDOWS/system32/netstat.exe", false );
//      templateTool.addConfigExternalTool( null, "netstat", false );
      templateTool.addConfigExternalTool( null, "echo", false );
      templateTool.save();

      // **** Run the tool
      final JC_ToolNode runTool = JC_ToolNode.createTool( toolsNode, templateTool, "netstatTool", user );
      runTool.setAutoStartParameters( null, "jecars:Input" );
      runTool.save();
      runTool.addParameterData( "JeCARS-RunOnSystem" ).addParameter( "nlr01214" );
      runTool.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "Echo Hello World\n" );
      final JC_ParameterDataNode pdn = runTool.addParameterData( "commandLine" );
//      pdn.addParameter( "-s" );
      pdn.addParameter( "%COMPUTERNAME%" );
      while( 1==1 ) {
        final String state = runTool.getState();
        System.out.println( "state = " + state );
        Thread.sleep( 1000 );
        if (state.startsWith( JC_ToolNode.STATE_CLOSED )) {
          break;
        }
      }

      final List<JC_Nodeable> outputs = (List<JC_Nodeable>)runTool.getOutputResources();
      assertEquals( 2, outputs.size() );
      JC_Nodeable result = outputs.get(0);
      JC_Streamable toolResultStream = result.getClient().getNodeAsStream( result.getPath_JC() );
      String toolResult = JC_Utils.readAsString( toolResultStream.getStream() );
      System.out.println(toolResult);
      result = outputs.get(1);
      toolResultStream = result.getClient().getNodeAsStream( result.getPath_JC() );
      toolResult = JC_Utils.readAsString( toolResultStream.getStream() );
      System.out.println(toolResult);
      assertEquals( null, runTool.getState(), JC_ToolNode.STATE_CLOSED_COMPLETED );

    }

    @Test
    public void multiPCTest1() throws JC_Exception, InterruptedException, IOException {
      final JC_Clientable client = getClient();
      client.setCredentials("Administrator", "admin".toCharArray());
      JC_Nodeable systems = client.getSingleNode( "/JeCARS/Systems" );
      if (!systems.hasNode( "nlr01214" )) {
        JC_Nodeable system = systems.addNode( "nlr01214", "jecars:RES_System" );
        system.setTitle( "nlr01214" );
        JC_Nodeable cpu = system.addNode( "CPU", "jecars:RES_CPU" );
        cpu.setTitle( "CPU" );
        JC_Nodeable core = cpu.addNode( "TestCore", "jecars:RES_Core" );
        core.setTitle("TestCore");
        core.addMixin( "jecars:mixin_unstructured" );
      }
      systems.save();
      
      runTool();
      
      systems.save();
      return;
    }

    @Test
    public void multiPCTest2() throws JC_Exception, InterruptedException, IOException {
      final JC_Clientable client = getClient();
      client.setCredentials("Administrator", "admin".toCharArray());
      JC_Nodeable systems = client.getSingleNode( "/JeCARS/Systems" );
      if (!systems.hasNode( "nlr01327w" )) {
        JC_Nodeable system = systems.addNode( "nlr01327w", "jecars:RES_System" );
        system.setTitle( "nlr01327w" );
        JC_Nodeable cpu = system.addNode( "CPU", "jecars:RES_CPU" );
        cpu.setTitle( "CPU" );
        JC_Nodeable core = cpu.addNode( "TestCore", "jecars:RES_Core" );
        core.setTitle("OverthereCore");
        core.addMixin( "jecars:mixin_unstructured" );
      }
      systems.save();
      
      runTool();
      
      systems.save();
      return;
    }
    
}