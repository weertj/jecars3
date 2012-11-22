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
 * JD_Taglist.java
 *
 * Created on 4 juli 2000, 14:33
 */
 
package nl.msd.jdots;

import java.util.*;
import java.util.regex.*;
import java.util.logging.*;

/** JDots Taglist, a generic container to store "Tag" -> "Data" objects. It
 * is possible to stored multiple data value in one key.
 * @author weertj
 * @version 1.0
 */
public class JD_Taglist extends JD_Object {

  static private Logger gLog = Logger.getLogger( "nl.msd.jdots.JD_Taglist" );
      
  static private JD_Taglist mPoolTL   = new JD_Taglist();
  static private boolean    mPoolUsed = false;
    
  /** Allocated a pooled taglist
   * @return tag list object
  */
  static synchronized public JD_Taglist allocTaglist() {
    if (mPoolUsed==false) {
      mTaglistPoolCounter++;
      mTaglistPoolUsedCounter++;
      mPoolUsed = true;
      return mPoolTL;
    } else {
      mTaglistPoolCounter++;
      mTaglistCounter--;
      return new JD_Taglist();
    }
  }
  
  /** Release a pooled taglist
   * @param pTags The taglist
   */
  static synchronized public void releaseTaglist( JD_Taglist pTags ) {
    if (pTags==mPoolTL) {
      mTaglistPoolCounter--;
      mPoolTL.clear();
      mPoolUsed = false;
      pTags = null;
    } else {
      mTaglistPoolCounter--;
      pTags = null;
    }
    return;
  }
  
  /** Creates new JD_Taglist
   */
  public JD_Taglist() {
    mHash = new HashMap(4);
    init();
    return;
  }

  /** Creates new JD_Taglist
   * @param pOrdered If true then an ordered (LinkedHashMap) is created
   */
  public JD_Taglist(boolean pOrdered) {
    if (pOrdered) {
      mHash = new LinkedHashMap(4);
    } else {
      mHash = new HashMap(4);
    }
    init();
    return;
  }
 
  /** Create a new taglist with a Tag.
   *
   * @param pKey Key definition
   * @param pData The data, may be null
   */
  public JD_Taglist(Object pKey, Object pData) {
    super();
    mHash = new HashMap();
    putData( pKey, pData );
    init();
    return;
  }

  private void init() {
    mTaglistCounter++;
    decreaseObjectCount();
    return;
  }

  
  /** Clone the taglist
   * @throws CloneNotSupportedException When the clone is not supported
   * @return The cloned object
   */
  final public Object clone() throws CloneNotSupportedException {
    JD_Taglist tags = new JD_Taglist();
    tags.mHash = (HashMap)mHash.clone();
    return tags;
  }


  /**
   * Destroy the taglist
   * 
   * @param pTags Not used
   * @throws nl.msd.jdots.JD_Exception when an error occurs
   */
  public void JD_Destroy( JD_Taglist pTags ) throws JD_Exception {
    increaseObjectCount();
    destroy();
    super.JD_Destroy( pTags );
    return;
  }

  /** Destroy all data
   */
  public void destroy() {
    clear();
    mHash = null;
    return;
  }

  /** Remove all data
   */
  public void clear() {
    if (mHash!=null) {
      mHash.clear();
    }
    return;
  }

  /** Get the first data object using the string key regular expression.
   * @param pKey Search this key.
   * @param pRegexInTaglistKey if true then the keys in the taglist can be regex.
   * @return The found data, or null when not found or empty.
   */
  public Object getDataRegex( String pKey, boolean pRegexInTaglistKey ) {
    int     index = 0;
    boolean found = false;
    Object  obj;
    Iterator it = mHash.keySet().iterator();
    String k;
    if (pRegexInTaglistKey==true) {
      while( it.hasNext() ) {
        k = (String)it.next();
        Pattern p = Pattern.compile( k );
        Matcher m = p.matcher( pKey );
        if (m.matches()) {
          return getData( k );
        }
      }        
    } else {
      Pattern p = Pattern.compile( pKey );
      while( it.hasNext() ) {
        k = (String)it.next();
        Matcher m = p.matcher( k );
        if (m.matches()) {
          return getData( k );
        }
      }
    }
    return null;
  }

