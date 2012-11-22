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
 * JD_Objectable.java
 *
 * Created on 15 februari 2005, 20:06
 */

package nl.msd.jdots;

/** Interface to define a JDots object
 * @author weertj
 */
public interface JD_Objectable {
  /**
   * Add a new JD_Objectable to this object
   * @param pObject The object which has to be added
   */
  public void JD_addObject( JD_Objectable pObject );
  /** The main function of the JDots system, invoke a method on the jdot object
   *
   * @param pMethod The name of the method to be called
   * @param pTags The taglist which will be send to the method
   * @param pFlags The flags
   * @throws JD_Exception When an error occurs
   */
  public void JD_doMethod( String pMethod, JD_Taglist pTags, int pFlags ) throws JD_Exception;

  /** Return the number of children
   * @return Number of children
   */
  public long              JD_getChildCount();
  /** Get the root object
   * @return root object
   */
  public JD_Objectable     JD_getRoot();
  /** Get parent object
   * @return parent object
   */
  public JD_Objectable     JD_getParent();
  /** Returns a JD_ObjectIterator over all child Nodes of this Node.
   * The cached and rewinded ObjectIterator is returned. So don't share the returned
   * object with other threads or cache it.
   * @return ObjectIterator
   */
  public JD_ObjectIterator JD_getObjects();
  /** Get a child object with a certain name
   * @param pName the name of the child object
   * @return the object or null when not found
   */
  public JD_Objectable     JD_getObject( String pName );
  /** Get a child object by path (../../../..)
   * @param pName the path forward slash seperated
   * @return the object or null when not found
   */
  public JD_Objectable     JD_getObjectbyPath( String pPath );
  /**
   * Remove a object from the object tree, this method only remove the
   * object without perfoming "destroy" operations. Normally the JD_remove() method should
   * be preferable to use.
   * @param pObject Remove this JDots object
   * @throws nl.msd.jdots.JD_Exception When an error occurs
   */
  public void              JD_removeObject( JD_Objectable pObject ) throws JD_Exception;
  /**
   * Remove a object from the object tree, the method will call a "JD_Destroy" to only it's children.
   * The default implementation of JD_Destroy will delegate this method to it's children again.
   * @param pPath relative path to the object, or null when this object has to be removed.
   * @throws nl.msd.jdots.JD_Exception When an error occurs
   */
  public void              JD_remove(       String        pPath   ) throws JD_Exception;
  
  public void              JD_removeChildren() throws JD_Exception;

  /** Retrieve the name of the object, the name is used to create the path.
   * @return The name of the object
   */
  public String JD_getName();
  /** Set the name of the object
   * @param pName the new name of the object
   */
  public void   JD_setName( String pName );
  
  /** Construct the full path identifier of this object
   * @return A string with the constructed path.
   */
  public String JD_getFullPath();

  
  /** Report an error
   * @param pThrow The error class
   */
  public void JD_reportError( Throwable pThrow );
  /**
   * Report an error with comment
   * @param pComment Comment for the error
   * @param pThrow The error class
   */
  public void JD_reportError( Throwable pThrow, String pComment );
  
  
  /** Ensure that the method must be available,
   * otherwise an exception will be generated
   */    
  static final public int JD_MUST_RUN      = 0x00000001;

  /** Delegates the method to all children.
   */  
  static final public int JD_DELEGATE      = 0x00000002;

  /** Execute the method only at one level of the tree,
   * after that the propagation will stop, regardless of the
   * delegation flags
   */  
  static final public int JD_ONE_LEVEL     = 0x00000004;

  /** Propagate the method to the parent JDot objects
   */  
  static final public int JD_TO_PARENT     = 0x00000008;

  /** Do not run the method on the current object, afterwards the flag will
   * be reset. So the method will be executed at other objects, if the delegation
   * flags are set.
   */  
  static final public int JD_NOT_ME        = 0x00000010;

  /** Start the method as a separate thread. every method call will be a single thread.
   */  
  static final public int JD_THREADED      = 0x00000020;

  /** Unsure that the method is executed in the <I>Swing</I> thread. If
   * the method is allready running in the <I>Swing</I> thread then nothing
   * is changed.
   */  
  static final public int JD_SWINGTHREAD   = 0x00000040;

  /** Delegate the method, high to low (at the same tree level), the opposite of JD_DELEGATE
   * For destroying objects this flag has an much higher performance
   */
  static final public int JD_DELEGATE_HTOL = 0x00000080;

  /** Start the method as a separate thread, but only as one single thread
   */  
  static final public int JD_THREADED_SINGLE = 0x00000100;

  /** Run the method only at the direct children, macro flag.
   */  
  static final public int JD_ONLY_CHILDREN = JD_ONE_LEVEL|JD_DELEGATE|JD_NOT_ME;
  
}
