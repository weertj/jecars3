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
import java.io.InputStream;
import java.util.ArrayList;
import javax.jcr.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import static org.jecars.tools.workflow.xml.WF_XmlWriter.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Read XML, and convert it to a JeCARS Workflow node hierarchy
 *
 * @author schulth
 */
public class WF_XmlReader {

    /**
     * add a jcr node to jcrParentNode using the specification of the XML
     * inputStream
     *
     * @param inputStream XML input stream
     * @param jcrParentNode
     * @param replace if any jcr node already exists and replace, then this jcr
     * node is replaced.
     * @return created toplevel node
     * @throws RepositoryException
     * @throws WF_XmlException e.g. an XML parsing error, or if (!replace) and a
     * node as specified in the XML already exists
     */
    public javax.jcr.Node addJcrNode(InputStream inputStream, javax.jcr.Node jcrParentNode, boolean replace) throws RepositoryException, WF_XmlException {
        try {
            Document doc = WF_XmlHelper.getDocBuilder().parse(inputStream);
            return addJcrNode(doc.getDocumentElement(), jcrParentNode, replace);
        } catch (IOException ioe) {
            throw new WF_XmlException("Failed to create JCR nodes from XML", ioe);
        } catch (SAXException se) {
            throw new WF_XmlException("Failed to create JCR nodes from XML", se);
        } catch (ParserConfigurationException pce) {
            throw new WF_XmlException("Failed to create JCR nodes from XML", pce);
        }
    }

    /**
     * add a jcr node to jcrParentNode using the contents of the xmlElement DOM
     *
     * @param xmlElement
     * @param jcrParentNode
     * @param replace if a jcr node with name as specified in xmlElement already
     * exists, replace it
     * @return added toplevel jcr node
     * @throws RepositoryException
     * @throws WF_XmlException when encountering problems in xmlElement, such as
     * a missing or an empty name attribute
     */
    public javax.jcr.Node addJcrNode(Element xmlElement, javax.jcr.Node jcrParentNode, boolean replace) throws RepositoryException, WF_XmlException {
        String name = getNameAttributeValue(xmlElement);
        if (jcrParentNode.hasNode(name)) {
            if (replace) {
                jcrParentNode.getNode(name).remove();
            } else {
//                throw new WF_XmlException(name + " already existing in parent=" + jcrParentNode.getPath());
            }
        }
        return addJcrNode(xmlElement, jcrParentNode);
    }

    /**
     * add a jcr node to jcrParentNode using the contents of the xmlNode DOM
     *
     * @param xmlNode
     * @param jcrParentNode
     * @return added toplevel jcr node
     */
    public javax.jcr.Node addJcrNode(Element xmlElement, javax.jcr.Node jcrParentNode) throws RepositoryException, WF_XmlException {
        String nodeName = xmlElement.getAttribute(ATTRNAME_NAME);
        if (nodeName == null || nodeName.isEmpty()) {
            throw new WF_XmlException("Node " + xmlElement.getNodeName() + ": Missing or empty mandatory attribute " + ATTRNAME_NAME);
        }
        String nodeType = xmlElement.getAttribute(ATTRNAME_PRIMARYTYPE);
        if (nodeType == null || nodeType.isEmpty()) {
            throw new WF_XmlException("Node " + xmlElement.getNodeName() + ": Missing or empty mandatory attribute " + ATTRNAME_TYPE);
        }
        // System.out.println("qqlq addJcrNode parent="+jcrParentNode.getPath()+" nodeName="+nodeName+", type="+nodeType);
        javax.jcr.Node result;
        if (jcrParentNode.hasNode(nodeName)) {
            // then this node was probably autocreated - reuse the autocreated node
            result = jcrParentNode.getNode(nodeName);
        } else {
            result = jcrParentNode.addNode(nodeName, nodeType);
        }
                
        addMixin(xmlElement, result); // 1st add mixins to avoid problems when setting the properties
        boolean skipSystemProps = true;
        setJcrProperties(xmlElement, result, skipSystemProps);
        jcrParentNode.save(); // must save parent instead of result
        for (Element child: WF_XmlHelper.getChildrenByTagName(xmlElement, TAGNAME_NODE)) {
            addJcrNode(child, result);
        }
        return result;
    }

