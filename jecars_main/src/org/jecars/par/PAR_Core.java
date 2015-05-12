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
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.jecars.tools.CARS_ThreadFactory;

/**
 *
 * @author weert
 */
public class PAR_Core<E> extends PAR_DefaultNode implements IPAR_Core<E> {
   
  static public final int MAX_STORED_EXEC_FINISHED = 500;
  
  private       EPAR_CoreType           mCoreType = EPAR_CoreType.UNKNOWN;
  private final List<IPAR_ResourceWish> mExecWishes = new ArrayList<>(16);
  private final Queue<IPAR_Execute<E>>  mExecQueue = new PriorityBlockingQueue<>();
  private final List<IPAR_Execute<E>>   mExecRunning = new ArrayList<>(16);
  private final LinkedList<IPAR_Execute<E>> mExecFinished = new LinkedList<>();
  private final AtomicInteger           mCurrentRunning = new AtomicInteger(0);
  private final AtomicInteger           mReadyRunning   = new AtomicInteger(0);
  
  private       double              mExpectedLoad = 0.0;
  private       double              mCurrentLoad = 0.0;
  private       double              mMaxLoad = 1.0;
  private       ExecutorService     mExecutorService = null;

//  public final Object CORELOCK = new Object();
  
  /** PAR_Core
   * 
   * @param pNode
   * @throws RepositoryException 
   */
  public PAR_Core( final Node pNode ) throws RepositoryException {
    super( pNode );
    mCoreType = EPAR_CoreType.AVAILABLE;
    return;
  }

  @Override
  public EPAR_CoreType coreType() {
    return mCoreType;
  }

  /** allocate
   * 
   * @param pLoad
   * @return 
   */
  @Override
  public boolean allocate( final IPAR_ResourceWish pWish, boolean pAcceptOverload ) {
    double load = pWish.expectedLoad();
    if (load==0) {
      mExecWishes.add( pWish );
      return true;
    }
    if ((mExpectedLoad+load)<=mMaxLoad) {
      mExpectedLoad += load;
      if (mExpectedLoad>=mMaxLoad) {
        mCoreType = EPAR_CoreType.ALLOCATED;
      }      
      mExecWishes.add( pWish );      
      return true;
    } else {
      if (pAcceptOverload) {
        // **** Overloading is acceptable
        if (mExpectedLoad<=mMaxLoad) {
          mExecWishes.add( pWish );
          mExpectedLoad += load;
        }
        if (mExpectedLoad>=mMaxLoad) {
          mCoreType = EPAR_CoreType.ALLOCATED;
        }
        return true;        
      }
    }
    return false;
  }

  @Override
  public void release( final IPAR_ResourceWish pWish ) {
    mExecWishes.remove( pWish );
    double load = pWish.expectedLoad();
    mExpectedLoad -= load;
    mExpectedLoad = Math.max( 0, mExpectedLoad );
    if (mExpectedLoad>=mMaxLoad) {
      mCoreType = EPAR_CoreType.ALLOCATED;
    } else {
      mCoreType = EPAR_CoreType.AVAILABLE;      
    }
    return;
  }

  
  @Override
  public long readyRunning() {
    return mReadyRunning.get();
  }

  @Override
  public long currentRunning() {
    return mCurrentRunning.get();
  }

  private ExecutorService executorService() throws RepositoryException {
    if (mExecutorService==null) {
      mExecutorService = Executors.newCachedThreadPool( new CARS_ThreadFactory( node().getPath(), Thread.MIN_PRIORITY ));
    }
    return mExecutorService;
  }
  
