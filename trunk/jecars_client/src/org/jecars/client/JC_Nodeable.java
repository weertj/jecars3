/*
 * Copyright 2008-2011 NLR - National Aerospace Laboratory
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

package org.jecars.client;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import org.jecars.client.nt.JC_PermissionNode;

/**
 * JC_Nodeable
 *
 * @version $Id: JC_Nodeable.java,v 1.27 2009/06/23 22:38:59 weertj Exp $
 */
public interface JC_Nodeable extends JC_Itemable, JC_WebDAVable {

  String getID();
  void   setID( final String pID );

  /** getSelfLink
   * Get the complete link (including base URL) to itself or null when unknown.
   * 
   * @return
   */
  String getSelfLink();    
    
  /** getClient
   * @return
   */
  JC_Clientable getClient();
  
  /** getParent
   * @return
   */
  JC_Nodeable   getParent() throws JC_Exception;
  
  /** addBatch
   *
   * @param pInput
   * @throws org.jecars.client.JC_Exception
   */
  void                       addBatch(    InputStream pInput ) throws JC_Exception;

  /** addPermissionNode
   * Created information is not saved
   *
   * @param pName
   * @param pPrincipal
   * @param pRights
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_PermissionNode addPermissionNode( final String pName, JC_Nodeable pPrincipal, Collection<String>pRights ) throws JC_Exception;

  /** getOrAddNode
   * Get the node or add it, if it isn't there
   * @param pName
   * @param pNodeType
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable                getOrAddNode(     final String pName, final String pNodeType ) throws JC_Exception;

  /** addNodes
   * 
   * @param pName
   * @param pNodeType
   * @param pFolderNodeType
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable                addNodes(    final String pName, final String pNodeType, final String pFolderNodeType ) throws JC_Exception;

  /** addNode
   * 
   * @param pName
   * @param pNodeType
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable                addNode(    final String pName, final String pNodeType ) throws JC_Exception;

  /** addNode create a new node using other node as reference (copy).
   *  http://jecars.sourceforge.net/jecars_doc.html#Creating_an_object_using_copy
   * 
   * @param pName
   * @param pNodeType
   * @param pLinkVia_URL, may contain URL or Path reference to a JeCARS node
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable                addNode(     final String pName, final String pNodeType, final String pLinkVia_URL ) throws JC_Exception;

  /** Add a complete node path, which means that the pPath is it path which will be created (if it isn't there),
   *  Every node type will be pFolderNodeType, except for the last node, which will be of type pNodeType
   * 
   * @param pPath
   * @param pNodeType
   * @param pFolderNodeType
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable                addNodePath( final String pPath, final String pNodeType, final String pFolderNodeType ) throws JC_Exception;

  /** addMixin adds a mixin nodetype to the current nodetype.
   * 
   * @param pMixin
   * @throws org.jecars.client.JC_Exception
   */
  void addMixin( final String pMixin ) throws JC_Exception;

