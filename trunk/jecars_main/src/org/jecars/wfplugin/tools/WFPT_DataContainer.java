/*
 * Copyright 2012-2013 NLR - National Aerospace Laboratory
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
import java.util.List;
import java.util.logging.Level;
import org.jecars.CARS_Utils;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Node;
import org.jecars.wfplugin.IWFP_Output;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_DataContainer implements IWFP_Interface {

  static public final String BASEDIRECTORY  = "BaseDirectory";
  static public final String DATADIRECTORY  = "Directory";
  static public final String APPEND         = "Append";

  /** start
   * 
   * @param pTool
   * @param pContext 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
//    File dc = new File( "C:/SVN/Projects/SPeCTRE/Beamforming/matlabtest" );
//      System.out.println("HELLO DATACONTAINER!!!!!! write to " + dc.getAbsolutePath() );
    // **** Check workflow for overrule parameterw
    final boolean append = Boolean.parseBoolean(pTool.getParameter( APPEND, "false" ));
    final String basedirectory = pTool.getParameter( BASEDIRECTORY, null );
    final String directory = pTool.getParameter( DATADIRECTORY, null );
    if (directory==null) {
      try {
        // **** Check if the data folder is set
        final List<IWFP_Node> nodes = pTool.getTask().getNode( "data" ).getNodes();
        if (nodes.isEmpty()) {
          pTool.reportException( Level.SEVERE, new WFP_Exception( "WFPT_DataContainer: Directory parameter not set") );
          return WFP_InterfaceResult.STOP();          
        } else {
          for( final IWFP_Node n : nodes ) {
            pContext.copyInput( n );
          }
        }
      } catch( WFP_Exception we ) {
        pTool.reportException( Level.SEVERE, we );
        return WFP_InterfaceResult.ERROR();        
      }
    } else {
      try {          
        File dc;
        if (basedirectory==null) {
          dc = new File( directory );
        } else {
          dc = new File( basedirectory, directory );
        }
        dc.mkdirs();
        for( IWFP_Input input : pContext.getInputs() ) {
          File writeFile = new File( dc, input.getName() );
            System.out.println("write input " + input.getPath() + " -> " + writeFile.getAbsolutePath() );
          pTool.reportMessage( Level.INFO, "WFPT_DataContainer: Write input " + input.getPath() + " to " + writeFile.getAbsolutePath() );
          final InputStream is = input.openStream();
          final FileOutputStream fos = new FileOutputStream( writeFile, append );
          CARS_Utils.sendInputStreamToOutputStream( 10000, is, fos );
          fos.close();
          is.close();
        }
        
        // **** To output  TODO Temporary disable.... missing use-case
//        for( final File dcf : dc.listFiles() ) {
//            final IWFP_Output output = pContext.addOutput( dcf.getName() );
//            FileInputStream fis = new FileInputStream( dcf );
//            output.setContents( fis );
//            fis.close();
//        }
      
      } catch( Exception e ) {
        pTool.reportException( Level.SEVERE, e );
        return WFP_InterfaceResult.ERROR();
      }
    }
      
    return WFP_InterfaceResult.OK();
  }
    
}
