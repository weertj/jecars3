/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Main;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.tools.workflow.xml.WF_XmlReader;

/**
 * Drop (Post) XML file on workflow template node to generate JeCARS workflow
 * node hierachy
 *
 * @author Schultheiss
 */
public class CARS_WorkflowsXMLInterfaceApp extends CARS_DefaultInterface {

    static final protected Logger LOG = Logger.getLogger("org.jecars.tools");

    /**
     * getToBeCheckedInterface
     *
     * @return
     */
    @Override
    protected String getToBeCheckedInterface() {
        return CARS_WorkflowsXMLInterfaceApp.class.getName();
    }

    /**
     * return true if pInterfaceNode is configured for the current class
     * @param pInterfaceNode
     * @return true if pInterfaceNode is configured for the current class
     * @throws RepositoryException
     */
    protected boolean isCorrectInterface(final Node pInterfaceNode) throws RepositoryException {
        return pInterfaceNode.hasProperty(CARS_DefaultMain.DEF_INTERFACECLASS) && // (pInterfaceNode.hasNode("jecars:Config")) &&
            getToBeCheckedInterface().equals(pInterfaceNode.getProperty(CARS_DefaultMain.DEF_INTERFACECLASS).getString());
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
     * @throws Exception 
     */
    @Override
    public Node addNode(CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pName, String pPrimType, JD_Taglist pParams) throws Exception {
      if (pName.endsWith( ".wfxml" )) {
        return super.addNode( pMain, pInterfaceNode, pParentNode, pName.substring( 0, pName.length() - ".wfxml".length() ), "jecars:Workflow", pParams );
      }
      return super.addNode(pMain, pInterfaceNode, pParentNode, pName, pPrimType, pParams);
    }

    
    
    /**
     *  Store a binary stream - our implementation: parse the XML stream, 
     *  and add (TODO? : replace) child nodes by a JeCARS node hierchy
     *  generated from the XML
     *
     * @param pMain
     * @param pInterfaceNode the Node which defines the application source
     * @param pNode
     * @param pBody
     * @param pMimeType
     * @return changed
     * @throws Exception
     */
    @Override
    public boolean setBodyStream(CARS_Main pMain, Node pInterfaceNode, Node pNode, InputStream pBody, String pMimeType) throws Exception {
        if (isCorrectInterface(pInterfaceNode)) {
            if (pBody==null) {
                // encountered when e.g.creating the node, but not yet sending the xml to it
                return false;
            } else {
                if (("text/xml".equals( pMimeType ) || (pNode.getPrimaryNodeType().isNodeType( "jecars:Workflow" )))) {
                  WF_XmlReader reader = new WF_XmlReader();
                  boolean replaceNodes = false; // todo: add a replace option?
                  reader.addJcrNode(pBody, pNode.getParent(), replaceNodes);
                  return true; // node has been changed
                } else {
                  return super.setBodyStream(pMain, pInterfaceNode, pNode, pBody, pMimeType);
                }
            }
        } else {
            // in else branche, otherwise this would store the XML input - that would be a cause for inconsistencies ...
            return super.setBodyStream(pMain, pInterfaceNode, pNode, pBody, pMimeType);
        }
    }
    
    /** Override set param property implementation: avoid setting jcr:mimeType on the interface class
     * @param pMain
     * @param pInterfaceNode
     * @param pNode
     * @param pPropName
     * @param pValue
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public Property setParamProperty( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final String pPropName, final String pValue ) throws Exception {
        if (isCorrectInterface(pInterfaceNode) && "jcr:mimeType".equals(pPropName)) {
            return null;
        } else { 
             return super.setParamProperty(pMain, pInterfaceNode, pNode, pPropName, pValue);                
        }
    }

}
