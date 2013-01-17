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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_ParameterDataNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_ToolEventNode;
import org.jecars.client.nt.JC_ToolNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
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
public class JC_ToolTest {

    private JC_Clientable mClient_Admin = null;

    public JC_ToolTest() {
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

    /** testRunTool
     *
     * @throws org.jecars.client.JC_Exception
     */
  //  @Ignore("even niet")
//    @Test
//    public void testRunTool() throws JC_Exception, Exception, InterruptedException, UnsupportedEncodingException {
//      final JC_Clientable client = getClient();
//      client.setCredentials("toolRunner", "toolRunner".toCharArray());
////      client.setCredentials("Administrator", "admin".toCharArray());
//      final JC_Nodeable toolsNode = client.getNode("/JeCARS/default/jecars:Tools");
//      final JC_UserNode user = (JC_UserNode)client.getUserNode().morphToNodeType();
//
//      if (toolsNode.hasNode( "simpleCalcTemplate" )) {
//        // **** Remove template tool
//        toolsNode.getNode( "simpleCalcTemplate" ).removeNode();
//        toolsNode.save();
//      }
//      if (toolsNode.hasNode( "simpleCalc" )) {
//        // **** Remove tool
//        toolsNode.getNode( "simpleCalc" ).removeNode();
//        toolsNode.save();
//      }
//      if (toolsNode.hasNode( "simpleCalc2" )) {
//        // **** Remove tool
//        toolsNode.getNode( "simpleCalc2" ).removeNode();
//        toolsNode.save();
//      }
//      if (toolsNode.hasNode( "simpleCalc3" )) {
//        // **** Remove tool
//        toolsNode.getNode( "simpleCalc3" ).removeNode();
//        toolsNode.save();
//      }
//      // **** Create template tool
//      final JC_ToolNode templateTool = JC_ToolNode.createTemplateTool( toolsNode, "simpleCalcTemplate", "org.jecars.tools.CARS_SimpleCalcTool", true );
////      templateTool.setAutoStartParameters( toolsNode.getPath(), "jecars:Input" );
//      templateTool.save();
//
//      // **** Run the tool
//      final JC_ToolNode runTool = JC_ToolNode.createTool( toolsNode, templateTool, "simpleCalc", user );
//      runTool.setAutoStartParameters( null, "jecars:Input" );
//      runTool.save();
//      runTool.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "No1=2\nNo2=8\n" );
////      runTool.start();
//      final JC_ToolNode runTool2 = JC_ToolNode.createTool( toolsNode, templateTool, "simpleCalc2", user );
//      runTool2.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "No1=12\nNo2=8\n" );
//      runTool2.start();
//      final JC_ToolNode runTool3 = JC_ToolNode.createTool( toolsNode, templateTool, "simpleCalc3", user );
//      runTool3.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "No1=14\nNo2=16\n" );
//      runTool3.start();
//      int runs = 0;
//      while( 1==1 ) {
//        final String state = runTool.getState();
//        System.out.println( "state = " + state );
//        final String state2 = runTool2.getState();
//        System.out.println( "state2 = " + state2 );
//        final String state3 = runTool3.getState();
//        System.out.println( "state3 = " + state3 );
//        Thread.sleep( 1000 );
//        if ((state.startsWith( JC_ToolNode.STATE_CLOSED )) &&
//            (state2.startsWith( JC_ToolNode.STATE_CLOSED )) &&
//            (state3.startsWith( JC_ToolNode.STATE_CLOSED ))) break;
//        if (runs==3) {
//          // **** After 3 runs stop tool3
//          runTool3.stop();
//        }
//        if (runs==4) {
//          // **** After 4 runs abort tool2
//          runTool2.abort();
//        }
//        runs++;
//      }
////      Thread.sleep( 2000 );
//      List<JC_ToolEventNode> tens = runTool.getToolEvents();
//      for (JC_ToolEventNode ten : tens) {
//        System.out.println(ten.toString());
//      }
//      tens = runTool2.getToolEvents();
//      for (JC_ToolEventNode ten : tens) {
//        System.out.println(ten.toString());
//      }
//      tens = runTool3.getToolEvents();
//      for (JC_ToolEventNode ten : tens) {
//        System.out.println(ten.toString());
//      }
//
//      assertEquals( null, runTool.getState(), JC_ToolNode.STATE_CLOSED_COMPLETED );
//      assertEquals( null, runTool2.getState(), JC_ToolNode.STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
//      assertEquals( null, runTool3.getState(), JC_ToolNode.STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
//
//      return;
//    }


    /** testExternalTool
     *
     * @throws org.jecars.client.JC_Exception
     */
//    @Test
    public void testExternalTool() throws JC_Exception, Exception, InterruptedException, UnsupportedEncodingException {
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
      final JC_ToolNode templateTool = JC_ToolNode.createTemplateTool( toolsNode, "netstatToolTemplate", "org.jecars.tools.CARS_ExternalTool", true );
      templateTool.addConfigExternalTool( null, "c:/WINDOWS/system32/netstat.exe", false );
      templateTool.save();

      // **** Run the tool
      final JC_ToolNode runTool = JC_ToolNode.createTool( toolsNode, templateTool, "netstatTool", user );
      runTool.setAutoStartParameters( null, "jecars:Input" );
      runTool.save();
      runTool.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "Echo Hello World\n" );
      final JC_ParameterDataNode pdn = runTool.addParameterData( "commandLine" );
      pdn.addParameter( "-s" );
      while( 1==1 ) {
        final String state = runTool.getState();
        System.out.println( "state = " + state );
        Thread.sleep( 1000 );
        if (state.startsWith( JC_ToolNode.STATE_CLOSED )) {
          break;
        }
      }

      final List<JC_Nodeable> outputs = (List<JC_Nodeable>)runTool.getOutputs();
      assertEquals( 1, outputs.size() );
      final JC_Nodeable result = outputs.get(0);

      final JC_Streamable toolResultStream = result.getClient().getNodeAsStream( result.getPath_JC() );
      final String toolResult = JC_Utils.readAsString( toolResultStream.getStream() );
      System.out.println(toolResult);
      assertEquals( null, runTool.getState(), JC_ToolNode.STATE_CLOSED_COMPLETED );
      return;
    }
}