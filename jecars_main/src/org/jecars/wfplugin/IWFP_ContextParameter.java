/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

/**
 *
 * @author weert
 */
public interface IWFP_ContextParameter extends IWFP_Node {

  String getStringValue() throws WFP_Exception;
  void   setValue( String pValue ) throws WFP_Exception;
  
}