  /** execute
   * 
   * @param pExec
   * @param pWish
   * @return 
   * @throws InterruptedException 
   * @throws javax.jcr.RepositoryException 
   * @throws java.util.concurrent.ExecutionException 
   */
  @Override
  public E execute( final IPAR_Execute<E> pExec, final IPAR_ResourceWish pWish ) throws InterruptedException, RepositoryException, ExecutionException {
    E result = null;

    synchronized( mExecQueue ) {
      mExecQueue.offer( pExec );
    }
    IPAR_Execute<E> runExec = null;
    
    boolean waitForExec = true;
    while( waitForExec ) {
      synchronized( mExecQueue ) {
        if (mCurrentLoad<=1 && mExecQueue.peek()==pExec) {
          waitForExec = false;
          mCurrentLoad += pWish.expectedLoad();
          runExec = mExecQueue.poll();
        }
      }
      if (waitForExec) {
        synchronized( mExecRunning ) {
//      System.out.println("Start Peeking on " + mExecQueue.size() + " - " + mExecWishes.size() + " load: " + mCurrentLoad );
          mExecRunning.wait( 2000 );
        }
      }
    }


//    while( ((runExec = mExecQueue.poll())!=pExec) || (mCurrentLoad>=1) ) {
//      mExecQueue.offer( runExec );
//      if (mExecWishes.contains(pWish) || mCurrentLoad<=0) {
//        // **** Our wish was granted....start
//        System.out.println("WISH WAS GRANTED " + pExec );
//        runExec = pExec;
//        mExecQueue.remove( pExec );
//        break;
//      }
//      synchronized( mExecRunning ) {
//      System.out.println("Start Peeking on " + mExecQueue.size() + " - " + mExecWishes.size() + " load: " + mCurrentLoad );
//        mExecRunning.wait( 2000 );
//      }
//    }
    
    // **** Add the wish which is being executed, (if not already there)
    if (!mExecWishes.contains( pWish )) {
      mExecWishes.add( pWish );
    }
//    final IPAR_Execute<E> exec;
//    synchronized( mExecQueue ) {
//      exec = mExecQueue.poll();
//    }
    // *************************************************************************
    // **** Check for runnable
    if (runExec.runnable()!=null) {
//      try {
//      System.out.println("Running on " + node().getPath() + " -> " + runExec.toolRun().name() );
//      } catch( RepositoryException e ) {
//        e.printStackTrace();
//      }
      synchronized( this ) {
        mCurrentRunning.incrementAndGet();
        mExecRunning.add( runExec );
      }
      try {
        pExec.toolRun().started( Calendar.getInstance() );
        executorService().submit( runExec.runnable() ).get();
      } finally {        
        synchronized( mExecRunning ) {
          mCurrentLoad -= pWish.expectedLoad();
          mExecRunning.notifyAll();
        }
        synchronized( this ) {
          mExecRunning.remove( runExec );
          mCurrentRunning.decrementAndGet();
          mReadyRunning.incrementAndGet();
          pExec.toolRun().finished( Calendar.getInstance() );
//          if (pExec.toolRun().resourceWish().expectedLoad()>0) {
        mExecFinished.remove( pExec );
        mExecFinished.addFirst( pExec );
        while( mExecFinished.size()>MAX_STORED_EXEC_FINISHED ) {
          mExecFinished.removeLast();
        }
//          }
        }
      }
    }
    // *************************************************************************
    // **** Check for callable
    if (runExec.callable()!=null) {
//      try {
//      System.out.println("Running (call) on " + node().getPath() + " -> " + runExec.toolRun().name() );
//      } catch( RepositoryException e ) {
//        e.printStackTrace();
//      }
      synchronized( this ) {
        mCurrentRunning.incrementAndGet();
        mExecRunning.add( runExec );
      }
      try {
//        System.out.println("START RUNNING " + runExec.id() );
        pExec.toolRun().started( Calendar.getInstance() );
        result = executorService().submit( runExec.callable() ).get();
//        System.out.println("END RUNNING " + runExec.id() );
      } finally {
        synchronized( mExecRunning ) {
          mCurrentLoad -= pWish.expectedLoad();
          mExecRunning.notify();
        }
        synchronized( this ) {
          mExecRunning.remove( runExec );
          mCurrentRunning.decrementAndGet();
          mReadyRunning.incrementAndGet();
          pExec.toolRun().finished( Calendar.getInstance() );
//          if (pExec.toolRun().resourceWish().expectedLoad()>0) {
          mExecFinished.remove( pExec );
          mExecFinished.addFirst( pExec );
          while( mExecFinished.size()>MAX_STORED_EXEC_FINISHED ) {
            mExecFinished.removeLast();
          }
//          }
        }
      }
    }
    return result;
  }

  @Override
  public double currentLoad() {
    return mCurrentLoad;
  }

  @Override
  public double expectedLoad() {
    return mCurrentLoad;
  }

  @Override
  public double maxLoad() {
    return mMaxLoad;
  }

  @Override
  public List<IPAR_Execute<E>> runningExecs() {
    synchronized( this ) {
      return new ArrayList<>( mExecRunning );
    }
  }

  @Override
  public List<IPAR_Execute<E>> queuedExecs() {
    synchronized( this ) {
      return new ArrayList<>( mExecQueue );
    }
  }

  @Override
  public LinkedList<IPAR_Execute<E>> finishedExecs() {
    synchronized( this ) {
      return new LinkedList<>( mExecFinished );
    }    
  }
  
  @Override
  public void removeQueuedExecs( final List<IPAR_Execute<E>> pExecs ) {
    synchronized( this ) {
      mExecFinished.removeAll( pExecs );
    }
    return;
  }
  
  @Override
  public void removeRunningExecs( final List<IPAR_Execute<E>> pExecs ) {
    synchronized( this ) {
      mExecFinished.removeAll( pExecs );
    }
    return;
  }

  
  @Override
  public void removeFinishedExecs( final List<IPAR_Execute<E>> pExecs ) {
    synchronized( this ) {
      mExecFinished.removeAll( pExecs );
    }
    return;
  }

  
 
}
