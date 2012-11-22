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
 * JD_Object.java
 *
 * Created on 15 februari 2005, 20:06
 *
 * Author: Jacco van Weert -- weertj@xs4all.nl
 */

package nl.msd.jdots;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.logging.*;

/**
 * Default implementation of a JDots object.
 * @author weertj(Prog)
 */
public class JD_Object implements JD_Objectable, Serializable {
  
  static private Logger gLog = Logger.getLogger( "nl.msd.jdots" );

//  static public Vector jdotsList = new Vector();

  static private long gObjectCounter = 0;
  /** Caching purpose
   */
  static private final Class[] gParameterTypes =new Class[] {JD_Taglist.class};
    
  private JD_DefaultObjectIterator mChildren = null;
  private JD_Objectable            mParent   = null;
  private String                   mName     = null;
  
  static public void printStatus() {
//    System.out.println( "JDots count: " + gObjectCounter );
//    Iterator it = jdotsList.iterator();
//    while( it.hasNext() ) {
//      JD_Objectable jo = (JD_Objectable)it.next();
//      System.out.println( "JDots: " + jo + " = " + jo.JD_getFullPath() );
//    }
    return;
  }
  
  static protected void increaseObjectCount() {
    gObjectCounter++;
    return;
  }

  static protected void decreaseObjectCount() {
    gObjectCounter--;
    return;
  }
  
  /** Creates a new instance of JD_Object
   */
  public JD_Object() {
    if (!(this instanceof JD_Taglist)) {
//      jdotsList.add(this);
      increaseObjectCount();
    }
    return;
  }

  /** Retrieve the name of the object, the name is used to create the path.
   * @return The name of the object
   */
  public String JD_getName() {
    return mName;
  }
  
  /** Set the name of the object
   * @param pName the new name of the object
   */
  public void JD_setName( String pName ) {
    mName = pName;
    return;    
  }

  /** Get the root object
   * @return root object
   */
  public JD_Objectable JD_getRoot() {
    JD_Objectable root = this;
    while( root.JD_getParent()!=null ) root = root.JD_getParent();
    return root;
  }  
  
  
  /** Get parent object
   * @return parent object
   */
  public JD_Objectable JD_getParent() {
    return mParent;
  }

  /**
   * @see JD_Objectable#JD_getFullPath
   */
  public String JD_getFullPath() {
    StringBuffer  path = new StringBuffer();
    JD_Objectable obj = this;

    while( obj!=null ) {
      path.insert( 0, "/" );
      path.insert( 0, obj.JD_getName() );
      obj = obj.JD_getParent();
    }
    return path.toString();
  }

  
  /**
   * Add a new JD_Objectable to this object
   * @param pObject The object which has to be added
   */
  public void JD_addObject( JD_Objectable pObject ) {
    if (pObject!=null) {
      if (mChildren==null) {
        mChildren = new JD_DefaultObjectIterator();
      }
      mChildren.storeObject( pObject );
      if (pObject instanceof JD_Object) {
        ((JD_Object)pObject).mParent = this;
      }
    }
    return;
  }

  /** Returns a JD_ObjectIterator over all child Nodes of this Node.
   * The cached and rewinded ObjectIterator is returned. So don't share the returned
   * object with other threads or cache it.
   * @return ObjectIterator
   */
  public JD_ObjectIterator JD_getObjects() {
    if (mChildren!=null) {
      try {
        JD_ObjectIterator oi = mChildren.cloneIterator();
        return oi;
      } catch( Exception e) {
        gLog.log( Level.SEVERE, "", e );
      }
    }
    return mChildren;
  }

  /** Get a child object by path (../../../..)
   * @param pName the path forward slash seperated
   * @return the object or null when not found
   */
  public JD_Objectable JD_getObjectbyPath( String pPath ) {
    JD_Objectable o= null;
    if (pPath.indexOf( '/' )!=-1) {
      String name = pPath.substring( 0, pPath.indexOf( '/' ));
      o = JD_getObject( name );
      if (o!=null) {
        o = o.JD_getObjectbyPath( pPath.substring( pPath.indexOf( '/' )+1 ) );
      }
    } else {
      o = JD_getObject( pPath );
    }
    return o;
  }

  /** JD_getObject
   * Get a child object on a certain index
   * @param pIndex
   * @return
   */
  public JD_Objectable JD_getObject( final int pIndex ) {
    return (JD_Objectable)mChildren.getStore().get( pIndex );
  }

