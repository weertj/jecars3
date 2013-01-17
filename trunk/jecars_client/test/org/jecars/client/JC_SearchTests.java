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

import java.util.Collection;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/** JC_SearchTests
 *
 * @version $Id: JC_SearchTests.java,v 1.3 2009/02/18 16:12:57 weertj Exp $
 */
public class JC_SearchTests {

    private JC_Clientable mClient_Admin = null;

    public JC_SearchTests() {
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
        JC_Params p = mClient_Admin.createParams( null );
        p.setEventCollectionID( "JC_SearchTests" );
        mClient_Admin.setDefaultParams( JC_RESTComm.GET,    p );
        mClient_Admin.setDefaultParams( JC_RESTComm.HEAD,   p );
        mClient_Admin.setDefaultParams( JC_RESTComm.DELETE, p );
        mClient_Admin.setDefaultParams( JC_RESTComm.PUT,    p );
        mClient_Admin.setDefaultParams( JC_RESTComm.POST,   p );
      } catch( Exception e ) {
        fail( e.getMessage() );
      }
    }

    @After
    public void tearDown() {
    }

    /** searchNamePatternTest1
     *
     * @throws java.lang.Exception
     */
    @Test
    public void searchNamePatternTest1() throws Exception {
      JC_Nodeable rootNode = mClient_Admin.getRootNode();
      rootNode.refresh();
      JC_Nodeable jn = rootNode.getNode( "JeCARS" );
      JC_Filter filter = JC_Filter.createFilter();
      filter.setNamePattern( "P_*|jecars:*" );
      Collection<JC_Nodeable> nodes = jn.getNodes( null, filter, null );
      assertEquals( 2, nodes.size() );
      String ts = "";
      for (JC_Nodeable node : nodes) {
        ts += node.getPath();
      }
      if ((ts.indexOf( "/JeCARS/jecars:Trashcans" )==-1) || (ts.indexOf( "/JeCARS/P_DefaultReadGroup" )==-1)) {
        assertTrue( ts, false );
      }
      return;
    }

    /** searchEventTest1
     *
     * @throws org.jecars.client.JC_Exception
     */
    @Test
    public void searchEventTest1() throws Exception {
      JC_Nodeable     rootNode = mClient_Admin.getRootNode();
      JC_Nodeable   eventsNode = rootNode.getNode( "JeCARS/default/Events" );
      JC_UsersNode   usersNode = (JC_UsersNode)rootNode.getNode( "JeCARS/default/Users" ).morphToNodeType();
      JC_GroupsNode groupsNode = (JC_GroupsNode)rootNode.getNode( "JeCARS/default/Groups" ).morphToNodeType();
      JC_UserNode eventUser = null;
      JC_GroupNode eventGroup = null;
      JC_PermissionNode eventGroupPerm = null;
      if (usersNode.hasUser( "eventUser" )==true) eventUser = usersNode.getUser( "eventUser" );
      if (groupsNode.hasGroup( "eventGroup" )==true) eventGroup = groupsNode.getGroup( "eventGroup" );
      if (eventsNode.hasNode( "P_testEventsRights" )==true) {
        eventGroupPerm = (JC_PermissionNode)eventsNode.getNode( "P_testEventsRights" ).morphToNodeType();
        eventGroupPerm.removeNode();
        eventGroupPerm.save();
        eventGroupPerm = null;
      }
      if (eventGroup==null) {
        // **** Create event group
        eventGroup = groupsNode.addGroup( "eventGroup" );
        groupsNode.save();
        JC_GroupNode drg = groupsNode.getGroup( "DefaultReadGroup" );
        drg.addGroup( eventGroup );
        drg.save();
      }
      if (eventUser==null) {
        // **** Create event user
        eventUser = usersNode.addUser( "eventUser", "Test Event User", "eventUser".toCharArray(), JC_PermissionNode.RS_ALLREADACCESS );
        usersNode.save();
      }
      if (eventGroupPerm==null) {
        eventGroupPerm = (JC_PermissionNode)eventsNode.addNode( "P_testEventsRights", "jecars:Permission" ).morphToNodeType();
        eventGroupPerm.save();
        eventGroupPerm.addRights( eventGroup, JC_PermissionNode.RS_ALLREADACCESS );
        eventGroupPerm.save();
      }
      eventGroup.addUser( eventUser );
      eventGroup.save();
      assertTrue( eventUser.getPath().equals( "/JeCARS/default/Users/eventUser" ));

      try {
        final JC_Clientable euc = JC_ClientTarget.getClient();
        euc.setCredentials( "eventUser", "eventUser".toCharArray() );

        JC_Nodeable rn = euc.getRootNode();
        JC_Nodeable events = rn.getNode( "JeCARS/default/Events" );
        assertTrue( events.getPath().equals( "/JeCARS/default/Events" ));

        // **** Search for events
        JC_Params params = euc.createParams( JC_RESTComm.GET );
        params.setDeep( true );
        JC_Filter filter = JC_Filter.createFilter();
        filter.addCategory( "Event" );
        JC_Query  query  = JC_Query.createQuery();
        query.setWhereString( "jecars:EventCollectionID='JC_SearchTests' OR jecars:EventCollectionID='123'" );
        Collection<JC_Nodeable> evs = events.getNodes( params, filter, query );
        if (evs.size()>0) {
          for (JC_Nodeable ev : evs) {
            System.out.println("Event:" + ev.getName());
          }
        } else {
          throw new JC_Exception( "no events found" );
        }

      } finally {
//        usersNode.removeUser( eventUser );
//        groupsNode.removeGroup( eventGroup );
//        eventGroupPerm.removeNode();
//        eventGroupPerm.save();
      }
      return;
    }

}