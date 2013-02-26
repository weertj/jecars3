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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public class WFP_Task extends WFP_Node implements IWFP_Task {

  /** WFP_Task
   * 
   * @param pNode 
   */
  public WFP_Task( final Node pNode ) {
    super( pNode );
    return;
  }

  /** createParameter
   * 
   * @param pName
   * @return
   * @throws WFP_Exception 
   */
  @Override
  public IWFP_Parameter createParameter( final String pName ) throws WFP_Exception {
    try {
      final Node n = getNode().addNode( pName, "jecars:parameterdata" );
      n.setProperty( "jcr:data", "" );
      final IWFP_Parameter par = new WFP_Parameter( n );
      return par;
    } catch( RepositoryException re ) {
//    re.printStackTrace();
      throw new WFP_Exception( re );
    }
  }

    
}
