/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public interface IWFP_Node {

  Node                  getJCRNode();
  
  IWFP_Node             addNode( final String pName, final String pNodeType ) throws WFP_Exception;
  void                  addMixin( final String pN ) throws WFP_Exception;
  String                getPath();
  String                getName() throws WFP_Exception;
  List<IWFP_Node>       getNodes() throws WFP_Exception;
  boolean               hasNode( final String pName ) throws WFP_Exception;
  IWFP_Node             getNode( final String pName ) throws WFP_Exception;
  List<IWFP_Property>   getProperties() throws WFP_Exception;
  boolean               hasProperty( final String pName );
  IWFP_Property         getProperty( final String pName ) throws WFP_Exception;
  IWFP_Node             getResolvedNode() throws WFP_Exception;

  IWFP_Property         setProperty( String pName, String pValue ) throws WFP_Exception;
  IWFP_Property         setProperty( String pName, long pValue   ) throws WFP_Exception;

  void                  remove() throws WFP_Exception;
  void                  rename( final String pName, final String pTitle ) throws WFP_Exception;

  Object                getNodeObject() throws WFP_Exception;
  Object                makeLocalCopy() throws WFP_Exception;
  
  void                  save() throws WFP_Exception;
 
  List<Node>      getParameterNodes() throws RepositoryException;
  IWFP_Parameter  getParameter( final String pName ) throws WFP_Exception;
  IWFP_Parameter  addParameter( final String pName ) throws WFP_Exception;

  
}
