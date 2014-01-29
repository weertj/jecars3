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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public class PAR_CPU extends PAR_DefaultNode implements IPAR_CPU {
  
  private EPAR_CPUType mCPUType = EPAR_CPUType.UNKNOWN;

  final private List<IPAR_Core>         mCores = new ArrayList<>();
  final private EnumSet<EPAR_ItemState> mStates = EnumSet.of( EPAR_ItemState.NEEDS_REFRESH );

  /** PAR_CPU
   * 
   * @param pNode
   * @throws RepositoryException 
   */
  public PAR_CPU( final Node pNode ) throws RepositoryException {
    super( pNode );
    mCPUType = EPAR_CPUType.AVAILABLE;
    return;
  }

  /** cores
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IPAR_Core> cores() throws RepositoryException {
    synchronized( node().getSession() ) {
      mStates.remove( EPAR_ItemState.NEEDS_REFRESH );
      NodeIterator ni = node().getNodes();
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (core(n)==null) {
          mCores.add( new PAR_Core( n ));
        }
      }
    }
    return mCores;
  }

  private List<IPAR_Core> cachedCores() throws RepositoryException {
    if (mStates.contains( EPAR_ItemState.NEEDS_REFRESH )) {
      return cores();
    }
    return mCores;
  }

  
  @Override
  public IPAR_Core core( final Node pNode ) throws RepositoryException {
    return core( pNode.getProperty( "jecars:Title" ).getString() );
  }

  /** core
   * 
   * @param pName
   * @return
   * @throws RepositoryException 
   */
  @Override
  public IPAR_Core core( final String pName ) throws RepositoryException {
    for( IPAR_Core core : cachedCores() ) {
      if (core.name().equals( pName )) {
        return core;
      }
    }
    return null;
  }

  @Override
  public EPAR_CPUType cpuType() {
    return mCPUType;
  }
  
}