    /**
     * set properties of jcrNode using the {@link #TAGNAME_PROPERTY} children of
     * xmlElement
     *
     * @param xmlElement
     * @param jcrNode
     * @param skipSystemProperties do not set any system property, such as the
     * jcr:* properties (e.g. jcr:CreatedBy) (so only jecars:* properties, etc)
     */
    private void setJcrProperties(Element xmlElement, javax.jcr.Node jcrNode, boolean skipSystemProperties) throws RepositoryException, WF_XmlException {
        for (Element xmlProperty: WF_XmlHelper.getChildrenByTagName(xmlElement, TAGNAME_PROPERTY)) {
            String propName = getNameAttributeValue(xmlProperty);
            if (!WF_JcrHelper.isSkipSystemProperty(skipSystemProperties, propName)) {
                setJcrProperty(xmlProperty, jcrNode);
            }
        }
    }
    
    /** addMixin to jcrNode
     * 
     * @param xmlElement the mixin names are stored in the {@link #TAGNAME_PROPERTY} 
     * children that have name "jcr:mixinTypes"
     * @param jcrNode
     * @throws RepositoryException
     * @throws WF_XmlException 
     */
    private void addMixin(Element xmlElement, javax.jcr.Node jcrNode) throws RepositoryException, WF_XmlException {
        // System.out.println("qqlq addMixin using xmlElement="+WF_XmlHelper.getPath(xmlElement));
        for (Element xmlProperty: WF_XmlHelper.getChildrenByTagName(xmlElement, TAGNAME_PROPERTY)) {
            String name = getNameAttributeValue(xmlProperty);
            if (xmlProperty.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE &&  "jcr:mixinTypes".equals(name)) {
                // cannot set this property via a setProperty ...
                int type = getJcrPropertyType(xmlProperty);
                Value[] values = getMultipleValues(xmlProperty, type);
                // System.out.println("qqlq addMixin "+jcrNode.getPath()+" using data of xml path="+WF_XmlHelper.getPath(xmlProperty));
                for (Value mixin : values) {
                    // System.out.println("qqlq addMixin "+jcrNode.getPath()+" mixin="+mixin.getString());
                    jcrNode.addMixin(mixin.getString());
                }
            }
        }
    }

    /**
     * set jcrNode's property using xmlProperty's contents
     *
     * @param xmlProperty
     * @param jcrNode
     * @throws RepositoryException
     * @throws WF_XmlException
     */
    private void setJcrProperty(Element xmlProperty, javax.jcr.Node jcrNode) throws RepositoryException, WF_XmlException {
        String name = getNameAttributeValue(xmlProperty);
        int type = getJcrPropertyType(xmlProperty);
        // note: jcr:mixinTypes are set via setMixin(...)
        if (!"jcr:mixinTypes".equals(name)) {
            if (xmlProperty.hasAttribute(ATTRNAME_MULTIPLETRUE)) {
                Value[] values = getMultipleValues(xmlProperty, type);
                // System.out.println("qqqlq setJcrProperty "+jcrNode.getPath()+" name="+name+", type="+type+" values.length="+values.length);
                jcrNode.setProperty(name, values, type);
            } else {
                Value value = getSingleValue(xmlProperty, type);
                // System.out.println("qqqlq setJcrProperty "+jcrNode.getPath()+" name="+name+", type="+type);
                jcrNode.setProperty(name, value, type);
            }
        }
    }
    
    
    /** return the Value[] as stored in xmlProperty
     * @param xmlProperty an XML node with multiple values stored in TAGNAME_PROPERTYVALUE child nodes
     * @param type JCR type as obtained through {@link #getJcrPropertyType(org.w3c.dom.Element) }
     * @return the Value[] as stored in xmlProperty's TAGNAME_PROPERTYVALUE child nodes
     * @throws RepositoryException
     * @throws WF_XmlException 
     */
    private Value[] getMultipleValues(Element xmlProperty, int type) throws RepositoryException, WF_XmlException {
        ArrayList<Value> result = new ArrayList<Value>();
        for (Element propValue: WF_XmlHelper.getChildrenByTagName(xmlProperty, TAGNAME_PROPERTYVALUE)) {
            if (propValue.hasAttribute(ATTRNAME_VALUE)) {
                String value = propValue.getAttribute(ATTRNAME_VALUE);
                result.add(ValueFactoryImpl.getInstance().createValue(value, type));
            } else {
                // value is in textContent block (might be an encoded binary)
                if (isExpectBinaryValue(propValue)) {
                    result.add(ValueFactoryImpl.getInstance().createValue(getBinaryValue(propValue)));
                } else {
                    String value = propValue.getTextContent();
                    result.add(ValueFactoryImpl.getInstance().createValue(value, type));
                }
            }
        }
        return result.toArray(new Value[0]);
    }
    
