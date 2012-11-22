/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import java.io.InputStream;
import java.util.List;

/**
 *
 * @author weert
 */
public interface IWFP_Property {
  
  String        getName()           throws WFP_Exception;
  String        getStringValue()    throws WFP_Exception;
  InputStream   getStreamValue()    throws WFP_Exception;
  List<Object>  getValues()         throws WFP_Exception;

}
