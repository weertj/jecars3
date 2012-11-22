/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_Tag implements IWFP_Interface {

  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
      System.out.println("----- TAG TOOL ------ " + Thread.currentThread() );
    
    try {
      for( final IWFP_Input input : pContext.getInputs() ) {
      System.out.println("TAG: " + input.getPath() + " -> " + pTool.getProperty( "jecars:WFPT_Tag_Name" ).getStringValue() + " = " + pTool.getProperty( "jecars:WFPT_Tag_Value" ).getStringValue() );
        input.setProperty( pTool.getProperty( "jecars:WFPT_Tag_Name" ).getStringValue(), pTool.getProperty( "jecars:WFPT_Tag_Value" ).getStringValue() );
        input.save();
      }
    
    } catch( Exception e ) {
      pTool.reportException( Level.WARNING, e);
    }
    return WFP_InterfaceResult.OK();
  }
    
}