  /** Get a child object with a certain name
   * @param pName the name of the child object
   * @return the object or null when not found
   */
  public JD_Objectable JD_getObject( String pName ) {
    JD_Objectable o = null;
    JD_ObjectIterator oi = JD_getObjects();
    String name;
    if (oi!=null) {
      while( oi.hasNext() ) {
//        o = oi.nextObject();
        o = (JD_Objectable)oi.next(); // **** Optimalization
//        name = o.JD_getName();
        name = o.JD_getName();
        if (name!=null) {
//          if (name.compareTo( pName )==0) return o;
          if (name.equals( pName )) return o;
        } else {
          if (pName==null) return o;
        }
      }
    }
    return null;
  }

  
  /**
   * @see JD_Objectable#JD_getChildCount
   */
  public long JD_getChildCount() {
    if (mChildren==null) {
      return 0;
    }
    return mChildren.getSize();
  }
  
  /**
   * @see JD_Objectable#JD_doMethod
   */
  public void JD_doMethod( String pMethod, JD_Taglist pTags, int pFlags ) throws JD_Exception {
    boolean not_me = false;
 
    // **** Single threaded method?
    if ((pFlags & JD_THREADED_SINGLE)!=0) {
      // **** Reset the JD_THREADED_SINGLE flag
      pFlags = pFlags & ~(JD_THREADED_SINGLE);
      JD_Thread thread = new JD_Thread( this, pMethod, pTags, pFlags );
      thread.start();
      return;
    }
 
    // ***** Must this method run?
    if ((pFlags & JD_NOT_ME)!=0) {
      // **** Reset the NOT_ME flag, do it only once
      pFlags = pFlags & ~(JD_NOT_ME);
      not_me = true;
    }
    
    // ********************************************
    // **** Delegation to the children high to low?
    if ((pFlags & JD_DELEGATE_HTOL)!=0) {      
      long          index = JD_getChildCount()-1;
      int           flags = pFlags;
      JD_Objectable child;

      if ((flags & JD_ONE_LEVEL)!=0) {
        flags = flags & ~(JD_DELEGATE_HTOL);
      }
      for( ; index>=0 ; index-- ) {
        mChildren.reset();
        mChildren.skip( index );
        child = mChildren.nextObject();
        child.JD_doMethod( pMethod, pTags, flags );
      }
    }
    
    
    // ********************************
    // **** Delegation to the children?
    if ((pFlags & JD_DELEGATE)!=0) {
      int           index = 0;
      int           flags = pFlags;
      JD_Objectable child;
      JD_ObjectIterator oi = JD_getObjects();

      if ((flags & JD_ONE_LEVEL)!=0) {
        flags = flags & ~(JD_DELEGATE);
      }
      if (oi!=null) {
        while( oi.hasNext() ) {
          child = oi.nextObject();
          child.JD_doMethod( pMethod, pTags, flags );
        }
      }
    }

    // *********************
    // **** Call the method?
    if (not_me==false) {
      Method   meth;
      Object[] args = new Object[] { pTags };
      Class    cl = getClass();
      try {
        meth = cl.getMethod( pMethod, gParameterTypes );

        // **** Check the THREADED Flags  
        if (((pFlags & JD_THREADED)!=0) ||
            ((pFlags & JD_SWINGTHREAD)!=0)) {
          // **** Call the method as thread.
          // **** ONLY(!!) the JD_TO_PARENT & JD_DELEGATE flag is supported.
          if ((pFlags & JD_TO_PARENT)!=0) {
            JD_Thread thread = new JD_Thread( this, pMethod, pTags, JD_TO_PARENT );
            if ((pFlags & JD_SWINGTHREAD)!=0) {
              throw new RuntimeException( "JD_SWINGTHREAD not supported");
            } else {
              thread.start();
            }
          } else {
            if ((pFlags & JD_DELEGATE)!=0) {
              JD_Thread thread = new JD_Thread( this, pMethod, pTags, JD_DELEGATE );
              if ((pFlags & JD_SWINGTHREAD)!=0) {
                throw new RuntimeException( "JD_SWINGTHREAD not supported");
              } else {
                thread.start();
              }
            } else {
              JD_Thread thread = new JD_Thread( this, pMethod, pTags, 0 );
              // **** Check the SWINGTHREAD Flags  
              if ((pFlags & JD_SWINGTHREAD)!=0) {
                throw new RuntimeException( "JD_SWINGTHREAD not supported");
              } else {
                thread.start();
              }
            }
          }
          pFlags = 0; // **** Reset the flags
        } else {
//       long time = System.currentTimeMillis();
//     System.out.println( "do: " + meth.getName() + " : " + getName() );
          meth.invoke( this, args );
//   time = (System.currentTimeMillis()-time);
//   if (time>0) {
//    System.out.println( "time: " + cl + ":" + pMethod + " : " + time + "ms" );
//  }
        }
        if ((pFlags & JD_ONE_LEVEL)!=0) {
          pFlags = pFlags & ~(JD_TO_PARENT);
        }
      } catch( NoSuchMethodException e ) {
        // **** Method is not found
        if ((pFlags & JD_MUST_RUN)!=0) {
          JD_reportError( e );
        }
      } catch( IllegalAccessException e ) {
        JD_reportError( e );
      } catch( InvocationTargetException e ) {
        Throwable t = e;
        while( t.getCause()!=null ) t = t.getCause();
        throw new JD_Exception( t );
//        throw new JD_Exception( e.getCause() );
//  e.printStackTrace();
 //       JD_reportError( e );
      }
    }

    // **** Propagation to the parent?
    if ((pFlags & JD_TO_PARENT)!=0) {
      if (JD_getParent()!=null) {
        JD_getParent().JD_doMethod( pMethod, pTags, pFlags );
      }
    }
    return;
  }

  
  /** Report an error
   * @param pThrow The error class
   */
  public void JD_reportError( Throwable pThrow ) {
    JD_reportError( pThrow, null );
    return;
  }