  /** Get the data objects using the string key regular expression.
   * @param pKey Search this key.
   * @param pRegexInTaglistKey if true then the keys in the taglist can be regex.
   * @return The found data, or null when not found or empty.
   */
  public Object[] getMDataRegex( String pKey, boolean pRegexInTaglistKey ) {
    ArrayList objs = new ArrayList();
    Iterator it = mHash.keySet().iterator();
    String k;
    if (pRegexInTaglistKey==true) {
      while( it.hasNext() ) {
        k = (String)it.next();
        Pattern p = Pattern.compile( k );
        Matcher m = p.matcher( pKey );
        if (m.matches()) {
          objs.add( getData( k ) );
        }
      }        
    } else {
      Pattern p = Pattern.compile( pKey );
      while( it.hasNext() ) {
        k = (String)it.next();
        Matcher m = p.matcher( k );
        if (m.matches()) {
          objs.add( getData( k ) );
        }
      }
    }
    return objs.toArray();
  }

  
  /** Get the first key using the string key regular expression.
   * @param pKey Search this key.
   * @param pRegexInTaglistKey if true then the keys in the taglist can be regex.
   * @return The found data, or null when not found or empty.
   */
  public Object getKeyRegex( String pKey, boolean pRegexInTaglistKey ) {
    Iterator it = mHash.keySet().iterator();
    String k;
    if (pRegexInTaglistKey==true) {
      while( it.hasNext() ) {
        k = (String)it.next();
        Pattern p = Pattern.compile( k );
        Matcher m = p.matcher( pKey );
        if (m.matches()) {
          return k;
        }
      }        
    } else {
      Pattern p = Pattern.compile( pKey );
      while( it.hasNext() ) {
        k = (String)it.next();
        Matcher m = p.matcher( k );
        if (m.matches()) {
          return k;
        }
      }
    }
    return null;
  }

  /** Get the keys using the string key regular expression.
   * @param pKey Search this key.
   * @param pRegexInTaglistKey if true then the keys in the taglist can be regex.
   * @return The collection.
   */
  public Collection getKeysRegex( String pKey, boolean pRegexInTaglistKey ) {
    Collection col = new ArrayList();
    Iterator it = mHash.keySet().iterator();
    String k;
    if (pRegexInTaglistKey==true) {
      while( it.hasNext() ) {
        k = (String)it.next();
        Pattern p = Pattern.compile( k );
        Matcher m = p.matcher( pKey );
        if (m.matches()) {
          col.add( k );
        }
      }        
    } else {
      Pattern p = Pattern.compile( pKey );
      while( it.hasNext() ) {
        k = (String)it.next();
        Matcher m = p.matcher( k );
        if (m.matches()) {
          if (col.contains( k )==false) {
            col.add( k );
          }
        }
      }
    }
    return col;
  }

  /** Get the first data object using the key.
   * @param pKey Search this key.
   * @return The found data, or null when not found or empty.
   */
  public Object getData(Object pKey) {
    return getData( pKey, 0 );
  }

  /** Is data item available?
   * @param pKey The search key
   * @param pValue The data value to be found
   * @return true when found
   */
  public boolean getData( Object pKey, Object pValue ) {
    if (getDataIndex( pKey, pValue )==-1) {
      return false;
    } else {
      return true;
    }
  }
  
  /** getDataList
   * 
   * @param pKey
   * @return
   */
  public ArrayList getDataList( Object pKey ) {
    return (ArrayList)mHash.get( pKey );    
  }

  /** Is data item available?
   * @param pKey The search key
   * @param pValue The data value to be found
   * @return The index number of the tag, = -1 when not found
   */
  public int getDataIndex( Object pKey, Object pValue ) {
    int     index = 0;
    boolean found = false;
    Object  obj;
    ArrayList arr = (ArrayList)mHash.get( pKey );
    if (arr!=null) {
      while( (!found) && (arr.size()>index) ) {
        obj = arr.get( index );
        if (obj!=null)
          found = obj.equals(pValue);
        index++;
      }
    }
    index--;
    if (found==false) index = -1;
    return index;
  }

  /** Retrieve data from the taglist
   * @param pKey Data key object
   * @param pIndex When multiple data values are available, the index
   * @return The value
   */
  public Object getData(Object pKey,int pIndex) {
    Object    obj = null;
    ArrayList arr = (ArrayList)mHash.get( pKey );
    if ((arr!=null) && (pIndex>=0)) {
      if (arr.size()>pIndex) {
        obj = arr.get( pIndex );
      }
    }
    return obj;
  }

