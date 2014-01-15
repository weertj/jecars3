/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.wfplugin;

/**
 *
 * @author weert
 */
public interface IWFP_InterfaceResult {
    
  boolean   hasState( final WFP_InterfaceResult.STATE pState );
  Throwable getError();

}
