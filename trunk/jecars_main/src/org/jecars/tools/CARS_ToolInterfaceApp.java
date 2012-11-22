/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_RESTMethodHandled;
import org.jecars.CARS_Utils;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.output.CARS_InputStream;

/**
 *
 * @author weert
 */
public class CARS_ToolInterfaceApp extends CARS_DefaultInterface {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools" );

  /**
   * 
   */
  public CARS_ToolInterfaceApp() {
    setToBeCheckedInterface( "org.jecars.tools.CARS_ToolInterfaceApp" );
    return;
  }


  
  /** init
   * 
   * @param pMain
   * @param pInterfaceNode
   * @throws Exception 
   */
  @Override
  public void init(CARS_Main pMain, Node pInterfaceNode) throws Exception {
    super.init( pMain, pInterfaceNode );
    return;
  }

  /** isCorrectInterface
   *
   * @param pInterfaceNode
   * @return
   * @throws RepositoryException
   */
  protected boolean isCorrectInterface( final Node pInterfaceNode ) throws RepositoryException {
    if (pInterfaceNode.hasProperty( CARS_DefaultMain.DEF_INTERFACECLASS ) && (pInterfaceNode.hasNode( "jecars:Config" ))) {
      return getToBeCheckedInterface().equals( pInterfaceNode.getProperty( CARS_DefaultMain.DEF_INTERFACECLASS ).getString() );
    }
    return false;
  }

  /** getWorkingDirectory
   *
   * @param pInterfaceNode
   * @return
   * @throws RepositoryException
   */
  static public File getWorkingDirectory( final Node pInterfaceNode ) throws RepositoryException {
    final long id = pInterfaceNode.getProperty( "jecars:Id" ).getLong();
    final Node config = pInterfaceNode.getNode( "jecars:Config" );
    final File wd = new File( config.getProperty( CARS_ExternalTool.WORKINGDIRECTORY ).getString() );
    return new File( wd, "wd_" + id );
  }

  /** isLink
   * 
   * @param pNode
   * @return
   * @throws RepositoryException
   */
  protected boolean isLink( final Node pNode ) throws RepositoryException {
    if (pNode.hasProperty( "jecars:IsLink" )) {
      return pNode.getProperty( "jecars:IsLink" ).getBoolean();
    }
    return false;
  }

  /** getNodes
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws CARS_RESTMethodHandled
   * @throws Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws CARS_RESTMethodHandled, Exception {
    if (isCorrectInterface( pInterfaceNode )) {

      if (isLink( pParentNode )) {
//      if (isLink( pParentNode ) && (!pParentNode.hasProperty( "jecars:PathToFile" )) &&
//          (pParentNode.hasProperty( "jecars:Partial")) && (!pParentNode.getProperty( "jecars:Partial" ).getBoolean())) {

        final File wd = getWorkingDirectory( pInterfaceNode );        
//       System.out.println("get aiiasjioj ji " + pParentNode.getPath() );
        if ((wd!=null) && (wd.exists())) {
//          final File input = new File( wd, pParentNode.getName() );
          final File input = new File( wd, pParentNode.getPath().substring( pInterfaceNode.getPath().length() ) );
          if (input.exists()) {
// System.out.println("PARTIAL STREAM FILE asdasd " + input.getAbsolutePath() );
//
            final Session appSession = CARS_Factory.getSystemApplicationSession();
            synchronized( appSession ) {
              final CARS_InputStream cis = new CARS_InputStream( appSession.getNode( pParentNode.getPath() ), input );
              pMain.getContext().setContentsResultStream( cis, "application/x-unknown" );
            }
//            final FileInputStream fis = new FileInputStream( input );
//            pMain.getContext().setContentsResultStream( fis, "application/x-unknown" );
          } else {
            throw new FileNotFoundException( input.getAbsolutePath() );
          }
        }
      }
    }
    super.getNodes(pMain, pInterfaceNode, pParentNode, pLeaf);
    return;
  }


  /** removeJeCARSNode
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pParams
   * @throws RepositoryException
   */
  @Override
  protected void removeJeCARSNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final JD_Taglist pParams ) throws RepositoryException {

    if (isCorrectInterface( pInterfaceNode )) {
      // **** pNode is a correct tool
      if (pInterfaceNode.hasProperty( "jecars:Id" )) {
        final File wd = getWorkingDirectory( pInterfaceNode );
        if ((wd!=null) && (wd.exists())) {
          if (CARS_Utils.deleteDirectory( wd )) {
            LOG.log( Level.INFO, "CARS_ExternalTool: Working directory " + wd.getAbsolutePath() + " is deleted" );
          } else {
            LOG.log( Level.WARNING, "CARS_ExternalTool: Error while deleting working directory " + wd.getAbsolutePath() );
          }
        }
      }
    }
    super.removeJeCARSNode(pMain, pInterfaceNode, pNode, pParams);

    return;
  }


}
