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

//  private static final String SERVER_ADDRESS = "http://3dflight.nlr.nl:8080/respite";
//  private static final String SERVER_ADDRESS = "http://localhost/cars";
  private static final String SERVER_ADDRESS = "http://localhost:8082/spade";
//  private static final String SERVER_ADDRESS = "http://nlr01031w:8080/spectreCars";

  static protected JC_Clientable getClient() throws JC_Exception {
     final JC_Clientable client = JC_Factory.createClient( SERVER_ADDRESS );
     client.setCredentials( "Administrator", "admin".toCharArray() );
//     client.getNode( "/JeCARS/ApplicationSources/AdminApp/Init_JeCARS_(!WARNING!)" );
     return client;
//      return JC_Factory.createLocalClient();
  }

}
