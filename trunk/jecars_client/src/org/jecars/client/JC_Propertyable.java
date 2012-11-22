/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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


/**
 * JC_Propertyable
 *
 * @version $Id: JC_Propertyable.java,v 1.10 2009/03/30 14:13:25 weertj Exp $
 */
public interface JC_Propertyable extends JC_Itemable {

  /** isMulti
   * 
   * @return
   */
  boolean isMulti();
 
  /** getNode
   * @return
   */
  JC_Nodeable getNode();
  
  /** getValue
   * @return
   */
  Object getValue() throws JC_Exception;

  /** getValueString
   * 
   * @return
   */
  String getValueString();

  /** decodeStringToStream
   * 
   */
  void decodeStringToStream();

  
  /** getStream
   * @return
   */
  JC_Streamable getStream() throws JC_Exception;
  
  /** getValueAs
   * @param pObjectClass
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  Object getValueAs( Class pObjectClass ) throws JC_Exception;

  /** getValueAsBoolean
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  boolean getValueAsBoolean() throws JC_Exception;

  /** getValueAsLong
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  long getValueAsLong() throws JC_Exception;

  /** getValueAsDouble
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  double getValueAsDouble() throws JC_Exception;


  /** getValueAsStream
   * @param pListener
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  InputStream getValueAsStream( JC_StreamListener pListener ) throws JC_Exception;

  /** getPropertyType
   * 
   * @return
   */
  JC_PropertyType getPropertyType() throws JC_Exception;  

  /** setValue
   * @param pValue
   */
  void setValue( Object pValue ) throws JC_Exception;  
  
  /** setValue
   * @param pValue
   */
  void setValue( String pValue ) throws JC_Exception;

  /** setValue
   * @param pValue
   */
  void setValue( long pValue ) throws JC_Exception;
  
  /** setValue
   * 
   * @param pNode
   * @throws org.jecars.client.JC_Exception
   */
  void setValue( JC_Nodeable pNode ) throws JC_Exception;
  
  /** setValue
   * @param pStream
   * @param pListener
   * @throws org.jecars.client.JC_Exception
   */
  void setValue( InputStream pStream, JC_StreamListener pListener ) throws JC_Exception;
  
  /** addValue, used for multivalue properties
   * 
   * @param pValue
   * @param pChanged
   * @throws org.jecars.client.JC_Exception
   */
  void addValue( String pValue, boolean pChanged ) throws JC_Exception;
  
  /** removeProperty
   * 
   * @throws JC_Exception 
   */
  void removeProperty() throws JC_Exception;

  
  /** removeValue
   * 
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  void removeValue( String pValue ) throws JC_Exception;
  
}
