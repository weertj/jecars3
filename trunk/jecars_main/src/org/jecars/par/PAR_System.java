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

import java.net.InetAddress;
import java.net.UnknownHostException;
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
public class PAR_System extends PAR_DefaultNode implements IPAR_System {
  
  private EPAR_SystemType mSystemType = EPAR_SystemType.UNKNOWN;

  final private List<IPAR_CPU>          mCPUs = new ArrayList<>();
  final private EnumSet<EPAR_ItemState> mStates = EnumSet.of( EPAR_ItemState.NEEDS_REFRESH );

  /** PAR_System
   * 
   * @param pNode
   * @throws RepositoryException 
   */
  public PAR_System( final Node pNode ) throws RepositoryException {
    super( pNode );
    try {
      final String computername = InetAddress.getLocalHost().getHostName();
      String title = pNode.getProperty( "jecars:Title" ).getString();
      if (computername.equals(title)) {
        mSystemType = EPAR_SystemType.LOCAL;
      } else {
        mSystemType = EPAR_SystemType.UNKNOWN;
      }
    } catch( UnknownHostException e ) {
      mSystemType = EPAR_SystemType.UNKNOWN;
    }
    return;
  }

  /** systemType
   * 
   * @return 
   */
  @Override
  public EPAR_SystemType systemType() {
    return mSystemType;
  }

  /** cpus
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IPAR_CPU> cpus() throws RepositoryException {
    synchronized( node().getSession() ) {
      mStates.remove( EPAR_ItemState.NEEDS_REFRESH );
      NodeIterator ni = node().getNodes();
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (cpu(n)==null) {
          mCPUs.add( new PAR_CPU( n ));
        }
      }
    }
    return mCPUs;
  }

  private List<IPAR_CPU> cachedCPUS() throws RepositoryException {
    if (mStates.contains( EPAR_ItemState.NEEDS_REFRESH )) {
      return cpus();
    }
    return mCPUs;
  }
  
  @Override
  public IPAR_CPU cpu( final Node pNode ) throws RepositoryException {
    return cpu( pNode.getProperty( "jecars:Title" ).getString() );
  }

  @Override
  public IPAR_CPU cpu(String pName) throws RepositoryException {
    for( IPAR_CPU cpu : cachedCPUS() ) {
      if (cpu.name().equals( pName )) {
        return cpu;
      }
    }
    return null;
  }

  
  
  /** cpus
   * 
   * @param pT
   * @return
   * @throws RepositoryException 
   */
  @Override
  public List<IPAR_CPU> cpus( final EPAR_CPUType pT ) throws RepositoryException {
    List<IPAR_CPU> cpus = new ArrayList<>();
    for( IPAR_CPU cpu : cpus() ) {
      if (cpu.cpuType().equals( pT )) {
        cpus.add( cpu );
      }
    }
    return cpus;
  }
  
  
  
}
