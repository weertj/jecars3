/*
 * Copyright 2014 NLR - National Aerospace Laboratory
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
package org.jecars.par;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public class PAR_DefaultNode implements IPAR_DefaultNode {
  
  final private Node mNode;

  /** PAR_DefaultNode
   * 
   * @param pNode 
   */
  public PAR_DefaultNode( final Node pNode ) {
    mNode = pNode;
    return;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IPAR_DefaultNode) {
      IPAR_DefaultNode dn = (IPAR_DefaultNode)obj;
      try {
        return dn.node().getPath().equals( node().getPath() );
      } catch( RepositoryException re ) {        
      }
    }
    return super.equals(obj);
  }
  
  
  
  /** name
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public String name() throws RepositoryException {
    return mNode.getProperty( "jecars:Title" ).getString();
  }

  /** node
   * 
   * @return 
   */
  @Override
  public Node node() {
    return mNode;
  }



  
}
