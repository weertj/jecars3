/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.apps;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.jcr.*;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Main;

/**
 * CARS_Interface
 *
 * @version $Id: CARS_Interface.java,v 1.12 2009/07/30 12:07:42 weertj Exp $
 */
public interface CARS_Interface {

  /** Retrieves the name of the application source
   * @return
   */
  String getName();
    
  /** Will be called only once, when JeCARS is started
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source
   */
  void init( CARS_Main pMain, Node pInterfaceNode ) throws Exception;
  
  /** getVersionEventFolders
   * 
   * @return
   */
  ArrayList<String>getVersionEventFolders();
  
  /** getVersion
   * 
   * @return
   */
  String getVersion();
  
  /** Add a node to the repository
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pParentNode the node under which the object must be added
   * @param pName the node name
   * @param pPrimType the node type
   * @param pParams list of parameters
   */
  Node addNode(       CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pName, String pPrimType, JD_Taglist pParams ) throws Exception;

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
  Node copyNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final Node pCopyNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws Exception;


  /** A node has been added (addNode) and now a Inputstream will be supplied as input
   */
  void nodeAdded(     CARS_Main pMain, Node pInterfaceNode, Node pNewNode, InputStream pBody )  throws Exception;

  /** A node has been added (addNode) and saved
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pNewNode
   * @throws java.lang.Exception
   */
  void nodeAddedAndSaved( CARS_Main pMain, Node pInterfaceNode, Node pNewNode )  throws Exception;
  
  /** Store a binary stream, on default the jecars:datafile node type is supported.
   *  If the pNode is an other type the method will stored the data in a Binary property
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pNode the node in which the data will be stored
   * @param pMimeType the mime type of the data if known, otherwise NULL
   * @return true when a update on the node is performed
   * @throws Exception when an error occurs.
   */
  boolean setBodyStream( CARS_Main pMain, Node pInterfaceNode, Node pNode,    InputStream pBody, String pMimeType ) throws Exception;

  /** initGetNodes
   *  Before the getNodes() is called the initGetNodes() is called to provide the plugin with means to
   *  process the init information
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pPathParts
   * @param pCurrentIndex
   * @throws java.lang.Exception
   */
  void initGetNodes(     CARS_Main pMain, Node pInterfaceNode, Node pParentNode,
                          ArrayList<String>pPathParts, int pCurrentIndex ) throws Exception;


  /** getNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws java.lang.Exception
   */
  void getNodes(      CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception;

  /** initHeadNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pPathParts
   * @param pCurrentIndex
   * @throws java.lang.Exception
   */
  void initHeadNodes( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, ArrayList<String>pPathParts, int pCurrentIndex ) throws Exception;


  /** headNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws java.lang.Exception
   */
  void headNodes(     CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception;

  /** Remove a node from the JeCARS repository
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pNode the node which has to be removed
   * @param pParams list of parameters
   */
  void removeNode(    CARS_Main pMain, Node pInterfaceNode, Node pNode, JD_Taglist pParams ) throws Exception;

  /** Set param property implementation
   * A check is performed for "jecars:StateRequest" property in a "jecars:Tool" node type.
   *   this will result in a lock of the node and setStateRequest() in the CARS_ToolInterface.
   */
  Property setParamProperty( CARS_Main pMain, Node pInterfaceNode, Node pNode, String pPropName, String pValue ) throws Exception;
    
  /**
   * setParamPropertyBulk
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pParams
   * @return
   * @throws Exception 
   */
  void setParamPropertyBulk( CARS_Main pMain, Node pInterfaceNode, Node pNode, Map<String, String> pParams ) throws Exception;
}