    /** return the single value as stored in xmlProperty
     * 
     * @param xmlProperty attribute {@link #ATTRNAME_VALUE} is used, 
     * or if attribute is not existing the contents of xmlProperty are used
     * @param JCR type as obtained through {@link #getJcrPropertyType(org.w3c.dom.Element) }
     * @return
     * @throws RepositoryException
     * @throws WF_XmlException 
     */
    private Value getSingleValue(Element xmlProperty, int type) throws RepositoryException, WF_XmlException {
        Value result;
        if (xmlProperty.hasAttribute(ATTRNAME_VALUE)) {
            result = ValueFactoryImpl.getInstance().createValue(xmlProperty.getAttribute(ATTRNAME_VALUE), type);
        } else {
            // value is in textContent block (might be an encoded binary)
            if (isExpectBinaryValue(xmlProperty)) {
                result = ValueFactoryImpl.getInstance().createValue(getBinaryValue(xmlProperty));
            } else {
                result = ValueFactoryImpl.getInstance().createValue(xmlProperty.getTextContent(), type);
            }
        }
        return result;
    }

    /**
     * return decoded propertyValueElement's textContent
     *
     * @param propertyValueElement
     * @return
     * @throws RepositoryException
     * @throws WF_XmlException
     */
    private Binary getBinaryValue(Element propertyValueElement) throws RepositoryException, WF_XmlException {
        String encodedValue = propertyValueElement.getTextContent();
        return WF_JcrHelper.decode(encodedValue);
    }

    /**
     * return true if xmlProperty's {@link #ATTRNAME_TYPE} attribute value
     * indicates that it is a binary value
     *
     * @param xmlProperty
     * @return
     * @throws WF_XmlException
     */
    private boolean isExpectBinaryValue(Element xmlProperty) throws WF_XmlException {
        int type = getJcrPropertyType(xmlProperty);
        return WF_JcrHelper.isBinaryValue(type);
    }

    /**
     * return the jcr property type as stored in xmlProperty's {@link #ATTRNAME_TYPE}
     * attribute
     *
     * @param xmlProperty
     * @return
     * @throws WF_XmlException
     */
    private int getJcrPropertyType(Element xmlProperty) throws WF_XmlException {
        String typeString = xmlProperty.getAttribute(ATTRNAME_TYPE);
        if (typeString == null || typeString.trim().isEmpty()) {
            throw new WF_XmlException("type attribute is mandatory for " + xmlProperty.getTagName());
        }
        try {
            return PropertyType.valueFromName(typeString);
        } catch (IllegalArgumentException e) {
            throw new WF_XmlException(ATTRNAME_TYPE + " value=" + typeString + " is not a valid type value");
        }
    }

    /**
     * return the {@link #ATTRNAME_NAME} attribute value of element.
     *
     * @param element
     * @return found value of name attribute
     * @throws WF_XmlException if attribute not found or isEmpty
     */
    private String getNameAttributeValue(Element element) throws WF_XmlException {
        if (element == null) {
            throw new WF_XmlException("null element");
        }
        String result = element.getAttribute(ATTRNAME_NAME);
        if (result == null || result.trim().isEmpty()) {
            throw new WF_XmlException("XML tag " + element.getTagName() + " has no value for mandatory attribute " + ATTRNAME_NAME);
        }
        return result;
    }
}
