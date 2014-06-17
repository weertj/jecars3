/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin.tools;

import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_Exception;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_InsertWorkflowTask implements IWFP_Interface {

  @Override
  public WFP_InterfaceResult start(final IWFP_Tool pTool, final IWFP_Context pContext) {
    try {
      WFPT_InsertWorkflowHelper wh = new WFPT_InsertWorkflowHelper();
      wh.insert1Workflow(pTool, pContext);      
      return WFP_InterfaceResult.OK();
    } catch (Exception e) {
      return WFP_InterfaceResult.ERROR().setError(e);
    } finally {
      try {
        pTool.save();
      } catch (WFP_Exception e) {
        return WFP_InterfaceResult.ERROR().setError(e);
      }
      try {
        pContext.save();
      } catch (WFP_Exception e) {
        return WFP_InterfaceResult.ERROR().setError(e);
      }
    }
  }
}
