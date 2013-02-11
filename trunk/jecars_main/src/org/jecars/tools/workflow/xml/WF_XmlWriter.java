/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.tools.workflow.xml;

import java.io.IOException;
import java.io.PrintWriter;
import javax.jcr.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Convert a JeCARS Workflow node hierarchy to XML
 *
 * @author schulth
 */
public class WF_XmlWriter {
    // ------ XML tag and attribute names ------

    /** name of XML node that stores a jcr node */
    public final static String TAGNAME_NODE = "node";
    /** name of XML node that stores a jcr property */
    public final static String TAGNAME_PROPERTY = "property";
    /** name of XML node that stores a binary jcr property value */
    public final static String TAGNAME_PROPERTYVALUE = "propertyValue";
    /** name of XML attribute that stores a name */
    public final static String ATTRNAME_NAME = "name";
    /** name of XML attribute that stores a value */
    public final static String ATTRNAME_VALUE = "value";
    /** name of XML attribute that indicates that property.isMultiple */
    public final static String ATTRNAME_MULTIPLETRUE = "multipleTrue";
    /** name of XML attribute that identifies the sequence number of the stored value  */
    public final static String ATTRNAME_IVALUE = "i";
    /** name of XML attribute that stores the size of a value */
    public final static String ATTRNAME_SIZE = "size";
    /** name of XML attribute that stores the type of a value */
    public final static String ATTRNAME_TYPE = "type";
    /** name of XML attribute that stores the primary type of a node */
    public final static String ATTRNAME_PRIMARYTYPE = "primaryType";
    
    
    /** JeCARS workflow node type name  */
    public final static String JECARSNODETYPE_WORKFLOW = "jecars:workflow";

    /**
     * return true if node is of type {@link #JECARSNODETYPE_WORKFLOW}
     *
     * @param node
     * @return
     * @throws RepositoryException
     */
    private boolean isJecarsWorkflowNode(javax.jcr.Node node) throws RepositoryException {
        return node != null; // For now: allow any node; ORIG: && node.isNodeType(JECARSNODETYPE_WORKFLOW);
    }

