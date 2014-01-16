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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Utils;
import org.jecars.backup.JB_ExportData;
import org.jecars.backup.JB_ImportData;
import org.jecars.backup.JB_Options;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_InterfaceResult;

/** WFPT_Archive
 *
 * @author weert
 */
public class WFPT_Restore implements IWFP_Interface {

  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
    
    try {
      Node node = (Node)pContext.getTransientInput( "NodeForRestore" );
      if (node==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( "NodeForRestore" );
        if (cpar!=null) {
          node = pTool.getNodeFromRoot( cpar.getStringValue() ).getJCRNode();
        }
      }
      String dir = (String)pContext.getTransientInput( "ArchiveDirectory" );
      if (dir==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( "ArchiveDirectory" );
        if (cpar!=null) {
          dir = cpar.getStringValue();
        }
      }
      String jecarsdir = (String)pContext.getTransientInput( "JeCARSBackupDirectory" );
      if (jecarsdir==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( "JeCARSBackupDirectory" );
        if (cpar!=null) {
          jecarsdir = cpar.getStringValue();
        }
      }

      if ((node!=null) && (dir!=null) && (jecarsdir!=null)) {
        final JB_ImportData restore = new JB_ImportData();
        final File exportJeCARS = new File( jecarsdir, "exportJeCARS.jb" );
        final FileInputStream fis = new FileInputStream( exportJeCARS );
        final JB_Options options = new JB_Options();
        options.setImportDirectory( new File(jecarsdir) );
        if (restore.importFromStream( node, fis, options )) {
          // **** Restore ok
        } else {
          throw new Exception( "Restore error: " + exportJeCARS.getAbsolutePath() );
        }
      }
    } catch( Exception e ) {
      pTool.reportException( Level.SEVERE, e );
      return WFP_InterfaceResult.ERROR().setError(e);        
    }
    
    return WFP_InterfaceResult.OK();
    
  }
  
  /** writeHumanNodes
   * 
   * @param pDirectory
   * @param pNodes
   * @throws RepositoryException
   * @throws IOException 
   */
  private void writeHumanNodes( final File pDirectory, final List<Node>pNodes ) throws RepositoryException, IOException {
    pDirectory.mkdirs();
    for( final Node node : pNodes ) {
      CARS_Utils.copyInputResourceToDirectory( CARS_Utils.getLinkedNode(node), pDirectory );
    }
  }
  
  
  
}
