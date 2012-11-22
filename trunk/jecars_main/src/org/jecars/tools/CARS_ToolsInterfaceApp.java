/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.tools;

import java.util.UUID;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Main;
import org.jecars.CARS_RESTMethodHandled;
import org.jecars.CARS_Utils;
import org.jecars.apps.CARS_DefaultInterface;

/**
 *
 * @author weert
 */
public class CARS_ToolsInterfaceApp extends CARS_DefaultInterface {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools" );

  /** getToBeCheckedInterface
   *
   * @return
   */
  protected String getToBeCheckedInterface() {
    return "org.jecars.tools.CARS_ToolsInterfaceApp";
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

  /** copyNode
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pCopyNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws Exception
   */
  @Override
  public Node copyNode( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, Node pCopyNode, String pName, String pPrimType, JD_Taglist pParams ) throws Exception {
    if (pCopyNode.isNodeType( "jecars:Workflow" )) {
      return super.copyNode(pMain, pInterfaceNode, pParentNode, pCopyNode, pName, pPrimType, pParams);      
    } else if (pCopyNode.isNodeType( "jecars:Tool" )) {
      final String force = (String)pParams.getData( "jecars:force" );
      pParams.removeData( "jecars:force" );
      String toolName = pName;
      if (!"true".equalsIgnoreCase(force)) {
        // **** Create a tool instance
        final UUID uuid = UUID.randomUUID();
        toolName += '_' + uuid.toString();
      }
      final String primToolType = pCopyNode.getPrimaryNodeType().getName();
      final Node newTool = pParentNode.addNode( toolName, primToolType );
      final CARS_ActionContext ac = pMain.getContext();
      newTool.addMixin( "jecars:permissionable" );
      CARS_Utils.addPermission( newTool, null, ac.getUsername(), "delegate,read,add_node,set_property,get_property,remove" );
      newTool.setProperty( "jecars:ToolTemplate", pCopyNode.getPath() );
      pParentNode.save();
//      final Node newNode = CARS_DefaultMain.addNode( newTool, pName, pPrimType );
      newTool.addMixin( "jecars:interfaceclass" );
      newTool.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_ToolInterfaceApp" );
      newTool.addNode( "jecars:Input", "jecars:dataresource" );
      pParentNode.save();
      return newTool;

    }

    return super.copyNode(pMain, pInterfaceNode, pParentNode, pCopyNode, pName, pPrimType, pParams);
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

    super.getNodes(pMain, pInterfaceNode, pParentNode, pLeaf);
    return;
  }


}
