/*
 * Copyright 2014 NLR - National Aerospace Laboratory
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

import org.jecars.tools.workflow.EWF_RunnerInstruction;
import org.jecars.tools.workflow.IWF_WorkflowRunner;

/** CARS_WorkflowController
 *
 * @author weert
 */
public class CARS_WorkflowController {
  
  /** stopRunners
   * 
   * @param pToolPath 
   */
  static public void stopRunners( final String pToolPath ) {
    // **** Ask Workflow runners to stop
    for( IWF_WorkflowRunner wr : CARS_DefaultWorkflow.getRunners( pToolPath ) ) {
      wr.addInstruction( EWF_RunnerInstruction.STOP );
    }
    return;
  }

  
}
