/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import java.util.EnumMap;

/**
 *
 * @author weert
 */
public class WFP_InterfaceResult {

  static public enum STATE {OK,STOP,THREADDEATH,ERROR,OUTPUT_DECISION};
    
//  static public final WFP_InterfaceResult OK   = new WFP_InterfaceResult( true );
//  static public final WFP_InterfaceResult STOP = new WFP_InterfaceResult( false );
  
  final private transient EnumMap<STATE,String> mState = new EnumMap<STATE,String>(STATE.class);

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
    
}