/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public class WFP_Parameter extends WFP_ContextParameter implements IWFP_Parameter {

  public WFP_Parameter(Node pNode) throws RepositoryException {
    super(pNode);
  }
    
}
