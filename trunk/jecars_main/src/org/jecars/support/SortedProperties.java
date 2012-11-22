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
package org.jecars.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author weert
 */
public class SortedProperties extends Properties {
  
  /** keys
   * 
   * @return
   */
  @Override
  public synchronized Enumeration<Object> keys() {
    Set set = keySet();
    return (Enumeration<Object>)sortKeys( set );
  }

  /** getAllKeysStartingWith
   * 
   * @param pPrefix
   * @return
   */
  public Collection<String> getAllKeysStartingWith( final String pPrefix ) {
    ArrayList<String> keys = new ArrayList<String>();
    Enumeration<?> pnames = propertyNames();
    while( pnames.hasMoreElements() ) {
      Object o = pnames.nextElement();
      if (o instanceof String) {
        if (((String)o).startsWith( pPrefix )==true) {
          keys.add( (String)o );
        }
      }
    }
    return keys;
  }
  
  /** sortKeys
   * 
   * @param pKeySet
   * @return
   */
  static public Enumeration<?>sortKeys( Set<String> pKeySet ) {
    List<String> sortedList = new ArrayList<String>();
    sortedList.addAll( pKeySet );
    Collections.sort(sortedList);
    return Collections.enumeration(sortedList);
  }

}
