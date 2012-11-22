/*
 * Copyright (c) 1996-2011 Maverick Software Development, 11/11 Software.
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

/*
 * JD_DefaultObjectIterator.java
 *
 * Created on 24 juli 2004, 22:47
 */

package nl.msd.jdots;

import java.io.Serializable;
import java.util.*;
import java.util.ArrayList;

/**
 * Default implementation of a object iterator
 * @author  weertj
 */
public class JD_DefaultObjectIterator extends JD_DefaultRangeIterator implements JD_ObjectIterator {
 
  /**
   * Clone the JD_ObjectIterator
   * @throws java.lang.CloneNotSupportedException 
   * @return The cloned iterator
   */
  public JD_ObjectIterator cloneIterator() throws CloneNotSupportedException {
//    JD_DefaultObjectIterator doi = (JD_DefaultObjectIterator)super.clone();
    JD_DefaultObjectIterator doi = new JD_DefaultObjectIterator();
    doi.mPos = mPos;
//    doi.mStore = (Vector)mStore.clone();
//    doi.setStoreVector( (Vector)getStoreVector().clone() );
    doi.setStore( (ArrayList)getStore().clone() );
    return doi;
  }
    
  /**
   * Retrieve the next JD_Objectable object
   * @return The next object
   */
  public JD_Objectable nextObject() {
    try {
      return (JD_Objectable)next();
    } catch( ArrayIndexOutOfBoundsException ae ) {
      throw new NoSuchElementException();
    }
  }    
    
}