    /**
     * PrintWriter writer writes XML presentation of workflowJcrNode
     *
     * @param writer the writer
     * @param workflowJcrNode node of type {@link #JECARSNODETYPE_WORKFLOW}
     * @throws RepositoryException
     * @throws WF_XmlException XML related error or workflowJcrNode is not of
     * required type
     */
    public void writeXml(PrintWriter writer, javax.jcr.Node workflowJcrNode) throws RepositoryException, WF_XmlException {
        try {
            WF_XmlHelper.writeXmlString(writer, getXmlDom(workflowJcrNode));
        } catch (TransformerException e) {
            throw new WF_XmlException("Failed to generate XML for JeCARS node=" + workflowJcrNode.getPath(), e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new WF_XmlException("Failed to generate XML for JeCARS node=" + workflowJcrNode.getPath(), e);
        }
    }

    /**
     * return XML node tree presentation for JeCARS tree with toplevel node
     * workflowJcrNode
     *
     * @param workflowJcrNode JeCARS node of type {@link #JECARSNODETYPE_WORKFLOW}
     * @return
     * @throws RepositoryException
     */
    public org.w3c.dom.Node getXmlDom(javax.jcr.Node workflowJcrNode) throws RepositoryException, WF_XmlException {
        if (workflowJcrNode == null) {
            throw new WF_XmlException("Internal error: null node");
        } else if (!isJecarsWorkflowNode(workflowJcrNode)) {
            throw new WF_XmlException("Cannot get XML for node=" + workflowJcrNode.getPath() + " - only get workflow XML for nodes of type=" + JECARSNODETYPE_WORKFLOW);
        } else {
            try {
                Document doc = WF_XmlHelper.getDoc();
                org.w3c.dom.Node xmlDom = getXmlDom(doc, workflowJcrNode);
                return xmlDom;
            } catch (ParserConfigurationException e) {
                throw new WF_XmlException("Failed to generate XML for JeCARS node=" + workflowJcrNode.getPath(), e);
            } catch (TransformerFactoryConfigurationError e) {
                throw new WF_XmlException("Failed to generate XML for JeCARS node=" + workflowJcrNode.getPath(), e);
            }
        }
    }

    /**
     * return XML node tree presentation for JeCARS tree with toplevel node
     * jcrNode
     *
     * @param doc
     * @param jcrNode
     * @return
     * @throws RepositoryException
     */
    private org.w3c.dom.Node getXmlDom(Document doc, javax.jcr.Node jcrNode) throws RepositoryException, WF_XmlException {
        boolean skipSystemProperties = true; // TODO: set skipSystemProperties via an option?
        org.w3c.dom.Node result = getXmlNodeFromJcrNode(doc, jcrNode, skipSystemProperties);
        NodeIterator jcrNodeIterator = jcrNode.getNodes();
        while (jcrNodeIterator.hasNext()) {
            org.w3c.dom.Node xmlChild = getXmlDom(doc, jcrNodeIterator.nextNode());
            result.appendChild(xmlChild);
        }
        return result;
    }

    /**
     * return XML node presentation of JeCARS node jcrNode
     *
     * @param xmlDoc
     * @param jcrNode
     * @param skipSystemProperties do not set any system property, such as the jcr:* properties (e.g. jcr:CreatedBy)
     * @return
     * @throws RepositoryException
     */
    private org.w3c.dom.Node getXmlNodeFromJcrNode(Document xmlDoc, javax.jcr.Node jcrNode, boolean skipSystemProperties) throws RepositoryException, WF_XmlException {
        Element xmlNode = xmlDoc.createElement(TAGNAME_NODE);
        xmlNode.setAttribute(ATTRNAME_NAME, jcrNode.getName());
        xmlNode.setAttribute(ATTRNAME_PRIMARYTYPE, jcrNode.getPrimaryNodeType().getName());
        //Note: for the mixin node types see property node with name jcr:mixinTypes 
        PropertyIterator propIterator = jcrNode.getProperties();
        while (propIterator != null && propIterator.hasNext()) {
            Property jcrProperty = propIterator.nextProperty();
            if (!WF_JcrHelper.isSkipSystemProperty(skipSystemProperties, jcrProperty.getName() )) {
                xmlNode.appendChild(getXmlNodeFromJcrProperty(xmlDoc, jcrProperty));
            }
        }
        return xmlNode;
    }

    /**
     * return XML node presentation of JeCARS node property jcrProperty
     *
     * @param xmlDoc
     * @param jcrProperty
     * @return
     * @throws RepositoryException
     */
    private org.w3c.dom.Node getXmlNodeFromJcrProperty(Document xmlDoc, Property jcrProperty) throws RepositoryException, WF_XmlException {
        Element xmlProp = xmlDoc.createElement(TAGNAME_PROPERTY);
        xmlProp.setAttribute(ATTRNAME_NAME, jcrProperty.getName());
        if (jcrProperty.isMultiple()) {
            xmlProp.setAttribute(ATTRNAME_MULTIPLETRUE, ""); // we do not use the value - existence of the attribute is enough
            int ivalue = 0;
            if (jcrProperty.getValues() != null) { // assume that all values have the same type
                xmlProp.setAttribute(ATTRNAME_TYPE, PropertyType.nameFromValue(jcrProperty.getValues()[0].getType()));
            }
            for (Value value : jcrProperty.getValues()) {
                addPropertyValue(xmlDoc, xmlProp, value, ivalue);
                ivalue++;
            }
        } else {
            xmlProp.setAttribute(ATTRNAME_TYPE, PropertyType.nameFromValue(jcrProperty.getValue().getType()));
            addPropertyValue(xmlDoc, xmlProp, jcrProperty.getValue(), -1);
        }
        return xmlProp;
    }

    /**
     * if seqno gte 0
     * then add {@link #TAGNAME_PROPERTYVALUE} node to propertyNode to store jcrValue;
     * else set propertyNode.textContents directly to the jcrValue.stringValue
     * 
     * if jcrValue is a binary {@link #isCdataValue(javax.jcr.Value) }, cdata
     * will be used with base64 encoded contents
     * else the value will be stored in a value attribute
     * 
     * Typical results: 
     * <ul>
     * <li>seqno -1, not binary:<xmp>
     *      <property name="jcr:created" type="Date" value="2012-02-27T14:29:13.246+01:00"/>
     * </xmp></li>
     *
     * <li>seqno gte 0, not binary:<xmp>
     *      <property name="jecars:Actions" n="2" type="String">
     *          <propertyValue i="0" value="read"/>
     *          <propertyValue i="1" value="get_property"/>
     *      </property>
     * </xmp></li>
     *
     * <li> seqno -1, binary: 
     * <xmp>
     *      <property name="jcr:data" size="1014644" type="Binary"><![CDATA[TVqQAAMAA...]]>
     * </xmp></ul></li>
     * @param xmlNode
     * @param jcrValue
     * @param seqno the seqno if there are multiple values. Use -1 if property does not have multiple values.
     *   
     * @throws DOMException
     * @throws IOException
     * @throws RepositoryException
     */
    private void addPropertyValue(Document xmlDoc, Element propertyNode, Value jcrValue, int seqno) throws RepositoryException, WF_XmlException {
        Element propertyValue;
        if (seqno >=0) {
           propertyValue = xmlDoc.createElement(TAGNAME_PROPERTYVALUE);
           propertyValue.setAttribute(ATTRNAME_IVALUE, Integer.toString(seqno));
           propertyNode.appendChild(propertyValue);
        } else {
            // do not create an unneccsary propertyValue XML node, but add value directly to the propertyNode
           propertyValue = propertyNode;
        }
        if (WF_JcrHelper.isBinaryValue(jcrValue.getType())) {
            Binary binary = jcrValue.getBinary();
            long size = binary.getSize();
            propertyValue.setAttribute(ATTRNAME_SIZE, Long.toString(size));
            CDATASection cdataNode = xmlDoc.createCDATASection(WF_JcrHelper.encode(binary));
            propertyValue.appendChild(cdataNode);
        } else {
            propertyValue.setAttribute(ATTRNAME_VALUE, jcrValue.getString());
        }
    }


}