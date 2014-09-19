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

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.RepositoryException;
import org.jecars.wfplugin.IWFP_InterfaceResult;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 * @param <E>
 */
public class PAR_ToolRun<E> implements IPAR_ToolRun<E> {

  private static final Logger LOG = Logger.getLogger("org.jecars.par");

  private final String           mName;
  private final IPAR_Execute<E>  mExec;

  private IPAR_ResourceWish mResourceWish;

  /** PAR_ToolRun
   * 
   * @param pName
   * @param pRunnable
   * @param pRW
   * @throws UnknownHostException 
   */
  public PAR_ToolRun( final String pName, final Runnable pRunnable, final IPAR_ResourceWish pRW ) throws UnknownHostException {
    mName         = pName;
    mExec         = new PAR_Execute<>( this, pRunnable );
    mResourceWish = pRW;
    return;    
  }


  /** PAR_ToolRun
   * 
   * @param pName
   * @param pCallable
   * @param pRW
   * @throws UnknownHostException 
   */
  public PAR_ToolRun( final String pName, final Callable<E> pCallable, final IPAR_ResourceWish pRW ) throws UnknownHostException {
    mName         = pName;
    mExec         = new PAR_Execute<>( this, pCallable );
    mResourceWish = pRW;
    return;    
  }

  /** name
   * 
   * @return 
   */
  @Override
  public String name() {
    return mName;
  }

  /** resourceWish
   * 
   * @return 
   */
  @Override
  public IPAR_ResourceWish resourceWish() {
    return mResourceWish;
  }

  
  
  /**
   * run
   *
   */
  @Override
  public void run() {
    final IPAR_Balancer bal = PAR_Balancer.BALANCER();
    if (mExec.runnable() == null) {
      LOG.log(Level.WARNING, "{0}: Runnable is null", mName);
    } else {
      // ***********************************************************************
      // **** Runnable
//      System.out.println("RUN - " + mName + " = " + Thread.currentThread().getName());

//      final Session sysSession = CARS_Factory.getSystemAccessSession();
        if (mResourceWish.numberOfCores() == 1) {
          try {
            final IPAR_ResourceWish resultWish = new PAR_ResourceWish();
            final List<IPAR_Core> cores = bal.coresByWish( mResourceWish, resultWish );
            if (!cores.isEmpty()) {              
              IPAR_Core core = null;
              try {
                core = getBestCore( cores );
                if (!bal.allocateWishToCore( core, mResourceWish )) {
                  // **** Didn't work.... add it as queue
                  core.allocate( mResourceWish, true );
                }
                core.execute( mExec, mResourceWish );
              } catch( ExecutionException | RepositoryException | InterruptedException e ) {
                e.printStackTrace();
              } finally {
                // **** Release core
                if (core!=null) {
                  core.release( mResourceWish );
                }
                // **** Release resourceWish
                bal.resourceWishReady( mResourceWish );
              }
            } else {
              mExec.runnable().run();              
            }
          } catch (RepositoryException re) {
            mExec.runnable().run();
          }
        }
    }
    return;
  }

  /** getBestCore
   * 
   * @param pCores
   * @return 
   */
  private IPAR_Core getBestCore( final List<IPAR_Core> pCores ) {
    IPAR_Core core = null;
    double cl = 999999;
    for( final IPAR_Core c : pCores ) {
      if (c.currentLoad()<=cl) {
        core = c;
        cl = c.currentLoad();
      }
    }
    return core;
  }
  
  /** call
   * 
   * @return
   * @throws Exception 
   */
  @Override
  public E call() throws Exception {
    E result = null;
    final IPAR_Balancer bal = PAR_Balancer.BALANCER();
    if (mExec.callable()==null) {
      LOG.log(Level.WARNING, "{0}: Callable is null", mName);
    } else {
      // ***********************************************************************
      // **** Callable
//      System.out.println("CALL - " + mName + " = " + Thread.currentThread().getName());

//      final Session sysSession = CARS_Factory.getSystemAccessSession();
        if (mResourceWish.numberOfCores() == 1) {
          try {
            final IPAR_ResourceWish resultWish = new PAR_ResourceWish();
            final List<IPAR_Core> cores = bal.coresByWish( mResourceWish, resultWish );
            if (!cores.isEmpty()) {
              IPAR_Core core = null;
              try {
                core = getBestCore( cores );
                if (!bal.allocateWishToCore( core, mResourceWish )) {
                  // **** Didn't work.... add it as queue
                  core.allocate( mResourceWish, true );
                }
                mResourceWish.toolInterface().reportStatusMessage( "Running at core: " + core.node().getPath() );
                mResourceWish.toolInterface().setConfigResourceWithByCoreNode( core.node() );                
                result = (E)core.execute( mExec, mResourceWish );
              } catch( Throwable e ) {
                e.printStackTrace();
              } finally {
                if (core!=null) {
                  core.release( mResourceWish );
                }
                // **** Release resourceWish
                bal.resourceWishReady( mResourceWish );
              }
            } else {
              if (mResourceWish.mustFollowWish()) {
                // **** Cannot allocate resource for running
                mResourceWish.toolInterface().reportMessage( Level.SEVERE, "Cannot allocate core for resourcewish: " + mResourceWish, false );
                mResourceWish.toolInterface().reportException( new Exception( "Cannot allocate core for resourcewish: " + mResourceWish ), Level.SEVERE );
                result = (E)WFP_InterfaceResult.ERROR();                
              } else {
                result = mExec.callable().call();              
              }
            }
          } catch (RepositoryException re) {
            mResourceWish.toolInterface().reportException( re, Level.WARNING );
            result = mExec.callable().call();
          }
        }
    }
    return result;
  }

  
  
}
