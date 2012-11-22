/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** JC_MultiValueProperty
 *
 * @version $Id: JC_MultiValueProperty.java,v 1.7 2009/03/24 15:24:41 weertj Exp $
 */
public class JC_MultiValueProperty extends JC_DefaultProperty implements Serializable {
  
  private final List<String> mPropertyValues        = new ArrayList<String>();
  private final List<String> mPropertyValuesNew     = new ArrayList<String>();
  private final List<String> mPropertyValuesDeleted = new ArrayList<String>();
  private       boolean      mRemoveAllProperties   = false;
  private       String       mDatatype              = "String";
  private       boolean      mIsUnstructured        = false;

  /** JC_MultiValueProperty
   *
   */
  public JC_MultiValueProperty () {
    super();
    return;
  }

  /** JC_MultiValueProperty
   *
   * @param pNode
   */
  public JC_MultiValueProperty( final JC_Nodeable pNode ) {
    super( pNode );
    return;
  }
  
  /** removeAllProperties
   * 
   * @return
   */
  protected final boolean removeAllProperties() {
    return mRemoveAllProperties;
  }

  /** setRemoveAllProperties
   *
   * @param pRemove
   */
  protected final void setRemoveAllProperties( final boolean pRemove ) {
    mRemoveAllProperties = pRemove;
    return;
  }

  /** isMulti
   * 
   * @return
   */
  @Override
  public boolean isMulti() {
    return true;
  }

  public String getDatatype() {
    return mDatatype;
  }
  
  public void setUnstructured( final boolean pU ) {
    mIsUnstructured = pU;
    return;
  }
  
  public boolean isUnstructured() {
    return mIsUnstructured;
  }

  /** setValues
   * replaces the current values with the given set strings
   *
   * @param pValues
   * @throws org.jecars.client.JC_Exception
   */
  public void setValues( final Collection<String>pValues ) throws JC_Exception {
    mDatatype = "String";
    final Collection<String> curvals = new ArrayList<String>( mPropertyValues );
    for (String val : pValues) {
      if (curvals.contains( val )) {
        curvals.remove( val );
      } else {
        addValue( val, true );
      }
    }
    return;
  }

  /** setValuesD
   * 
   * @param pValues
   * @throws org.jecars.client.JC_Exception
   */
  public void setValuesD( final Collection<Double>pValues ) throws JC_Exception {
    mDatatype = "Double";
    Collection<String> curvals = new ArrayList<String>( mPropertyValues );
    String v;
    for (Double val : pValues) {
      v = val.toString();
      if (curvals.contains( v )) {
        curvals.remove( v );
      } else {
        addValue( v, true );
      }
    }
    return;
  }

  /** setValuesL
   *
   * @param pValues
   * @throws org.jecars.client.JC_Exception
   */
  public void setValuesL( final Collection<Long>pValues ) throws JC_Exception {
    mDatatype = "Long";
    Collection<String> curvals = new ArrayList<String>( mPropertyValues );
    String v;
    for (Long val : pValues) {
      v = val.toString();
      if (curvals.contains( v )) {
        curvals.remove( v );
      } else {
        addValue( v, true );
      }
    }
    return;
  }

  /** setValuesB
   *
   * @param pValues
   * @throws org.jecars.client.JC_Exception
   */
  public void setValuesB( final Collection<Boolean>pValues ) throws JC_Exception {
    mDatatype = "Boolean";
    Collection<String> curvals = new ArrayList<String>( mPropertyValues );
    String v;
    for (Boolean val : pValues) {
      v = val.toString();
      if (curvals.contains( v )) {
        curvals.remove( v );
      } else {
        addValue( v, true );
      }
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
    Object o = super.getValue();
    if (o==null) {
      StringBuilder sb = new StringBuilder();
      for (String prop : mPropertyValues) {
        sb.append( prop ).append( '\n' );
      }
      o = sb.toString();
    }
    return o;
  }

  /** getValues
   * 
   * @return
   */
  public List<String> getValues() {
    return mPropertyValues;
  }

  public List<String> getValues( String pPrefix ) {
    List<String> vals = new ArrayList<String>();
    for( final String v : mPropertyValues ) {
      if (v.startsWith(pPrefix)) {
        vals.add(v);
      }
    }
    return vals;
  }

  /** getSize
   * Get the number of property values
   * 
   * @return
   */
  public int getSize() {
    return mPropertyValues.size();
  }

  @Override
  public String getValueString() {
    try {
      return getValue().toString();
    } catch( Exception je ) {
      return null;
    }
  }

  
  /** getValuesAsDouble
   *
   * @return
   */
  public Collection<Double> getValuesAsDouble() {
    Collection<Double> dv = new ArrayList<Double>( mPropertyValues.size() );
    for (String v : mPropertyValues) {
      dv.add( Double.valueOf( v ));
    }
    return dv;
  }

  /** getValuesAsLong
   *
   * @return
   */
  public Collection<Long> getValuesAsLong() {
    Collection<Long> dv = new ArrayList<Long>( mPropertyValues.size() );
    for (String v : mPropertyValues) {
      dv.add( Long.valueOf( v ));
    }
    return dv;
  }

  /** getValuesNew
   * 
   * @return
   */
  protected List<String> getValuesNew() {
    return Collections.unmodifiableList(mPropertyValuesNew);
  }

  /** clearNewValues
   * 
   */
  protected void clearNewValues() {
    mPropertyValuesNew.clear();
    return;
  }
  
  /** getValuesDeleted
   * 
   * @return
   */
  protected List<String> getValuesDeleted() {
    return Collections.unmodifiableList(mPropertyValuesDeleted);
  }
  
  /** clearDeletedValues
   * 
   */
  protected void clearDeletedValues() {
    mPropertyValuesDeleted.clear();
    return;
  }
  
  /** addValue
   * 
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void addValue( final String pValue, final boolean pChanged ) throws JC_Exception {
    if (pChanged) {
      mPropertyValuesNew.add( pValue );
      setChanged( true );
    } else {
      mPropertyValues.add( pValue );        
    }
    return;
  }
  
  /** removeValue
   * 
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  @Override
  public void removeValue( final String pValue ) throws JC_Exception {
    mPropertyValuesDeleted.add( pValue );
    setRemoved( true );
    setChanged( true );
    return;
  }
 
  /** removeValues
   * 
   * @param pValues
   * @throws JC_Exception 
   */
  public void removeValues( final List<String> pValues ) throws JC_Exception {
    for( final String s : pValues ) {
      removeValue( s );
    }
    return;
  }

  
  /** removeAllValues
   * 
   * @throws JC_Exception
   */
  public void removeAllValues() throws JC_Exception {
//    Collection<String> vals = getValues();
//    for (String val : vals) {
//      removeValue( val );
//    }
    mRemoveAllProperties = true;
    setRemoved( true );
    setChanged( true );
    return;
  }
  
}
