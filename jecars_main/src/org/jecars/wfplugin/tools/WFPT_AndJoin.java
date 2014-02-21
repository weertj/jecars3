/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.wfplugin.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_AndJoin implements IWFP_Interface {

//  final private CyclicBarrier mBarrier = new CyclicBarrier(2);
  final private static ConcurrentMap<String, CyclicBarrier> BARRIERS    = new ConcurrentHashMap<String, CyclicBarrier>();
  final private static ConcurrentMap<String, Thread>        FIRSTTHREAD = new ConcurrentHashMap<String, Thread>();

  final private static ConcurrentMap<String, List<IWFP_Input>>            ANDCONTEXTOBJECTS = new ConcurrentHashMap<>();
  final private static ConcurrentMap<String, List<IWFP_ContextParameter>> ANDCONTEXTOBJECTSPARAMS = new ConcurrentHashMap<>();
//  private Thread mFirstThread = null;
  
  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
//      System.out.println("----- AND JOIN ------ " + Thread.currentThread() );
    final String taskPath = pTool.getTaskPath();
    final Thread ct = FIRSTTHREAD.putIfAbsent( taskPath, Thread.currentThread() );
    BARRIERS.putIfAbsent( taskPath, new CyclicBarrier(2) );
    
    try {
      if (ct!=null) {
        // **** Only store the 2,3,4th... thread
        
        synchronized( ANDCONTEXTOBJECTS ) {
          List<IWFP_Input>inputs = ANDCONTEXTOBJECTS.get( taskPath );
          if (inputs==null) {
            ANDCONTEXTOBJECTS.put( taskPath, new ArrayList<IWFP_Input>() );
            inputs = ANDCONTEXTOBJECTS.get( taskPath );
          }
          // **** Copy input to the source input
          for( final IWFP_Input input : pContext.getInputs() ) {
            inputs.add( input );
          }
        }
        
        synchronized( ANDCONTEXTOBJECTSPARAMS ) {
          List<IWFP_ContextParameter>params = ANDCONTEXTOBJECTSPARAMS.get( taskPath );
          if (params==null) {
            ANDCONTEXTOBJECTSPARAMS.put( taskPath, new ArrayList<IWFP_ContextParameter>() );
            params = ANDCONTEXTOBJECTSPARAMS.get( taskPath );
          }
          // **** Copy parameters to the source input
          for( final IWFP_ContextParameter cpar : pContext.getParameters() ) {
            params.add( cpar );
          }
        }
      
      }
//        System.out.println("ENTER BARRIER AWAIT");
      BARRIERS.get( taskPath ).await();
//        System.out.println("************************ EXIT BARRIER AWAIT");
      if (!FIRSTTHREAD.get( taskPath ).equals(Thread.currentThread())) {
        // **** Wait until the entry in the FIRSTTHREAD is gone, the main thread has left the task
        // **** and then it is save for the deading threads to finish
        while( FIRSTTHREAD.get( taskPath )!=null ) {
          Thread.sleep( 2000 );
        }
        return WFP_InterfaceResult.STOP_THREADDEATH();//new WFP_InterfaceResult( false, true );
      }
      
      // **** Copy the gathered inputs to the new context
      for( final IWFP_Input input : ANDCONTEXTOBJECTS.get( taskPath ) ) {
        pContext.copyInput( input );
      }

      // **** Copy the gathered parameters to the new context
      for( final IWFP_ContextParameter param : ANDCONTEXTOBJECTSPARAMS.get( taskPath ) ) {
        pContext.copyParameter( param );
      }
      
      synchronized( ANDCONTEXTOBJECTS ) {
        BARRIERS.remove( taskPath );
        ANDCONTEXTOBJECTS.remove( taskPath );
        FIRSTTHREAD.remove( taskPath );
      }
      synchronized( ANDCONTEXTOBJECTSPARAMS ) {
        ANDCONTEXTOBJECTSPARAMS.remove( taskPath );        
      }
      
      
    } catch( Exception e ) {
      pTool.reportException( Level.SEVERE, e);
      return WFP_InterfaceResult.STOP();
    }
    return WFP_InterfaceResult.OK();
  }
    
}
