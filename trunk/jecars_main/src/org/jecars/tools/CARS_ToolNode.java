/*
 * Copyright 2009 NLR - National Aerospace Laboratory
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
package org.jecars.tools;

import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.Property;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;

/**
 *  CARS_ToolNode
 *
 * @version $Id: CARS_ToolNode.java,v 1.3 2009/06/21 21:00:14 weertj Exp $
 */
public class CARS_ToolNode {

  public static final String STATE_REQUEST = "jecars:StateRequest";

  private final CARS_ToolInterface mTool;


  /** CARS_ToolNode
   *
   * @param pToolNode
   */
  private CARS_ToolNode( final CARS_Main pMain, final Node pToolNode, final boolean pCreateNewMain ) throws Exception {
    mTool = CARS_ToolsFactory.getTool( pMain, pToolNode, null, pCreateNewMain );
    return;
  }

  /** newInstance
   *
   * @param pToolNode
   * @return
   */
  static public CARS_ToolNode newInstance( final CARS_Main pMain, final Node pToolNode, final boolean pCreateNewMain ) throws Exception {
    return new CARS_ToolNode( pMain, pToolNode, pCreateNewMain );
  }


  /** setParamProperty
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pPropName
   * @param pValue
   * @return
   * @throws java.lang.Exception
   */
  public Property setParamProperty( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final String pPropName, final String pValue ) throws Exception {
//    System.out.println("set tool param " + pPropName + " = " + pValue );
    if (pPropName.equals( STATE_REQUEST )) {
      final CARS_ActionContext ac = CARS_ActionContext.createActionContext( pMain.getContext(0) );
      final CARS_Main main = pMain.getFactory().createMain( ac );
        final Node n = main.getSession().getNode( pNode.getPath() );
//      final Node n = main.getSession().getRootNode().getNode( pNode.getPath().substring(1) );
//       if (n.isNodeType( "mix:lockable")==false) n.addMixin( "mix:lockable" );
//       n.save();
      if (n.isLocked()) {
        return n.getProperty( pPropName );
      } else {
        final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( main, n, null, false );
        return ti.setStateRequest( pValue );
      }
    }
    return null;
  }

  /** addNode
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws java.lang.Exception
   */
  public Node addNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws Exception {
    final String dip = mTool.getDefaultInstancePath();
    if (dip==null) {
      return CARS_DefaultMain.addNode( pParentNode, pName, pPrimType );
    }
    Node toolParent = pMain.getNode( dip, null, false );
    String toolName = mTool.getName();
    UUID uuid = UUID.randomUUID();
    toolName += '_' + uuid.toString();
    final String primToolType = mTool.getTool().getPrimaryNodeType().getName();
    final Node newTool = toolParent.addNode( toolName, primToolType );
    final CARS_ActionContext ac = pMain.getContext();
    newTool.addMixin( "jecars:permissionable" );
    CARS_Utils.addPermission( newTool, null, ac.getUsername(), "delegate,read,add_node,set_property,get_property,remove" );
    newTool.setProperty( "jecars:ToolTemplate", mTool.getTool().getPath() );
    toolParent.save();
    final Node newNode = CARS_DefaultMain.addNode( newTool, pName, pPrimType );
    toolParent.save();
    return newNode;
  }

  /** nodeAdded
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pNewNode
   * @param pBody
   * @throws java.lang.Exception
   */
  public void nodeAdded( final CARS_Main pMain, final Node pInterfaceNode, final Node pNewNode, final InputStream pBody )  throws Exception {
    return;
  }

  /** nodeAddedAndSaved
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNewNode
   * @throws java.lang.Exception
   */
  public void nodeAddedAndSaved( final CARS_Main pMain, final Node pInterfaceNode, final Node pNewNode )  throws Exception {
    final String arwi = mTool.getAutoRunWhenInput();
    if (arwi!=null) {
      final String nodeName = CARS_Utils.convertNodeName( pNewNode );
//      System.out.println("comoom otol " + pNewNode.getName() + " ---> " + arwi );
      final Pattern checkPat = Pattern.compile( arwi );
      final Matcher m = checkPat.matcher( nodeName );
      if (m.find()) {
        final CARS_ActionContext ac = CARS_ActionContext.createActionContext( pMain.getContext(0) );
        final CARS_Main main = pMain.getFactory().createMain( ac );
        final Node n = main.getSession().getRootNode().getNode( pNewNode.getParent().getPath().substring(1) );
        if (!n.isLocked()) {
          final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( main, n, null, false );
          ti.setStateRequest( CARS_ToolInterface.STATEREQUEST_START );
        }
//        System.out.println(" AUTO START TOOL------ " + mTool.getName() );
      }
    }
    return;
  }


}
