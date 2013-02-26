/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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
package org.jecars.wfplugin.tools;

import java.util.logging.Level;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Task;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_LoopCounter implements IWFP_Interface {  
      
  static public final String COUNTER = "Counter";
   
  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
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
      } else {
        IWFP_ContextParameter p = pContext.addParameter( COUNTER );
        p.setValue( "1" );
        pTool.getTask();
        pContext.save();
//        final IWFP_Task task = pTool.getWorkflow().getTaskByName( "Sleep0" );
//        task.createParameter( "TestBla_Directory" ).setValue( "babalsalsa" );
//        task.save();
      }
    } catch( WFP_Exception we ) {
      pTool.reportException( Level.SEVERE, we );
      return WFP_InterfaceResult.ERROR();        
    }
      
    return WFP_InterfaceResult.OK();
  }

    
}
