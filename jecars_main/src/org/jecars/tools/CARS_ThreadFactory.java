/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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
package org.jecars.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** CARS_ThreadFactory
 *
 */
public class CARS_ThreadFactory implements ThreadFactory {

  private static final AtomicInteger gCount = new AtomicInteger(1);

  private final String mBaseName;
  private final int    mBasePriority;
  
  public CARS_ThreadFactory( final String pName, final int pPriority ) {
    mBaseName = pName;
    mBasePriority = pPriority;
    return;
  }
  
  /** newThread
   * 
   * @param pRun
   * @return 
   */
  @Override
  public Thread newThread( final Runnable pRun ) {
    final Thread t;
//    if (pRun instanceof CARS_DefaultToolInterface.ToolRunnable) {
//      CARS_DefaultToolInterface.ToolRunnable trun = (CARS_DefaultToolInterface.ToolRunnable)pRun;
//      CARS_DefaultToolInterface ti = trun.getToolInterface();
//      String name;
//      try {
//        name = mBaseName + '-' + gCount.incrementAndGet() + '-' + ti.getName();
//      } catch(Exception e ) {
//        name = mBaseName + '-' + gCount.incrementAndGet();
//      }
//      t = new Thread( pRun, name );
//      t.setPriority( ti.getThreadPriority() );
//    } else {
      final String name = mBaseName + '-' + gCount.incrementAndGet();
      t = new Thread( pRun, name );
      t.setPriority( mBasePriority );
//    }
    return t;
  }
    
}
