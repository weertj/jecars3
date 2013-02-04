/*
 * Copyright 2008-2009 NLR - National Aerospace Laboratory
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
import java.util.Calendar;
import java.util.Date;
import org.apache.jackrabbit.util.ISO8601;

/**
 * JC_DefaultNode
 *
 * @version $Id: JC_DefaultProperty.java,v 1.24 2009/06/17 15:02:59 weertj Exp $
 */
public class JC_DefaultProperty extends JC_DefaultItem implements JC_Propertyable, Cloneable {

  private JC_Nodeable       mNode   = null;
  private String            mValue  = null;
  private long              mLValue = Long.MAX_VALUE;
  private double            mDValue = Double.MAX_VALUE;
  private boolean           mBValue = false;            // **** Indicates if mLValue represents the boolean value
  private int               mPropertyType = JC_PropertyType.TYPE_UNDEFINED;
  private JC_Streamable     mStream = null;
  private JC_Nodeable       mNodeValue = null;
  
  public JC_DefaultProperty() {
    return;
  }
    
  /** JC_DefaultProperty
   * 
   * @param pNode
   */
  public JC_DefaultProperty( final JC_Nodeable pNode ) {
    mNode = pNode;
    return;
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
//    dp.mNode   = mNode;
//    dp.mValue  = mValue;
//    dp.mLValue = mLValue;
//    return dp;
//  }
  
  /** destroy
   * 
   */
  protected void destroy() {
    mStream = null;
    mNodeValue = null;
    mNode = null;
    mValue = null;
    return;
  }
  
  /** isMulti
   * 
   * @return
   */
  @Override
  public boolean isMulti() {
    return false;
  }

  
  /** getPropertyType
   * 
   * @return
   */
  @Override
  public JC_PropertyType getPropertyType() throws JC_Exception {
    final JC_PropertyType pt = new JC_PropertyType();
    pt.setType( mPropertyType );
    return pt;
  }
  
  /** setPropertyType
   * 
   * @param pType
   */
  protected void setPropertyType( String pType ) {
    mPropertyType = JC_PropertyType.valueFromName( pType );
    return;
  }
  
  protected void _setValue( final String pName, final String pValue ) {
    setName( pName );
    mValue = pValue;
    return;
  }

  protected void _setValue( final String pName, final double pValue ) {
    setName( pName );
    mDValue = pValue;
    mValue = null;
    return;
  }

  /** _setValue
   *
   * @param pName
   * @param pValue
   */
  protected void _setValue( final String pName, final boolean pValue ) {
    setName( pName );
    mBValue = true;
    if (pValue) {
      mLValue = 1;
    } else {
      mLValue = 0;      
    }
    return;
  }

  protected void _setValue( final String pName, final long pValue ) {
    setName( pName );
    mLValue = pValue;
    mValue = null;
    return;
  }

  protected void _setValue( final String pName, final JC_Nodeable pValue ) {
    setName( pName );
    mNodeValue = pValue;
    return;
  }

  protected void _setValue( final String pName, final JC_Streamable pValue ) {
    setName( pName );
    mStream = pValue;
    mValue = null;
    return;
  }
  
  /** setValue
   * @param pValue
   */
  @Override
  public void setValue( Object pValue ) throws JC_Exception {
    if (pValue instanceof String)
      setValue((String) pValue);
    else
      throw new UnsupportedOperationException("Not supported yet.");    
    return;
  }

  
  /** setValue
   * @param pName
   * @param pValue
   */
  protected void setValue( final String pName, final String pValue ) {
    setName( pName );
    setValue( pValue );
    return;
  }
  
  /** setValue
   * @param pValue
   */
  @Override
  public void setValue( long pValue ) throws JC_Exception {
    mLValue = pValue;
    _setChanged( true );
    return;
  }

  /** setValue
   * 
   * @param pName
   * @param pNode
   * @throws org.jecars.client.JC_Exception
   */
  protected void setValue( final String pName, JC_Nodeable pNode ) throws JC_Exception {
    setName( pName );
    mNodeValue = pNode;
    return;
  }

  /** _setChanged
   * 
   * @param pV
   */
  protected void _setChanged( boolean pV ) {
    setChanged( pV );
    if (mNode!=null) mNode.setChanged( pV );
    return;
  }

  /** setValue
   * 
   * @param pNode
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void setValue( JC_Nodeable pNode ) throws JC_Exception {
    mNodeValue  = pNode;
    mStream     = null;
    mNodeValue  = null;
    _setChanged( true );
    return;
  }
    
  /** setValue
   * @param pName
   * @param pValue
   */
  protected void setValue( final String pName, final JC_Streamable pValue ) {
    setName( pName );
    _setChanged( true );
    mStream = pValue;
    return;
  }

  /** setValue
   * 
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  protected void setValue( final String pName, final long pValue ) throws JC_Exception {
    setName( pName );
    setValue( pValue );
    return;
  }

  @Override
  public JC_Nodeable getNode() {
    return mNode;
  }

  /** getValueString
   * Code all values to string values
   * 
   * @return
   */
  @Override
  public String getValueString() {
    if (mValue!=null) {
      return mValue;
    }
    if (mBValue) {
      if (mLValue==0) {
        return "false";
      }
      return "true";
    }
    if (mLValue!=Long.MAX_VALUE) {
      return String.valueOf(mLValue);
    }
    if (mDValue!=Double.MAX_VALUE) {
      return String.valueOf(mDValue);
    }
    if (mNodeValue!=null) {
      try {
        return mNodeValue.getPath();
      } catch( JC_Exception e ) {          
      }
    }
    return null;
  }

  /** decodeStringToStream
   *
   */
  @Override
  public void decodeStringToStream() {
    if (mValue!=null) {
      mStream = JC_DefaultStream.createStream( mValue, "text/plain" );
    }
    return;
  }


  /** getValue
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Object getValue() throws JC_Exception {
    if (mValue!=null) {
      return mValue;
    }
    if (mBValue) {
      if (mLValue==0) {
        return false;
      }
      return true;
    }
    if (mLValue!=Long.MAX_VALUE) {
      return mLValue;
    }
    if (mDValue!=Double.MAX_VALUE) {
      return mDValue;
    }
    if (mNodeValue!=null) {
      return mNodeValue;
    }
    return null;
  }

  /** getStream
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public JC_Streamable getStream() throws JC_Exception {
    return mStream;
  }

  /** getValueAsBoolean
   * 
   * @return
   * @throws JC_Exception
   */
  @Override
  public boolean getValueAsBoolean() throws JC_Exception  {
    return (Boolean)getValueAs( Boolean.class );
  }

  /** getValueAsLong
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public long getValueAsLong() throws JC_Exception  {
    return (Long)getValueAs( Long.class );
  }

  /** getValueAsDouble
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public double getValueAsDouble() throws JC_Exception  {
    return (Double)getValueAs( Double.class );
  }
  
  /** getValueAs
   * 
   * @param pObjectClass
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public Object getValueAs( final Class pObjectClass ) throws JC_Exception  {
    if (pObjectClass.equals( Date.class )) {
      final Object v = getValue();
      if (v instanceof String) {
        final Calendar c = ISO8601.parse( (String)v );
        return c.getTime();
      }
    } else if (pObjectClass.equals( Calendar.class )) {
      final Object v = getValue();
      if (v instanceof String) {
        final Calendar c = ISO8601.parse( (String)v );
        return c;
      }
    } else if (pObjectClass.equals( Double.class )) {
      final Object v = getValue();
      if (v instanceof Double) {
        return v;
      } else if (v instanceof Long) {
        return (double)((Long)v);
      } else if (v instanceof String) {
        return Double.parseDouble( (String)v );
      }
    } else if (pObjectClass.equals( Long.class )) {
      final Object v = getValue();
      if (v instanceof Long) {
        return v;
      } else if (v instanceof String) {
        return Long.parseLong( (String)v );
      }
    } else if (pObjectClass.equals( Integer.class )) {
      final Object v = getValue();
      if (v instanceof Integer) {
        return v;
      } else if (v instanceof String) {
        return Integer.parseInt( (String)v );
      }
    } else if (pObjectClass.equals( Boolean.class )) {
      return Boolean.parseBoolean( getValueString() );
    }
    throw new UnsupportedOperationException("Not supported yet. " + pObjectClass);
  }

  /** setValue
   * 
   * @param pValue
   */
  @Override
  public void setValue(String pValue) {
    if (pValue.equals( mValue )==false) {
      mValue = pValue;
      _setChanged( true );
    }
    return;
  }

  public void addValue( String pValue, boolean pChanged ) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public InputStream getValueAsStream(JC_StreamListener pListener) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setValue(InputStream pStream, JC_StreamListener pListener) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public void refresh() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void save() throws JC_Exception {
    mNode.save();
    return;
  }

  @Override
  public void removeProperty() throws JC_Exception {
    if (isNew()) {
      destroy();
    } else {
      mNode.setChanged( true );
      setChanged( true );
      setRemoved( true );
    }
    return;
  }

  
  @Override
  public void removeValue(String pValue) throws JC_Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String toString() {
    return getName() + "=" + mValue;
  }

  
}

