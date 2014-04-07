/*
 * Copyright 2007-2012 NLR - National Aerospace Laboratory
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
package org.jecars;

import java.io.InputStream;
import javax.jcr.*;
import nl.msd.jdots.JD_Taglist;

/**
 * CARS_Main
 *
 */
public interface CARS_Main {
  
  
  void destroy();
  
  void addContext( CARS_ActionContext pContext );

  /** Get the default action context
   * 
   * @return
   */
  CARS_ActionContext getContext();
  CARS_ActionContext getContext( int pNo );
  void               removeContext( CARS_ActionContext pContext );
  
  /** Add a node reference to a multivalued property
   * @param pNode Property of this node
   * @param pProperty The name of the property
   * @param pReference The node reference
   * @return the multivalued property
   * @throws Exception when an error occurs
   */
  Property addReference( Node pNode, String pProperty, Node pReference ) throws Exception;
  /** addPermission
   * @param pParentNode
   * @param pGroupname
   * @param pUsername
   * @param pRights
   * @return
   * @throws java.lang.Exception
   */
  Node addPermission( Node pParentNode, String pGroupname, String pUsername, String pRights ) throws Exception;

  Node addNode(    String pFullPath, JD_Taglist pParamsTL, InputStream pBody, String pBodyContentType ) throws Exception;
  Node updateNode( String pFullPath, JD_Taglist pParamsTL, InputStream pBody, String pBodyContentType ) throws Exception;
  Node getRoot() throws Exception;
  Node getNode( String pFullPath, JD_Taglist pTags, boolean pAsHead ) throws Exception;
  Node getNodeDirect( String pFullPath ) throws Exception;
  Node getNodeHighestAvailable( String pFullPath ) throws Exception;
  void removeNode( String pFullPath, JD_Taglist pTags ) throws Exception;

  Property setParamProperty( Node pNode, String pPropName, String pValue ) throws Exception;

  /** Set the jecars:Id property on the given node
   * @param pNode The node
   * @throws java.lang.Exception when an exception occurs
   */
  void setId( Node pNode ) throws Exception;
  /** Retrieve the jecars:Id property from the node
   * @param pNode The node
   * @return id
   * @throws java.lang.Exception
   */
  long getId( Node pNode ) throws Exception;
  
  Node getLoginUser();
//  public Node getUserSources() throws Exception;
//  public Node getUserSource(         String pUserSource ) throws Exception;
//  public void synchronizeUserSource( String pUserSource ) throws Exception;

//  public Node getGroupSources() throws Exception;
//  public Node getGroupSource(         String pGroupSource ) throws Exception;
//  public void synchronizeGroupSource( String pGroupSource ) throws Exception;
 
  Node getUsers()  throws Exception;
  Node getGroups() throws Exception;
  Node addUser(  String pID, char[] pPassword, String pUserNodeType ) throws Exception;
  Node addUser(  String pID, char[] pPassword ) throws Exception;
  Node addGroup( String pID, String pFullname, String pGroupNodeType ) throws Exception;
  Node addGroup( String pID, String pFullname ) throws Exception;
 
  void      setCurrentViewNode( Node pNode );
  Node      getCurrentViewNode();
  void      setCurrentViewProperty( final Property pProp );
  Property  getCurrentViewProperty();

  Session getSession();

  CARS_Factory getFactory();

  boolean mayChangeNode( Node pNode ) throws RepositoryException;

  
}
