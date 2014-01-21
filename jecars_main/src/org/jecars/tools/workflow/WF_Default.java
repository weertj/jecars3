/*
 * Copyright 2011-2012 NLR - National Aerospace Laboratory
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

package org.jecars.tools.workflow;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 */
abstract public class WF_Default implements IWF_Default {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools.workflow" );

  private       Node   mNode;
  private       String mPath = "";

  /** WF_Default
   * 
   * @param pNode 
   */
  public WF_Default( final Node pNode ) {
    mNode = pNode;
    try {
      if (pNode==null) {
        mPath = "";
      } else {
        mPath = pNode.getPath();
      }
    } catch( RepositoryException re ) {
      LOG.log( Level.WARNING, re.getMessage() );
    }
    return;
  }
  
  /** equals
   * 
   * @param obj
   * @return 
   */
  @Override
  public boolean equals( Object obj ) {
    if (obj instanceof IWF_Default) {
      IWF_Default t = (IWF_Default)obj;
      try {
        return t.getNode().getPath().equals( getNode().getPath() );
      } catch (RepositoryException ex) {
        return super.equals(obj);
      }
    }
    return super.equals(obj);
  }

  /** hashCode
   * 
   * @return 
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + (this.mPath != null ? this.mPath.hashCode() : 0);
    return hash;
  }

  /** getPath
   * 
   * @return 
   */
  @Override
  public String getPath() {
    return mPath;
  }
  
  /** getNode
   * 
   * @return 
   */
  @Override
  public Node getNode() {
    return mNode;
  }
  
  /** setNode
   * 
   * @param pN 
   */
  protected void setNode( Node pN ) {
    mNode = pN;
    return;
  }
  
  @Override
  public void save() throws RepositoryException {
    mNode.getSession().save();
    return;
  }

  @Override
  public String toString() {
    try {
      return mNode.getPath();
    } catch( Exception e ) {
      return super.toString();
    }
  }
  
  
  
}
