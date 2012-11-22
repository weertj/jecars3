/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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

import java.util.*;

import javax.jcr.*;

/**
 * CARS_DefaultRangeIterator
 *
 * @version $Id: CARS_DefaultRangeIterator.java,v 1.1 2007/09/26 14:19:06 weertj Exp $
 */
public class CARS_DefaultRangeIterator implements RangeIterator, Enumeration {
    
    protected int    mPos   = -1;
    protected Vector mStore = new Vector();
        
    public void storeObject( Object pObject ) {
      mStore.add( pObject );
      return;
    }
    
    public boolean hasNext() {
      if (mStore==null) return false;
      if ((mPos+1)<mStore.size()) {
        return true;
      } else {
        return false;
      }
    }
    
    public Object next() {
      return mStore.get( ++mPos );
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public long getPosition() {
      return mPos+1;
    }
    
    public long getSize() {
      if (mStore==null) return 0;
      return mStore.size();
    }
    
    public void skip( long pSkipNum ) {
      mPos += pSkipNum;
      if (mPos>=mStore.size()) throw new NoSuchElementException();
      return;
    }
    
    public void reset() {
      mPos = -1;
      return;
    }
    
    public void destroy() {
      mStore.removeAllElements();
      mStore = null;
      return;
    }
    
    public boolean hasMoreElements() {
      return hasNext();
    }
    
    public Object nextElement() {
      return next();
    }
    
    public Object getObjectAt( int pIndex ) {
      return mStore.get( pIndex );
    }
    
    public void removeObject( Object pObject ) {
      mStore.remove( pObject );
      return;
    }
    
    public List getAsList() {
      return (List)mStore.clone();
    }

    public Object[] getAsArray() {
      return mStore.toArray();
    }
    
    public void overwriteFromArray( Object[] pArray ) {
      ListIterator i = mStore.listIterator();
      for (int j=0; j<pArray.length; j++) {
        i.next();
        i.set(pArray[j]);
      }
      reset();
      return;
    }
    
    public void setFromList( List pList ) {
      destroy();
      mStore = new Vector( pList );
      reset();
      return;
    }
    
}
