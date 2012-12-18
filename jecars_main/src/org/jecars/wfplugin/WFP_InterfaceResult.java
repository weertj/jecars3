/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import java.util.EnumSet;

/**
 *
 * @author weert
 */
public class WFP_InterfaceResult {

  static public enum STATE {OK,STOP,THREADDEATH,ERROR};
    
//  static public final WFP_InterfaceResult OK   = new WFP_InterfaceResult( true );
//  static public final WFP_InterfaceResult STOP = new WFP_InterfaceResult( false );
  
  final private transient EnumSet<STATE> mState = EnumSet.noneOf( STATE.class );

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
      mState.add( STATE.OK );
    } else {
      mState.add( STATE.STOP );
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
      mState.add( STATE.OK );
    } else {
      mState.add( STATE.STOP );
    }
    if (pThreadDeath) {
      mState.add( STATE.THREADDEATH );
    }
    return;
  }

  public void clear() {
    mState.clear();
    return;
  }
  
  public WFP_InterfaceResult replaceBy( WFP_InterfaceResult pIR ) {
    mState.clear();
    mState.addAll( pIR.getStates() );
    return this;
  }
  
  public EnumSet<STATE> getStates() {
    return mState;
  }

  public WFP_InterfaceResult setState( final STATE pState ) {
    clear();
    addState( pState );
    return this;
  }
  
  public WFP_InterfaceResult addState( final STATE pState ) {
    mState.add( pState );
    return this;
  }
  
  public boolean isContinueWorkflow() {
    return mState.contains( STATE.OK );
  }

  public boolean isThreadDeath() {
    return mState.contains( STATE.THREADDEATH );
  }
    
}
