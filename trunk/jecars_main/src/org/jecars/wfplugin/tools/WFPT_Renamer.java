/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin.tools;

import java.util.logging.Level;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_Renamer implements IWFP_Interface {

  static public final String RENAMETO = "RenameTo";
    
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
      
    try {
      String renameto = pTool.getParameter( RENAMETO, null );
      if (renameto!=null) {
        for( final IWFP_Input input : pContext.getInputs() ) {
          final String title;
          if (input.hasProperty( "jecars:Title" )) {
            title = input.getProperty( "jecars:Title" ).getStringValue();
          } else {
            title = renameto;
          }
          input.rename( renameto, title );
        }
      }
    } catch(WFP_Exception we) {
      pTool.reportException( Level.SEVERE, we );
      return WFP_InterfaceResult.ERROR();
    }

    return WFP_InterfaceResult.OK();
  }

}
