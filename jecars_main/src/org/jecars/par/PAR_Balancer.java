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

  public static IPAR_Balancer BALANCER() {
    return BALANCER;
  }

  final private AtomicLong mSchedulerLock = new AtomicLong(1);

  final private List<IPAR_System>       mSystems = new ArrayList<>(8);
  final private List<IPAR_ResourceWish> mResources = new ArrayList<>(64);
  final private EnumSet<EPAR_ItemState> mStates = EnumSet.of( EPAR_ItemState.NEEDS_REFRESH );
  
  /** PAR_Balancer
   * 
   */
  private PAR_Balancer() {
  }

  /** currentResources
   * 
   * @return 
   */
  @Override
  public List<IPAR_ResourceWish> currentResources() {
    final List<IPAR_ResourceWish> rws = new ArrayList<>(64);
    synchronized (mResources) {
      rws.addAll( mResources );
    }
    return rws;
  }


  /**
   * systems
   *
   * @return
   * @throws RepositoryException
   */
  @Override
  public List<IPAR_System> systems() throws RepositoryException {
    mStates.remove( EPAR_ItemState.NEEDS_REFRESH );
    final Session ses = CARS_Factory.getSystemAccessSession();
    synchronized( ses ) {
      final NodeIterator ni = ses.getNode("/JeCARS/Systems").getNodes();
      while (ni.hasNext()) {
        final Node n = ni.nextNode();
        if (n.isNodeType("jecars:RES_System")) {
          if (system(n)==null) {
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
   * @param pWish will be copied in the resource list do not reuse!
   * @param pAllocate
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public List<IPAR_Core> coresByWish( final IPAR_ResourceWish pWish, final IPAR_ResourceWish pResult ) throws RepositoryException {
    final List<IPAR_Core> cores = new ArrayList<>(8);
    synchronized (mSchedulerLock) {
      mSchedulerLock.incrementAndGet();
//      int nrCores = pWish.numberOfCores();
      if (pWish.systemType()==EPAR_SystemType.LOCAL) {
        for (final IPAR_System sys : systems()) {
          // **** System check
          if ((sys.systemType()==EPAR_SystemType.LOCAL) &&
              (sys.name().matches( pWish.runOnSystem()))) {
            for (final IPAR_CPU cpu : sys.cpus(EPAR_CPUType.AVAILABLE)) {
              // **** CPU check
              if (cpu.name().matches( pWish.runOnCPU())) {
                for (final IPAR_Core core : cpu.cores()) {
                  
                  // **** Core check
                  if (core.name().matches( pWish.runOnCore() )) {
                    cores.add( core );
                  }
                }
              }
            }
          }
        }
      } else {
        final boolean systemTypeCheck = !(pWish.systemType()==EPAR_SystemType.ALL);
        // **** NON LOCAL systems
        for (final IPAR_System sys : systems()) {
          // **** System check
          if ((sys.systemType()!=EPAR_SystemType.LOCAL || !systemTypeCheck) &&
              (sys.name().matches( pWish.runOnSystem()))) {
            for (final IPAR_CPU cpu : sys.cpus(EPAR_CPUType.AVAILABLE)) {
              // **** CPU check
              if (cpu.name().matches( pWish.runOnCPU())) {
                for (final IPAR_Core core : cpu.cores()) {
                  
                  // **** Core check
                  if (core.name().matches( pWish.runOnCore() )) {
                    cores.add( core );
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

  @Override
  public boolean allocateWishToCore( IPAR_Core pCore, IPAR_ResourceWish pWish ) {
    if (pCore.allocate( pWish, false )) {
      // **** The resource is allocated
      synchronized( mResources ) {
        if (!mResources.contains( pWish )) {
          mResources.add( pWish );
        }
      }
      return true;
    }
    return false;
  }

  
  @Override
  public IPAR_Balancer resourceWishReady( IPAR_ResourceWish pWish ) {
    synchronized( mResources ) {
      mResources.remove( pWish );
    }
    return this;
  }

  
  
}
