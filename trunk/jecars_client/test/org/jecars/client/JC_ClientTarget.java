/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client;

/**
 *
 * @author weert
 */
public class JC_ClientTarget {

  private static final String SERVER_ADDRESS = "http://localhost:8888/cars";

  static protected JC_Clientable getClient() throws JC_Exception {
     final JC_Clientable client = JC_Factory.createClient( SERVER_ADDRESS );
     client.setCredentials( "Administrator", "admin".toCharArray() );
     return client;
  }

}
