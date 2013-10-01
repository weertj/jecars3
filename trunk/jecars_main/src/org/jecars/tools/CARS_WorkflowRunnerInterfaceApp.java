/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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

package org.jecars.tools;

import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Main;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.tools.workflow.WF_WorkflowRunner;

/**
 *
 * @author weert
 */
public class CARS_WorkflowRunnerInterfaceApp extends CARS_DefaultInterface {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools" );

  /** CARS_WorkflowRunnerInterfaceApp
   * 
   */
  public CARS_WorkflowRunnerInterfaceApp() {
    setToBeCheckedInterface( "org.jecars.tools.CARS_WorkflowRunnerInterfaceApp" );
    return;
  }

  @Override
  public void init(CARS_Main pMain, Node pInterfaceNode) throws Exception {
    // **** No reporting
    return;
  }
  
  
  /** isCorrectInterface
   *
   * @param pInterfaceNode
   * @return
   * @throws RepositoryException
   */
  protected boolean isCorrectInterface( final Node pInterfaceNode ) throws RepositoryException {
    if (pInterfaceNode.hasProperty( CARS_DefaultMain.DEF_INTERFACECLASS )) {
      return getToBeCheckedInterface().equals( pInterfaceNode.getProperty( CARS_DefaultMain.DEF_INTERFACECLASS ).getString() );
    }
    return false;
  }
  
  /** setParamProperty
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pPropName
   * @param pValue
   * @return
   * @throws Exception 
   */
  @Override
  public Property setParamProperty(CARS_Main pMain, Node pInterfaceNode, Node pNode, String pPropName, String pValue) throws Exception {
    final Property p = super.setParamProperty( pMain, pInterfaceNode, pNode, pPropName, pValue );
//    if ("jecars:SingleStep".equals(pPropName)) {
//      WF_WorkflowRunner wrun = new WF_WorkflowRunner( pNode );
//      wrun.singleStep();
//    }
    p.getSession().save();
    if ("jecars:COMMAND".equals(pPropName)) {
      try {
        if ("RESTART".equals( p.getString() )) {
          final CARS_Main newmain = pMain.getFactory().createMain( CARS_ActionContext.createActionContext(pMain.getContext()) );
          final Node newrunnerNode = newmain.getSession().getNode( pNode.getPath() );              
          WF_WorkflowRunner wrun = new WF_WorkflowRunner( newmain, newrunnerNode, false );
          wrun.restart( false );
          wrun.destroy();
        } else if ("SINGLESTEP".equals( p.getString() )) {
          final CARS_Main newmain = pMain.getFactory().createMain( CARS_ActionContext.createActionContext(pMain.getContext()) );
          final Node newrunnerNode = newmain.getSession().getNode( pNode.getPath() );              
          WF_WorkflowRunner wrun = new WF_WorkflowRunner( newmain, newrunnerNode, false );
//          WF_WorkflowRunner wrun = new WF_WorkflowRunner( pMain, pNode );
          wrun.singleStep();
          wrun.destroy();
        }
      } finally {
        p.setValue( "READY" );
        p.getSession().save();
      }
    }
    return p;
  }

  
  
  
}