  /**
   * Report an error with comment
   * @param pComment Comment for the error
   * @param pThrow The error class
   */
  public void JD_reportError( Throwable pThrow, String pComment ) {
    try {
      JD_Taglist tags = new JD_Taglist();
      tags.putData( "java.lang.Throwable", pThrow );
      tags.putData( "Comment",   pComment );
      JD_doMethod( "JD_RaiseError", tags, JD_TO_PARENT );
    } catch ( JD_Exception ee ) {
      gLog.log( Level.SEVERE, "FATAL: Error", ee );
    }
    return;
  }

  /**
   * Remove a object from the object tree, this method only remove the
   * object without perfoming "destroy" operations. Normally the JD_remove() method should
   * be preferable to use.
   * @param pObject Remove this JDots object
   * @throws nl.msd.jdots.JD_Exception When an error occurs
   */
  public void JD_removeObject( JD_Objectable pObject ) throws JD_Exception {
    mChildren.removeObject( pObject );
    return;
  }
  
  public void JD_removeChildren() throws JD_Exception {
    JD_doMethod( "JD_Destroy", null, JD_ONLY_CHILDREN );
    return;
  }

  /** JD_setChildren
   * 
   * @param pC
   */
  protected void JD_setChildren( JD_DefaultObjectIterator pC ) {
    mChildren = pC;
    return;
  }
  
  /** JD_getChildren
   * 
   * @return
   */
  protected JD_DefaultObjectIterator JD_getChildren() {
    return mChildren;
  }

  /**
   * Remove a object from the object tree, the method will call a "JD_Destroy" to only it's children.
   * The default implementation of JD_Destroy will delegate this method to it's children again.
   * @param pPath relative path to the object, or null when this object has to be removed.
   * @throws nl.msd.jdots.JD_Exception When an error occurs
   */
  public void JD_remove( String pPath ) throws JD_Exception {
    if (pPath==null) {
//System.out.println( " JD_REMOVE -- " + JD_getFullPath() );
//stacktrace();
      if (JD_getParent()!=null) {
        JD_doMethod( "JD_Destroy", null, JD_ONLY_CHILDREN );
        JD_getParent().JD_removeObject( this );
        mParent = null;
      } else {
        JD_doMethod( "JD_Destroy", null, JD_ONLY_CHILDREN );        
      }
      
//      if (jdotsList.contains(this)) {
//        jdotsList.removeElement(this);
//      } else {
//        if ((this instanceof JD_Taglist)==false) {
//          System.out.println( "Free twice: " + this );
//        }
//      }
      
      decreaseObjectCount();
      mName = null;
      if (mChildren!=null) {
        mChildren.destroy();
        mChildren = null;
      }
    } else {
      throw new JD_Exception( "Cannot remove: " + pPath );
    }
    return;
  }

  
 /**
   * Destroy the jdot object
   * @param pTags The following tags are used;<BR>
   * @throws nl.msd.jdots.JD_Exception When an error occurs
   */
  public void JD_Destroy( JD_Taglist pTags ) throws JD_Exception {

    JD_remove( (String)null );
    // **** Clear members
//    decreaseObjectCount();
//    mName     = null;
//    mChildren = null;
    return;
  }
  
  static public void stacktrace() {
    try {
      throw new Exception();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }  
  
}
