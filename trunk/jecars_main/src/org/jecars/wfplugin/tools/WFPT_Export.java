/*
 * Copyright 2013-2014 NLR - National Aerospace Laboratory
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
import javax.jcr.query.Query;
import org.jecars.CARS_Utils;
import org.jecars.backup.JB_ExportData;
import org.jecars.backup.JB_Options;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 * WFPT_Archive
 *
 * @author weert
 */
public class WFPT_Export implements IWFP_Interface {

  static private final Object EXPORT = new Object();

  static public final String METADATADEF = "MetaDataDef";
  static public final String NODEFOREXPORT = "NodeForExport";
  static public final String TOOLTYPE = "ToolType";
  static public final String TOOLINSTANCENAME = "ToolInstanceName";
  static public final String DATAFILENAME = "DatafileName";
  static public final String EXPORTDIRECTORY = "ExportDirectory";

  /**
   * start
   *
   * @param pTool
   * @param pContext
   * @return
   */
  @Override
  public WFP_InterfaceResult start(final IWFP_Tool pTool, final IWFP_Context pContext) {

    try {
      final Node   exportNode       = pContext.getParameterNodeValue(NODEFOREXPORT, pTool);
      final String toolType         = pContext.getParameterStringValue(TOOLTYPE);
      final String exportDirectory  = pContext.getParameterStringValue(EXPORTDIRECTORY);
      final String toolInstanceName = pContext.getParameterStringValue(TOOLINSTANCENAME);
      final String dataFileName     = pContext.getParameterStringValue(DATAFILENAME);
      final String metaData         = pContext.getParameterStringValue(METADATADEF);
      
      String toolTypeValue          = toolType;
      String toolInstanceNameValue  = toolInstanceName;
      String dataFileNameValue      = dataFileName;
      
      if (metaData==null) {
        // **** No meta data is set, use the value given a parameters
        exportNode( pTool, exportNode, toolTypeValue, exportDirectory, toolInstanceNameValue, dataFileNameValue );
      } else {
        // **** Meta data is set... read the properties for the metadata node
        final StringBuilder query = new StringBuilder( "SELECT * FROM jecars:inputresource WHERE " );
        query.append( " CONTAINS(*, '" ).append( metaData ).append( "')" );
        final Query q = exportNode.getSession().getWorkspace().getQueryManager().createQuery( query.toString(), Query.SQL );
        NodeIterator ni = q.execute().getNodes();
        while (ni.hasNext()) {
          Node metadataNode = ni.nextNode();
          Node workflowNode = metadataNode.getParent();
          if (workflowNode.isNodeType( "jecars:Workflow" )) {
            toolTypeValue         = metadataNode.getProperty( toolType ).getString();
            toolInstanceNameValue = metadataNode.getProperty( toolInstanceName ).getString();
            dataFileNameValue     = metadataNode.getProperty( dataFileName ).getString();
            exportNode( pTool, workflowNode, toolTypeValue, exportDirectory, toolInstanceNameValue, dataFileNameValue );
          }
        }

      }
      
    } catch (RepositoryException | IOException | WFP_Exception e) {
      pTool.reportException(Level.SEVERE, e);
      return WFP_InterfaceResult.ERROR().setError(e);
    }
    return WFP_InterfaceResult.OK();

  }

  public void exportNode( final IWFP_Tool pTool, Node pExportNode, String pToolType, String pExportDirectory, String pToolInstanceName, String pDataFileName) throws RepositoryException, IOException {
    if ((pExportNode != null) && (pToolType != null) && (pExportDirectory != null) && (pToolInstanceName != null) && (pDataFileName != null)) {
      final List<Node> humanNodes = new ArrayList<>(128);
      final NodeIterator ni = pExportNode.getNodes();
      while (ni.hasNext()) {
        humanNodes.add(ni.nextNode());
      }
      if (pDataFileName.indexOf( '.' )!=-1) {
        pDataFileName = pDataFileName.substring( 0, pDataFileName.indexOf( '.' ) );
      }
      File exportDir = new File( pExportDirectory, pToolType + "/" + pToolInstanceName + "/" + pDataFileName);
      pTool.reportMessage( Level.INFO, "Export " + pExportNode.getName() + " to " + exportDir.getAbsolutePath() );
      writeHumanNodes( exportDir, humanNodes);
    }
  }

  /**
   * writeHumanNodes
   *
   * @param pDirectory
   * @param pNodes
   * @throws RepositoryException
   * @throws IOException
   */
  private void writeHumanNodes(final File pDirectory, final List<Node> pNodes) throws RepositoryException, IOException {
    pDirectory.mkdirs();
    for (final Node node : pNodes) {
      CARS_Utils.copyInputResourceToDirectory(CARS_Utils.getLinkedNode(node), pDirectory);
    }
    // **** Remove empty files
    for( File f : pDirectory.listFiles() ) {
      if (f.isFile()) {
        if (f.length()==0) {
          f.delete();
        }
      }
    }
  }

}