  /**
   * Return the hasmap iterator
   * @return The iterator
   */
  public Iterator getIterator() {
    Set set = mHash.keySet();
    return set.iterator();
  }

  
  /** Return the hashmap
   * @return The hashmap as reference
   */  
  public HashMap getHashMap() {
    return mHash;
  }
  
  /** Is taglist empty?
   * @return true = taglist empty
   */  
  public boolean isEmpty() {
    return mHash.isEmpty();
  }
  
    
  /** Return the number of keys in the taglist, note that these are only the
   *  parent keys.
   * @return the number of parent keys.
   */
  public int getNumberOfKeys() {
    return mHash.size();
  }

  /** Merge the taglist
   * @param pTags The taglist to be merged
   */
  public void putData( JD_Taglist pTags ) {
    if (pTags!=null) {
      mHash.putAll( pTags.mHash );
    }
    return;
  }

  
  /** Put data in the taglist
   * @param pKey The data key
   * @param pData The data itself
   */
  public void putData( Object pKey, Object pData ) {
    if (mHash.containsKey( pKey )==false) {      
      mHash.put( pKey, new ArrayList() );
    }
    ((ArrayList)mHash.get( pKey )).add( pData );
    return;
  }

  /** Put data in the taglist
   * @param pKey The data key
   * @param pData The data itself
   * @param pIndex The index on which data shall be put
   */
  public void putData( Object pKey, Object pData, int pIndex ) {
    ArrayList arr = (ArrayList)mHash.get( pKey );
    if ((arr!=null) && (pIndex>=0)) {
      arr.set( pIndex, pData );
    }
    return;
  }
  
  /** Remove <B>all</B> data values with the key.
   * @param pKey The key
   */
  public void removeData(Object pKey) {
    ArrayList arr = (ArrayList)mHash.remove( pKey );
    if (arr!=null) {
      arr.clear();
    }
    return;
  }

  /** Remove <B>all</B> keys with data value.
   * @param pValue The value
   */
  public void removeDataValue( Object pValue ) {
    boolean changed = true;
    while( changed==true ) {
      synchronized( this ) {
        int i;
        Object key, data;
        Set set = mHash.keySet();
        Iterator it = set.iterator();
        changed = false;
        while( (changed==false) && (it.hasNext()) ) {
           key = it.next();
           i = 0;
           while( (changed==false) && ((data=getData(key,i++))!=null) ) {
  //           if (data.equals(pValue)) {             
  //           }
           }
           if ((i==1) && (pValue==null)) {
             removeData( key );
             changed = true;
           }
           // **** Hmmmmmmmmmm TODO
           if ((i==2) && (pValue!=null)) {
             removeData( key );
             changed = true;             
           }
        }
      }
    }
    return;
  }


  /** Replace the data, removing all keys first
   * @param pKey The key
   * @param pData The data
   */
  public void replaceData( Object pKey, Object pData ) {
    removeData( pKey );
    putData( pKey, pData );
    return;
  }


  /** Sort the data belong to the key
   * @param pKey Data key
   * @param pComp An object with the Comparable interface
   */
  public void sortData( Object pKey, Comparator pComp ) {
    ArrayList arr = (ArrayList)mHash.get( pKey );
    if (arr!=null) {
      Object[] objl = arr.toArray();
      Arrays.sort( objl, pComp );
      removeData( pKey );
      int l = objl.length-1;
      int i = 0;
      while( i<=l ) {
        putData( pKey, objl[i++] );
      }
    }
    return;
  }
  
  /** Debug print
   */
  public void print() {
    int i;
    Object key, data;
    Set set = mHash.keySet();
    Iterator it = set.iterator();
    System.out.println( "Taglist start\n----------" );
    while( it.hasNext() ) {
       key = it.next();
       i = 0;
       while( (data=getData(key,i++))!=null ) {
         System.out.println( "(key) " + key + " = (" + (i-1) + ") " + data );
       }
       if (i==1) System.out.println( "(key) " + key + " = NULL" );
    }
    return;
  }


  /** Print state
   */
  static public void printState() {
    gLog.fine( "Total number of Taglists used = " + mTaglistCounter );
    gLog.fine( "Total number of pool Taglists open = " + mTaglistPoolCounter );
    gLog.fine( "Total number of pool Taglists used = " + mTaglistPoolUsedCounter );
    return;
  }

  /** The taglist storage
   */
  protected HashMap mHash = null;

  static int mTaglistCounter         = 0;
  static int mTaglistPoolCounter     = 0;
  static int mTaglistPoolUsedCounter = 0;
}
