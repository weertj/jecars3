/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.gui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.scripts.JCS_defaultScript;

/**
 *
 * @author weert
 */
public class GUI_Frame extends JFrame {

  /** setClient
   *
   * @param pClient
   * @throws JC_Exception
   */
  public void setClient( final JC_Clientable pClient ) throws JC_Exception {
    return;
  }

  public void reportException( final Throwable pT ) {
    pT.printStackTrace();
  }

  /** start
   *
   * @param pFrame
   * @param pArgs
   */
  public static void start( final GUI_Frame pFrame, final String pArgs[] ) {
   final JCS_defaultScript ds = new JCS_defaultScript();
   ds.parseArguments( pArgs );
   if (("".equals( ds.mUsername)) || ("".equals( ds.mPassword )) || ("".equals( ds.mJeCARSServer ))) {
     final LoginDialog ld = new LoginDialog( null, true );
     ld.setVisible( true );
     if (ld.isOk()) {
       ds.mUsername      = ld.getUserName();
       ds.mPassword      = new String( ld.getPassword() );
       ds.mJeCARSServer  = ld.getServer();
     } else {
       System.exit( 0 );
     }
   }

   java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
              try {
                final JC_Clientable client = JC_Factory.createClient( ds.mJeCARSServer );
                client.setCredentials( ds.mUsername, ds.mPassword.toCharArray() );
                pFrame.setClient( client );
                pFrame.setVisible(true);
              } catch( Exception e ) {
                e.printStackTrace();
                JOptionPane.showMessageDialog( null, "<html>" + e.getMessage() + "</html>", "Error", JOptionPane.ERROR_MESSAGE );
              }
            }
        });
    }


}
