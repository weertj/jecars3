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
 * JD_DefaultRangeIterator.java
 *
 * Created on 24 juli 2004, 22:41
 */

package nl.msd.jdots;

import java.io.Serializable;
import java.util.*;


/** JD_DefaultRangeIterator
 * Default implementation of a range iterator
 * @author  weertj
 */
public class JD_DefaultRangeIterator implements Enumeration, Iterator, JD_RangeIterator, Serializable {
    
    protected int       mPos   = -1;
    private   ArrayList mStore = new ArrayList();
    
    /** setStore
     *
     * @param pStore
     */
    protected void setStore( ArrayList pStore ) {
      mStore = pStore;
      return;
    }

    /** getStore
     *
     * @return
     */
    protected ArrayList getStore() {
      return mStore;
    }
    
    /**
     * Store an object for iterator use
     * @param pObject Object to be stored
     */
    public void storeObject( Object pObject ) {
      mStore.add( pObject );
      return;
    }
    
    /**
     * Remove an object from the Iterator
     * @param pObject The object to be removed
     */
    public void removeObject( Object pObject ) {
      mStore.remove( pObject );
      return;
    }
    
    /**
     * Are there objects left
     * @return true if next() will return a new object
     */
    public boolean hasNext() {
      if ((mPos+1)<mStore.size()) {
        return true;
      } else {
        return false;
      }
    }
    
    /**
     * Return the next object of the iterator
     * @return The next object
     */
    public Object next() {
      try {
        return mStore.get( ++mPos );
      } catch (ArrayIndexOutOfBoundsException aiobe) {
        throw new NoSuchElementException();
      }
    }
    
    /** Not implemented
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /** Returns the index of <code>node</code> in the receivers children.
     * If the receiver does not contain <code>node</code>, -1 will be
     * returned.
     */
    public int getIndex( Object pObject ) {
      return mStore.indexOf( pObject );
    }
    
    /**
     * Get the current object position
     * @return The position
     */
    public long getPos() {
      return mPos;
    }
    
    /**
     * The total number of objects in the iterator
     * @return Number of objects
     */
    public long getSize() {
      return mStore.size();
    }
    
    /**
     * SKip a number of objects
     * @param pSkipNum The number to be skipped
     */
    public void skip( long pSkipNum ) {
      mPos += pSkipNum;
      if (mPos>=mStore.size()) throw new NoSuchElementException();
      return;
    }
    
    /**
     * Reset the iterator
     */
    public void reset() {
      mPos = -1;
      return;
    }

    /** GC friendly destroy
     */
    public void destroy() {
//      mStore.removeAllElements();
      mStore.clear();
      mStore = null;
      return;
    }

    public boolean hasMoreElements() {
      return hasNext();
    }
    
    public Object nextElement() {
      return next();
    }

    
}
