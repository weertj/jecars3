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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Factory;

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

  /**
   * PAR_ToolRun
   *
   * @param pName
   * @param pRunnable
   */
  public PAR_ToolRun(final String pName, final Runnable pRunnable) throws UnknownHostException {
    mName = pName;
    mExec = new PAR_Execute( this, pRunnable );
    init();
    return;
  }

  /**
   * PAR_ToolRun
   *
   * @param pName
   * @param pCallable
   */
  public PAR_ToolRun(final String pName, final Callable<E> pCallable) throws UnknownHostException {
    mName = pName;
    mExec = new PAR_Execute( this, pCallable );
    init();
    return;
  }

  private void init() throws UnknownHostException {
    mResourceWish = new PAR_ResourceWish().system("").numberOfCores(1).expectedLoad(0);
    return;
  }

  @Override
  public String name() {
    return mName;
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
      System.out.println("RUN - " + mName + " = " + Thread.currentThread().getName());

//      final Session sysSession = CARS_Factory.getSystemAccessSession();
        if (mResourceWish.numberOfCores() == 1) {
          try {
            final List<IPAR_Core> cores = bal.coresByWish( mResourceWish, true );
            if (!cores.isEmpty()) {
              final IPAR_Core core = cores.get(0);
              try {
                core.execute( mExec, mResourceWish );
              } catch( ExecutionException | RepositoryException | InterruptedException e ) {
                e.printStackTrace();
              } finally {
                core.release( mResourceWish.expectedLoad() );
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
      System.out.println("CALL - " + mName + " = " + Thread.currentThread().getName());

//      final Session sysSession = CARS_Factory.getSystemAccessSession();
        if (mResourceWish.numberOfCores() == 1) {
          try {
            final List<IPAR_Core> cores = bal.coresByWish(mResourceWish, true);
            if (!cores.isEmpty()) {
              final IPAR_Core core = cores.get(0);
              try {
                result = (E)core.execute( mExec, mResourceWish );
              } catch( ExecutionException | RepositoryException | InterruptedException e ) {
                e.printStackTrace();
              } finally {
                core.release( mResourceWish.expectedLoad() );
              }
            } else {
              mExec.runnable().run();              
            }
          } catch (RepositoryException re) {
            mExec.runnable().run();
          }
        }
    }
    return result;
  }

}
