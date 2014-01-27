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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author weert
 */
public class PAR_Execute<E> implements IPAR_Execute, Comparable<Integer> {
  
  static private final AtomicLong COUNTER = new AtomicLong(1);
  
  private final String          mID;
  private final Integer         mPriority = new Integer( 1 );
  private final IPAR_ToolRun<E> mToolRun;
  private final Runnable        mRunnable;
  private final Callable<E>     mCallable;

  public PAR_Execute( final IPAR_ToolRun pToolRun, final Runnable pRunnable ) {
    mRunnable = pRunnable;
    mCallable = null;
    mToolRun  = pToolRun;
    mID       = "PAR_Execute_" + COUNTER.getAndIncrement();
    return;
  }

  public PAR_Execute( final IPAR_ToolRun pToolRun, final Callable pCallable ) {
    mRunnable = null;
    mCallable = pCallable;
    mToolRun  = pToolRun;
    mID       = "PAR_Execute_" + COUNTER.getAndIncrement();
    return;
  }

  @Override
  public String id() {
    return mID;
  }

  @Override
  public Callable<E> callable() {
    return mCallable;
  }
  
  @Override
  public Runnable runnable() {
    return mRunnable;
  }

  @Override
  public IPAR_ToolRun<E> toolRun() {
    return mToolRun;
  }
  
  
  
  @Override
  public int compareTo(Integer o) {
    return mPriority.compareTo( o );
  }
  
}
