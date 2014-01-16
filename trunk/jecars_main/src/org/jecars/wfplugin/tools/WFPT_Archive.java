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
public class WFPT_Archive implements IWFP_Interface {

  static private final Object EXPORT = new Object();
  
  static public final String NODEFORARCHIVE          = "NodeForArchive";
  static public final String ARCHIVEDIRECTORY        = "ArchiveDirectory";
  static public final String JECARSBACKUPDIRECTORY   = "JeCARSBackupDirectory";
  
  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
    
    try {
      Node node = (Node)pContext.getTransientInput( NODEFORARCHIVE );
      if (node==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( NODEFORARCHIVE );
        if (cpar!=null) {
          node = pTool.getNodeFromRoot( cpar.getStringValue() ).getJCRNode();
        }
      }
      String dir = (String)pContext.getTransientInput( ARCHIVEDIRECTORY );
      if (dir==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( ARCHIVEDIRECTORY );
        if (cpar!=null) {
          dir = cpar.getStringValue();
        }
      }
      String jecarsdir = (String)pContext.getTransientInput( JECARSBACKUPDIRECTORY );
      if (jecarsdir==null) {
        IWFP_ContextParameter cpar = pContext.getParameter( JECARSBACKUPDIRECTORY );
        if (cpar!=null) {
          jecarsdir = cpar.getStringValue();
        }
      }

      if ((node!=null) && (dir!=null)) {      
        try {
          synchronized( EXPORT ) {
            final List<Node> humanNodes = new ArrayList<Node>();
            final NodeIterator ni = node.getNodes();
            while( ni.hasNext() ) {
              humanNodes.add( ni.nextNode() );
            }
            writeHumanNodes( new File( dir, node.getName() ), humanNodes );
          }
        } catch( IOException ie ) {
        } catch( RepositoryException re ) {
          pTool.reportException( Level.SEVERE, re );
          return WFP_InterfaceResult.ERROR().setError(re);
        }
      }

      if ((node!=null) && (jecarsdir!=null)) {
        try {
          synchronized( EXPORT ) {
            JB_ExportData export = new JB_ExportData();
            JB_Options options = new JB_Options();
            pTool.reportMessage( Level.CONFIG, "Archive to " + dir );
            pTool.reportMessage( Level.CONFIG, "JeCARSBackup to " + jecarsdir );
            options.changeFilePathRoot( new File( dir, node.getName() ) );
            options.setExportDirectory( new File( new File( dir, node.getName() ), jecarsdir ) );
            options.addExcludePath( "/jcr:system.*" );
            export.exportToDirectory( node, options );
          }
        } catch( Exception e ) {
          pTool.reportException( Level.SEVERE, e );
          return WFP_InterfaceResult.ERROR().setError(e);        
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
