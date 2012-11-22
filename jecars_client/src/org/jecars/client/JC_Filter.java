/*
 * Copyright (c) 2006 Google Inc.
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

import com.google.gdata.data.DateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JC_Filter
 *
 * @version $Id: JC_Filter.java,v 1.4 2009/02/09 10:57:43 weertj Exp $
 */
public class JC_Filter {

  private String mNamePattern = null;

  /** Minimum updated timestamp for matched entries. */
  private DateTime mUpdatedMin;


  /** Maximum updated timestamp for matched entries. */
  private DateTime mUpdatedMax;


  /** Minimum published timestamp for matched entries. */
  private DateTime mPublishedMin;


  /** Maximum published timestamp for matched entries. */
  private DateTime mPublishedMax;

  private Collection<String> mCategories = null;
  
  /** createFilter
   * 
   * @return
   */
  static public JC_Filter createFilter() {
    return new JC_Filter();
  }

  /** setNamePattern
   * 
   * @param pPattern
   */
  public void setNamePattern( final String pPattern ) {
    mNamePattern = pPattern;
    return;
  }

  /** getNamePattern
   *
   * @return
   */
  public String getNamePattern() {
    return mNamePattern;
  }
  
  /** addCategory
   * 
   * @param pCat
   */
  public void addCategory( final String pCat ) {
    if (mCategories==null) {
      mCategories = new ArrayList<String>();
    }
    if (!mCategories.contains( pCat )) {
      mCategories.add( pCat );
    }
    return;
  }
  
  /** getCategories
   * 
   * @return
   */
  public Collection<String>getCategories() {
    return mCategories;
  }
  
  
}
