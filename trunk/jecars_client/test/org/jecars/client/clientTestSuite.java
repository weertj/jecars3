/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author weert
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({org.jecars.client.JC_ToolTest.class,
                     org.jecars.client.JC_ObservationManagerTest.class,
                     org.jecars.client.JC_PermissionsTest.class,
                     org.jecars.client.JC_SearchTests.class,
                     org.jecars.client.JC_UsersTests.class,
                     org.jecars.client.JC_DefaultNodeTest.class,
                     org.jecars.client.JC_UtilsTest.class})
public class clientTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}