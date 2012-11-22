/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client;

import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author weert
 */
public interface IJC_Retriever {

  JC_Rights getRights(       final JC_DefaultNode pDNode, final InputStream pIS ) throws JC_Exception;
  void      parseChildNodes( final JC_DefaultNode pDNode, final InputStream pIS, final JC_Clientable pClient, final Collection<JC_Nodeable>pNodes ) throws JC_Exception;
  void      populateNode(    final JC_DefaultNode pDNode, final InputStream pIS ) throws JC_Exception;

}
