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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import org.jecars.CARS_Utils;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Output;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_DataContainer implements IWFP_Interface {

  /** start
   * 
   * @param pTool
   * @param pContext 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
    File dc = new File( "C:/SVN/Projects/SPeCTRE/Beamforming/matlabtest" );
      System.out.println("HELLO DATACONTAINER!!!!!! write to " + dc.getAbsolutePath() );
    try {
      for( IWFP_Input input : pContext.getInputs() ) {
          System.out.println("write input " + input.getPath() );
        final InputStream is = input.openStream();
        final FileOutputStream fos = new FileOutputStream( new File( dc, input.getName() ));
        CARS_Utils.sendInputStreamToOutputStream( 10000, is, fos );
        fos.close();
        is.close();
      }
      
      // **** To output
      for( final File dcf : dc.listFiles() ) {
        final IWFP_Output output = pContext.addOutput( dcf.getName() );
        FileInputStream fis = new FileInputStream( dcf );
        output.setContents( fis );
        fis.close();
      }
      
    } catch( Exception e ) {
      pTool.reportException( Level.WARNING, e );
    }
      
    return WFP_InterfaceResult.OK();
  }
    
}