  /**  removeMixin removes a mixin nodetype from the current nodetype.
   *
   * @param pMixin
   * @throws JC_Exception
   */
  void removeMixin( final String pMixin ) throws JC_Exception;

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final boolean pValue ) throws JC_Exception;

  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final long pValue ) throws JC_Exception;

  /** setProperty
   * 
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable setProperty( final String pName, final double pValue ) throws JC_Exception;
  
  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final String pValue ) throws JC_Exception;

  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final Calendar pValue ) throws JC_Exception;

  /** setProperty
   * 
   * @param pName
   * @param pNode
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final JC_Nodeable pNode ) throws JC_Exception;

  /** setProperty
   * @param pName
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final String pName, final JC_Streamable pValue ) throws JC_Exception;

  /** setProperty
   * @param pValue
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setProperty( final JC_Streamable pValue ) throws JC_Exception;

  /** replaceMultiValueProperty
   * @param pName
   * @param values
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            replaceMultiValueProperty( final String pName, final Collection<String>  pValues) throws JC_Exception;

  /** replaceMultiValuePropertyL
   * 
   * @param pName
   * @param pValues
   * @return
   * @throws JC_Exception 
   */
  JC_Propertyable            replaceMultiValuePropertyL( final String pName, final Collection<Long>  pValues) throws JC_Exception;


  
  /** setMultiValueProperty
   * @param pName
   * @param values
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setMultiValueProperty( final String pName, final Collection<String>  pValues) throws JC_Exception;

  /** setMultiValuePropertyD
   * 
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setMultiValuePropertyD( final String pName, final Collection<Double>  pValues) throws JC_Exception;

  /** setMultiValuePropertyL
   *
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setMultiValuePropertyL( final String pName, final Collection<Long>    pValues) throws JC_Exception;

  /** setMultiValuePropertyB
   *
   * @param pName
   * @param pValues
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            setMultiValuePropertyB( final String pName, final Collection<Boolean> pValues) throws JC_Exception;
  
  /** getNode retrieve the node from the JeCARS server
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable                getNode( final String pName )     throws JC_Exception;

  /** getNode
   * 
   * @param pName
   * @param pRetrieve if true then the node actually retrieved from the JeCARS server
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable                getNode( final String pName, final boolean pRetrieve )     throws JC_Exception;
    
  /** getNode
   * 
   * @param pName
   * @param pParams
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable getNode( final String pName, JC_Params pParams ) throws JC_Exception;

  /** getNode
   * 
   * @param pName
   * @param pRetrieve if true then the node actually retrieved from the JeCARS server
   * @param pParams
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Nodeable getNode( final String pName, final boolean pRetrieve, JC_Params pParams ) throws JC_Exception;

  /** getResolvedNode
   *
   * @param pName
   * @param pParams
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable getResolvedNode( final String pName, JC_Params pParams ) throws JC_Exception;

  /** getResolvedNode
   * 
   * @param pName
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable getResolvedNode( final String pName ) throws JC_Exception;
  
  /** resolve
   * Resolve the node using the jecars:Link property until a node is encountered without this property
   * 
   * @return
   * @throws JC_Exception
   */
  JC_Nodeable resolve() throws JC_Exception;

  /** getNumberOfChildNodes
   * 
   * @return
   */
  int getNumberOfChildNodes() throws JC_Exception;

  /** getLongPolling
   * 
   * @return
   * @throws JC_Exception 
   */
  Map<String, String> getLongPolling() throws JC_Exception;
  
  /** getNodeList
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  List<JC_Nodeable> getNodeList() throws JC_Exception;

  /** getNodes
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  Collection<JC_Nodeable>    getNodes()                        throws JC_Exception;

  /** getNodes
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  Collection<? extends JC_Nodeable>    getNodesExt()           throws JC_Exception;
  
  /** getNodes
   * 
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  Collection<JC_Nodeable> getNodes( JC_Params pParams, JC_Filter pFilter, JC_Query pQuery )       throws JC_Exception;

  /** getNodesExt
   * 
   * @param pParams
   * @param pFilter
   * @param pQuery
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  Collection<? extends JC_Nodeable> getNodesExt( JC_Params pParams, JC_Filter pFilter, JC_Query pQuery ) throws JC_Exception;

  /** getPropertyStream
   * 
   * @param pName
   * @param pProps
   * @param pOffset =-1 not used
   * @param pLength =-1 not used
   * @return
   * @throws JC_Exception 
   */
  JC_Streamable getPropertyStream( final String pName, final EnumSet<JC_StreamProp> pProps, long pOffset, long pLength ) throws JC_Exception;

  /** getProperty
   * @param pName
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            getProperty( final String pName ) throws JC_Exception;

  /** getProperty
   * 
   * @param pName
   * @param pRetrieve
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Propertyable            getProperty( final String pName, final boolean pRetrieve ) throws JC_Exception;

  /** getProperty
   * 
   * @param pName
   * @param pParams
   * @return
   * @throws JC_Exception
   */
  JC_Propertyable            getProperty( final String pName, final JC_Params pParams ) throws JC_Exception;

  /** getProperty
   *
   * @param pName
   * @param pRetrieve
   * @param pParams
   * @return
   * @throws JC_Exception
   */
  JC_Propertyable            getProperty( final String pName, final boolean pRetrieve, final JC_Params pParams ) throws JC_Exception;

  /** hasProperty
   * @param pName
   * @return
   */
  boolean hasProperty( String pName ) throws JC_Exception;

  /** hasProperty
   * 
   * @param pName
   * @param pRetrieve
   * @return
   * @throws JC_Exception 
   */
  boolean hasProperty( String pName, final boolean pRetrieve ) throws JC_Exception;
  
  /** hasNode
   * When the node isn't found in the cache a HEAD call will be performed to check
   * if the object is available at the server.
   * 
   * @param pName
   * @return
   */
  boolean hasNode( final String pName ) throws JC_Exception;

  /** hasNode
   * 
   * @param pName
   * @param pGetNodes if true then a getnodes() is performed (before the hasNode check) when the cache is empty.
   * @param pDoHead if true then when the node isn't found in the cache a HEAD call
   *                will be performed to check if the object is available at the server.
   * @return
   * @throws JC_Exception
   */
  boolean hasNode( final String pName, final boolean pGetNodes, final boolean pDoHead ) throws JC_Exception;

  /** hasNodeNameCheck, the same as hasNode(...) but only a getName() check is done, not a ATOM_TITLE check
   *
   * @param pName
   * @param pGetNodes if true then a getnodes() is performed (before the hasNode check) when the cache is empty.
   * @param pDoHead if true then when the node isn't found in the cache a HEAD call
   *                will be performed to check if the object is available at the server.
   * @return
   * @throws JC_Exception
   */
  boolean hasNodeNameCheck( final String pName, final boolean pGetNodes, final boolean pDoHead ) throws JC_Exception;


  /** getProperties
   * @return
   */
  Collection<JC_Propertyable>getProperties() throws JC_Exception;

  /** moveNode
   * Move this node, only this node object is changed
   * 
   * @param pNewName
   * @return true if the operation was succesfull
   * @throws JC_Exception
   */
  boolean moveNode( final String pNewName ) throws JC_Exception;

  /** removeChildNode
   *
   * @param pName
   * @throws JC_Exception
   */
  void removeChildNode( final String pName ) throws JC_Exception;

  /** removeNode
   * @throws java.lang.Exception
   */
  void removeNode() throws JC_Exception;

  /** removeNodeForced
   * 
   * @throws JC_Exception
   */
  void removeNodeForced() throws JC_Exception;
  
  /** getRights
   * 
   * @param pPrincipal
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  JC_Rights getRights( String pPrincipal ) throws JC_Exception;

  /** getPath
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  String getPath() throws JC_Exception;
  
  String getMimeType();

  /** getPath_JC, creates(!) a JC_Path and returns it
   * @return
   */
  JC_Path getPath_JC();
  
  /** getLastAccessedDate
   * @return get the date of the last access of this node (read or write)
   */
  public Calendar getLastAccessedDate();
  
  /** getModifiedDate
   * @return get the date of the last modification of this node (write)
   */
  public Calendar getModifiedDate();
  
  /** getExpireDate
   * @return The date after which this node can(!) be removed
   */  
  public Calendar getExpireDate();
  
  /** setExpireDate
   *
   * @param pPlusMinutes
   * @throws JC_Exception
   */
  public void setExpireDate( final int pPlusMinutes ) throws JC_Exception;

  /** setTitle
   * 
   * @param pT
   * @return
   * @throws JC_Exception 
   */
  JC_Propertyable setTitle( final String pT ) throws JC_Exception;


  public String getTitle();
  
  public String getBody();
  
  /** setExpireDate
   * @param pDate
   */
  public void setExpireDate( Calendar pDate ) throws JC_Exception;

  /** getNodeType
   * 
   * @return
   */
  public String getNodeType();
 
  /** morphToNodeType
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_DefaultNode morphToNodeType() throws JC_Exception;
  
  /** getChildNodeDefs
   *  get the child node definition allowed for creation for this node
   * 
   * @return collection of nodetype string names
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<String>getChildNodeDefs() throws JC_Exception;
  
  /** getUpdateAsHead
   * 
   * get the update for this node as an HTTP HEAD call
   * 
   * @return true if the node has been updated, false if it is still the same
   * @throws org.jecars.client.JC_Exception if an error occurs
   */
  public boolean getUpdateAsHead() throws JC_Exception;

  JC_Streamable exportNode( boolean pDeep ) throws JC_Exception;

  /** importNode
   * 
   * @param pStream
   */
  void importNode( JC_Streamable pStream ) throws JC_Exception;
  
  /** setNodeType
   *
   * @param pNodeType
   * @return
   * @throws JC_Exception
   * @throws UnsupportedEncodingException
   */
  JC_Nodeable setNodeType( final String pNodeType ) throws JC_Exception;


  /** isParent -- Check if this node is parent of the given child node
   * 
   * @param pChildNode
   * @return
   * @throws JC_Exception 
   */
  boolean isParent( final JC_Nodeable pChildNode ) throws JC_Exception;

  
  /** isDataFile
   * 
   * @return
   */
  boolean isDataFile();

  /** isInErrorState
   * 
   * @return
   */
  boolean isInErrorState();

  /** isNull
   *
   * @return
   */
  boolean isNull();

  /** isDestroyed
   * 
   * @return 
   */
  boolean isDestroyed();

  
  /** isSyncronized
   * 
   * @return
   */
  boolean isSynchronized();

  /** getVersionLabels
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  List<String> getVersionLabels() throws JC_Exception;

  /** commitVersion
   * 
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  void commitVersion( String pLabel ) throws JC_Exception;

  /** checkout
   *
   * @throws org.jecars.client.JC_Exception
   */
  void checkout() throws JC_Exception;

  /** restoreVersion
   * 
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  void restoreVersion( String pLabel ) throws JC_Exception;

  /** removeVersionByLabel
   * 
   * @param pLabel
   * @throws org.jecars.client.JC_Exception
   */
  void removeVersionByLabel( String pLabel ) throws JC_Exception;

  /** Does the child nodes already got loaded
   * 
   * @return 
   */
  boolean gotChildNodes();
  
}
