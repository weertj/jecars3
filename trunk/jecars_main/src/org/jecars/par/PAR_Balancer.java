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
import java.util.concurrent.atomic.AtomicLong;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Factory;

/**
 *
 * @author weert
 */
public class PAR_Balancer implements IPAR_Balancer {

  static final private IPAR_Balancer BALANCER = new PAR_Balancer();

  final private AtomicLong mSchedulerLock = new AtomicLong(1);

  final private List<IPAR_System>       mSystems = new ArrayList<>();
  final private EnumSet<EPAR_ItemState> mStates = EnumSet.of( EPAR_ItemState.NEEDS_REFRESH );
  
  /** PAR_Balancer
   * 
   */
  private PAR_Balancer() {
  }

  public static IPAR_Balancer BALANCER() {
    return BALANCER;
  }

  /**
   * systems
   *
   * @return
   * @throws RepositoryException
   */
  @Override
  public List<IPAR_System> systems() throws RepositoryException {
    final Session ses = CARS_Factory.getSystemAccessSession();
    mStates.remove( EPAR_ItemState.NEEDS_REFRESH );
    synchronized( ses ) {
      final NodeIterator ni = ses.getNode("/JeCARS/Systems").getNodes();
      while (ni.hasNext()) {
        final Node n = ni.nextNode();
        if (system(n)==null) {
          if (n.isNodeType("jecars:RES_System")) {
            mSystems.add(new PAR_System(n));
          }
        }
      }
    }
    return mSystems;
  }

  private List<IPAR_System> cachedSystems() throws RepositoryException {
    if (mStates.contains( EPAR_ItemState.NEEDS_REFRESH )) {
      return systems();
    }
    return mSystems;
  }

  @Override
  public IPAR_System system( final Node pNode ) throws RepositoryException {
    return system( pNode.getProperty( "jecars:Title" ).getString() );
  }
  
  /** system
   * 
   * @param pName
   * @return
   * @throws RepositoryException 
   */
  @Override
  public IPAR_System system( final String pName ) throws RepositoryException {
    for( IPAR_System sys : cachedSystems() ) {
      if (sys.name().equals( pName )) {
        return sys;
      }
    }
    return null;
  }
  
  
  /**
   * coresByWish
   *
   * @param pWish
   * @param pAllocate
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public List<IPAR_Core> coresByWish( final IPAR_ResourceWish pWish, final boolean pAllocate ) throws RepositoryException {
    final List<IPAR_Core> cores = new ArrayList<>();
    synchronized (mSchedulerLock) {
      mSchedulerLock.incrementAndGet();
      int nrCores = pWish.numberOfCores();
      if (pWish.systemType()==EPAR_SystemType.LOCAL) {
        for (final IPAR_System sys : systems()) {
          if (sys.systemType()==EPAR_SystemType.LOCAL) {
            for (final IPAR_CPU cpu : sys.cpus(EPAR_CPUType.AVAILABLE)) {
              for (final IPAR_Core core : cpu.cores()) {
                if ((core.coreType()==EPAR_CoreType.AVAILABLE) || (pWish.expectedLoad()==0 && core.coreType()==EPAR_CoreType.ALLOCATED)) {
                  if (pAllocate) {
                    if (core.allocate( pWish.expectedLoad() )) {
                      cores.add( core );
                      nrCores--;
                    }
                  } else {
                    cores.add( core );
                    nrCores--;
                  }
                  if (nrCores<=0) {
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
    return cores;
  }

}
