/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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
package org.jecars.wfplugin;

import java.util.EnumMap;

/**
 *
 * @author weert
 */
public class WFP_InterfaceResult implements IWFP_InterfaceResult {

  static public enum STATE {OK,STOP,THREADDEATH,ERROR,OUTPUT_DECISION};
    
//  static public final WFP_InterfaceResult OK   = new WFP_InterfaceResult( true );
//  static public final WFP_InterfaceResult STOP = new WFP_InterfaceResult( false );
  
  final private transient EnumMap<STATE,String> mState = new EnumMap<STATE,String>(STATE.class);
  private transient Throwable mError = null;

  public WFP_InterfaceResult() {
    return;
  }
  
  /** WFP_InterfaceResult
   * 
   * @param pContinueWorkflow 
   */
  @Deprecated
  public WFP_InterfaceResult( boolean pContinueWorkflow ) {
    if (pContinueWorkflow) {
      addState( STATE.OK );
    } else {
      addState( STATE.STOP );
    }
    return;
  }

  static public WFP_InterfaceResult OK() {
    return new WFP_InterfaceResult().addState( STATE.OK );
  }

  static public WFP_InterfaceResult STOP() {
    return new WFP_InterfaceResult().addState( STATE.STOP );
  }

  static public WFP_InterfaceResult ERROR() {
    return new WFP_InterfaceResult().addState( STATE.ERROR );
  }

  static public WFP_InterfaceResult STOP_THREADDEATH() {
    return new WFP_InterfaceResult().addState( STATE.STOP ).addState( STATE.THREADDEATH );
  }
  
  /** WFP_InterfaceResult
   * 
   * @param pContinueWorkflow
   * @param pThreadDeath 
   */
  @Deprecated
  public WFP_InterfaceResult( boolean pContinueWorkflow, boolean pThreadDeath) {
    if (pContinueWorkflow) {
      addState( STATE.OK );
    } else {
      addState( STATE.STOP );
    }
    if (pThreadDeath) {
      addState( STATE.THREADDEATH );
    }
    return;
  }

  public void clear() {
    mState.clear();
    return;
  }
  
  public WFP_InterfaceResult replaceBy( WFP_InterfaceResult pIR ) {
    mState.clear();
    mState.putAll( pIR.mState );
    mError = pIR.getError();
    return this;
  }

  public boolean isDecision() {
    return mState.containsKey( STATE.OUTPUT_DECISION );
  }
  
  public boolean hasState( final STATE pState ) {
    return mState.containsKey( pState );
  }
  
  public String getStateValue( final STATE pState ) {
    return mState.get( pState );
  }
  
  public WFP_InterfaceResult setState( final STATE pState ) {
    clear();
    addState( pState );
    return this;
  }
  
  final public WFP_InterfaceResult addState( final STATE pState ) {
    mState.put( pState, "" );
    return this;
  }

  final public WFP_InterfaceResult addState( final STATE pState, final String pValue ) {
    mState.put( pState, pValue );
    return this;
  }
  
  public boolean isContinueWorkflow() {
    return mState.containsKey( STATE.OK );
  }

  public boolean isThreadDeath() {
    return mState.containsKey( STATE.THREADDEATH );
  }
  
  public WFP_InterfaceResult setError( final Throwable pT ) {
    mError = pT;
    return this;
  }

  @Override
  public Throwable getError() {
    return mError;
  }
    
}
