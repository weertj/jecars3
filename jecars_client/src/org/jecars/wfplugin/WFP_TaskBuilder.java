/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import nl.msd.jdots.JD_Taglist;
import org.jecars.client.JC_Exception;
import org.jecars.client.nt.JC_ParameterDataNode;
import org.jecars.client.nt.JC_WorkflowNode;
import org.jecars.client.nt.JC_WorkflowTaskNode;
import org.jecars.client.nt.JC_WorkflowTaskPortNode;

/**
 *
 * @author weert
 */
public class WFP_TaskBuilder {

  private WFP_TaskBuilder() {
  }

  
  /** addSleepTask
   * 
   * @param pWorkflow
   * @param pSleepTimeInSecs
   * @param pRepeatTimeSecs
   * @param pExpireContextAfterMin
   * @return    <pre>
   *            tags.putData( "Sleep", sleep );
   *            tags.putData( "Sleep_In0", sleep_in0 );
   *            tags.putData( "Sleep_Out0", sleep_out0 );
   *            </pre>
   * @throws JC_Exception 
   */
  static public JD_Taglist addSleepTask( JC_WorkflowNode pWorkflow, long pSleepTimeInSecs, long pRepeatTimeSecs, long pExpireContextAfterMin ) throws JC_Exception {
 
    JD_Taglist tags = new JD_Taglist();
      
    // ****************************************************
    // **** SLEEP
    JC_WorkflowTaskNode sleep = pWorkflow.addJavaTask( "Sleep", "org.jecars.wfplugin.tools.WFPT_Sleep" );
    sleep.save();
    if (pSleepTimeInSecs>0) {
      JC_ParameterDataNode pdn = sleep.addParameterData( "SleepTimeInSecs" );
      pdn.addParameter( String.valueOf( pSleepTimeInSecs ) );
    }
    if (pRepeatTimeSecs>0) {
      JC_ParameterDataNode pdn2 = sleep.addParameterData( "RepeatTimeSecs" );
      pdn2.addParameter( String.valueOf(pRepeatTimeSecs) );
    }
    if (pExpireContextAfterMin>0) {
      sleep.setExpireContextAfterMinutes( pExpireContextAfterMin );
    }
    sleep.save();
    JC_WorkflowTaskPortNode sleep_in0  = sleep.addInputPort( "In", null, ".*", ".*" );
    JC_WorkflowTaskPortNode sleep_out0 = sleep.addOutputPort( "Out", null, ".*", ".*" );
    tags.putData( "Sleep", sleep );
    tags.putData( "Sleep_In0", sleep_in0 );
    tags.putData( "Sleep_Out0", sleep_out0 );    
    return tags;
  }    
    
}
