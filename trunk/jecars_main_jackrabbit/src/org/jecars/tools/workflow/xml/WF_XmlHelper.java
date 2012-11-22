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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper methods for reading and writing XML
 * @author schulth
 */
public class WF_XmlHelper {
    
    /** write XML string presentation of node using writer
     * 
     * @param writer to be used to write the XML string output
     * @param node the XML node to be translated to an XML string
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError 
     */
    public static void writeXmlString(PrintWriter writer, Node node) throws TransformerException, TransformerFactoryConfigurationError {
        //set up a transformer
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        //trans.setOutputProperty(OutputKeys.STANDALONE, "yes");

        //Write XML to response
        try {
            StreamResult result = new StreamResult(writer);
            DOMSource source = new DOMSource(node);
            trans.transform(source, result);
        } finally {
            writer.close();
        }
    }

    /** return XML string presentation of DOM with toplevel doc
     * Note: uses {@link #writeDom(java.io.PrintWriter, org.w3c.dom.Node) }
     * @param doc
     * @return XML string presentation
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError 
     */
    public static String getXMLString(Node doc)  throws TransformerException, TransformerFactoryConfigurationError {
        if (doc==null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        writeXmlString(writer, doc);
        return baos.toString();
    }

    /** Factory for Document. To be used when creating an XML dom tree;
     * its typical usage is:
     * Document doc = Wf_XmlHelper.getDoc();
     * Element root = doc.createElement(...);
     * @return
     * @throws ParserConfigurationException 
     */
    public static Document getDoc() throws ParserConfigurationException {
        return getDocBuilder().newDocument();
    }    
    /** Factory for DocumentBuilder. To be used when parsing an XML document, e.g.:
     * dom = getDocBuilder().parse(...)
     * @return
     * @throws ParserConfigurationException 
     */
    public static DocumentBuilder getDocBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        return dbfac.newDocumentBuilder();
    }    
    
    /** for test and debug purposes return the path (i.e. all the parents) to this element 
     * @param element
     * @return 
     */
    public static String getPath(Element element) {
        StringBuilder result = new StringBuilder();
        Node parent = element;
        while (parent!=null) {
            if (parent.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element elemParent = (Element) parent;
                if (elemParent.getAttribute("name") != null && !elemParent.getAttribute("name").isEmpty()) {
                    result.insert(0, "[name=" + elemParent.getAttribute("name") + "]");
                } else {
                    result.insert(0, "[noname]");
                }
            }
            result.insert(0, "/"+parent.getNodeName());
            parent = parent.getParentNode();
        }
        return result.toString();
    }
    
    /** return Element children by tag name
     * @param parent
     * @param name the tag name
     * @return children of parent that have tagname name
     */
    public static List<Element> getChildrenByTagName(Element parent, String name) {
        List<Element> result = new ArrayList<Element> ();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
                result.add((Element) child);
            }
        }
        return result;
    }

}
