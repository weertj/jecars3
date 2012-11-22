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
import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jecars.support.BASE64Decoder;
import org.jecars.support.BASE64Encoder;
import org.w3c.dom.DOMException;

/**
 * Contains generic JCR methods as used by WF_XmlReader / WF_XmlWriter
 *
 * @author schulth
 */
public class WF_JcrHelper {

    /**
     * return true if type is a type that must be stored as a binary value
     *
     * @param type a PropertyType type
     * @return
     */
    public static boolean isBinaryValue(int type) {
        return type == PropertyType.BINARY;
    }

    /** encode contents of binary using {@link BASE64Encoder}
     * @param binary
     * @return
     * @throws RepositoryException
     * @throws WF_XmlException 
     */
    public static String encode(Binary binary) throws RepositoryException, WF_XmlException {
        StringBuilder data = new StringBuilder();
        byte[] bytes = new byte[8192];
        long size = binary.getSize();
        long totalRead = 0;
        while (totalRead < size) {
            try {
                int nread = binary.read(bytes, totalRead); // start in binary at position totalRead, and read max bytes.length bytes
                if (nread > 0) {
                    data.append(BASE64Encoder.encodeBuffer(bytes, 0, nread));
                    totalRead = totalRead + nread;
                } else {
                    // strange no read data, but totalRead < size?
                    throw new WF_XmlException("Unexpected end of binary");
                }
            } catch (DOMException e) {
                throw new WF_XmlException(e);
            } catch (IOException e) {
                throw new WF_XmlException(e);
            }
        }
        return data.toString();
    }
    
   /** decode contents of a String that was encoded using {@link #encode(javax.jcr.Binary) }
     * @param binary
     * @return
     * @throws RepositoryException
     * @throws WF_XmlException 
     */
    public static Binary decode(String encoded) throws RepositoryException, WF_XmlException {
        Binary result = new BinaryImpl(BASE64Decoder.decodeBuffer(encoded));
        return result;
    }
    
    /** return true if property with name propertyName should be skipped
     * @param skipSystemProperty whether to skip system properties such as the jcr:* properties (e.g. jcr:CreatedBy)
     *    Note: some system properties cannot be skipped because we need the info in order to create the JeCARS nodes from the XML, such as jcr:mixinTypes.
     * @param propertyName property name including nameSpace, e.g. jcr:createdBy, or jecars:Modified
     * @return true if propertyName indicates a system property, and skipSystemProperty
     */
    public static boolean isSkipSystemProperty(boolean skipSystemProperty, String propertyName) {
        boolean isSystemProperty = propertyName!=null && 
                (  (propertyName.startsWith("jcr:") 
                      && (!propertyName.equals("jcr:mixinTypes")) // always need jcr:mixinTypes info
                      && (!propertyName.equals("jcr:data"))       // always need jcr:data
                      && (!propertyName.equals("jcr:mimeType"))   // when jcr:data present, also need jcr:mimeType
                    ) 
                   ||  propertyName.equals("jecars:Modified")
                );
        return skipSystemProperty && isSystemProperty;
    }

}
