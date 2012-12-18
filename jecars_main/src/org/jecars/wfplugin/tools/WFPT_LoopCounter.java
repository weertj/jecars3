/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin.tools;

import java.util.Calendar;
import java.util.logging.Level;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_LoopCounter implements IWFP_Interface {  
      
  static public final String COUNTER = "Counter";
   
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
    try {
      IWFP_ContextParameter param = pTool.getContextParameter( pContext, null, COUNTER, true );
      if (param!=null) {
        long counter = Long.parseLong(param.getStringValue());
        counter--;
        param.setValue( String.valueOf(counter) );
        pTool.save();
        if (--counter<0) {
          return WFP_InterfaceResult.STOP();
        }
      }
    } catch( WFP_Exception we ) {
      pTool.reportException( Level.SEVERE, we );
      return WFP_InterfaceResult.ERROR();        
    }
      
    return WFP_InterfaceResult.OK();
  }

    
}
