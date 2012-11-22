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

import java.io.Serializable;

/**
 * JC_DefaultItem
 *
 * @version $Id: JC_DefaultItem.java,v 1.7 2009/04/17 14:15:31 weertj Exp $
 */
public class JC_DefaultItem implements JC_Itemable, Serializable, Cloneable {

  private static final long serialVersionUID = 200912251640L;

  private  String mName      = null;
  private boolean mIsChanged = false;
  private boolean mIsRemoved = false;
  private boolean mIsNew     = false;
 
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
  
  /** morphTo
   * 
   * @param pItem
   */
  protected void morphTo( final JC_DefaultItem pItem ) {
    pItem.mName      = mName;
    pItem.mIsChanged = mIsChanged;
    pItem.mIsNew     = mIsNew;
    pItem.mIsRemoved = mIsRemoved;
    return;
  }
  
  /** getName
   * @return
   */
  @Override
  public String getName() {
    return mName;
  }

  /** setName
   * @param pName
   */
//  @Override
  public void setName( final String pName ) {
    mName = pName;
    return;
  }

  @Override
  public boolean isChanged() {
    return mIsChanged;
    
  }

  /** isRemoved
   * @return
   */
  @Override
  public boolean isRemoved() {
    return mIsRemoved;
  }

  /** isNew
   *
   * @return
   */
  @Override
  public boolean isNew() {
    return mIsNew;
  }

  /** setChanged
   * 
   * @param isChanged
   */
  @Override
  public void setChanged( final boolean isChanged) {
    mIsChanged = isChanged;
    if (this instanceof JC_Propertyable) {
      ((JC_Propertyable)this).getNode().setChanged( isChanged );
    }
    return;
  }
  
  /** setRemoved
   * @param isRemoved
   */
  @Override
  public void setRemoved( final boolean pIsRemoved ) {
    mIsRemoved = pIsRemoved;
    if (this instanceof JC_Propertyable) {
      ((JC_Propertyable)this).getNode().setChanged( pIsRemoved );
    }
    return;
  }
  
  /** setNew
   * @param isNew
   */
  @Override
  public void setNew( final boolean pIsNew ) {
    mIsNew = pIsNew;
    return;
  }

  @Override
  public void save( final JC_Params pParams ) throws JC_Exception {
    return;
  }

  @Override
  public void save() throws JC_Exception {
    return;
  }

  @Override
  public void refresh() throws JC_Exception {
    return;
  }

}
