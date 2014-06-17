/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin.tools;

import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Input;
import org.jecars.wfplugin.IWFP_Tool;

/**
 *
 * @author weert
 */
public class WFPT_InsertWorkflowHelper {

  final public static String PARAMETER_WORKFLOWPATH = "Par_WorkflowPath";

  /**
   * For each input, insert a workflow instance between exit and current pTool.
   * The workflow is defined by parameter {@link #PARAMETER_WORKFLOWPATH}
   *
   * @param pTool
   * @param pContext
   * @param inputs
   * @throws Exception
   */
  public void insertWorkflow4EachInput(final IWFP_Tool pTool, final IWFP_Context pContext, List<IWFP_Input> inputs) throws Exception {
    int seqNo = 0;
    for (IWFP_Input input : inputs) {
      insert1Workflow(pTool, pContext,
              ".*", // any type 
              escapeRegexSpecials(input.getName()), // workflow will only accept this input name
              seqNo);
      seqNo++;
    }
  }

  private String escapeRegexSpecials(String in) {
    return in.replace(".", "\\.").replace("$", "\\$");
  }

  /**
   * @param pTool
   * @param pContext
   * @throws Exception
   */
  public void insert1Workflow(final IWFP_Tool pTool, final IWFP_Context pContext) throws Exception {
    insert1Workflow(pTool, pContext, ".*", ".*", 0);
    return;
  }

  /**
   *
   * @param pTool
   * @param pContext
   * @param wflInputNodeType node type for inserted workflow input port (e.g.
   * ".*")
   * @param wflInputNodeName node name for inserted workflow input port (e.g.
   * ".*")
   * @param seqNo will be added to names of created nodes (in order to make them
   * unique)
   * @throws Exception
   */
  private void insert1Workflow(final IWFP_Tool pTool, final IWFP_Context pContext, String wflInputNodeType, String wflInputNodeName, int seqNo) throws Exception {
    // get template definition
    final IWFP_ContextParameter par_wfl_path = pContext.getParameter(PARAMETER_WORKFLOWPATH);
    if (par_wfl_path == null || par_wfl_path.getStringValue() == null || par_wfl_path.getStringValue().isEmpty()) {
      throw new Exception("Failed to get parameter=" + PARAMETER_WORKFLOWPATH + " from context=" + pContext.getContextNode().getPath());
    }
    final String workflowTemplatePath = par_wfl_path.getStringValue();

    final Node taskNode = (Node) pTool.getTaskAsNode().getNodeObject();
    final Node tasksNode = taskNode.getParent();
    final Node linksNode = tasksNode.getParent().getNode("links");

    // check that exit node exists, and get input port
    if (!tasksNode.hasNode("Exit")) {
      throw new Exception("exit node not found");
    }
    final Node exit = tasksNode.getNode("Exit");
    final Node exitInPort = getUniqueChild(exit.getNode("inputs"));

    // get output port of current taskNode
    Node taskNodeOutPort = getUniqueChild(taskNode.getNode("outputs"));

    // insert the subworkflow task and ports
    final String insertedWflName = "insertedSubWorkflow" + seqNo;
    final Node workflowTask = tasksNode.addNode(insertedWflName, "jecars:workflowtask");
    workflowTask.setProperty("jecars:taskpath", workflowTemplatePath);
    workflowTask.setProperty("jecars:type", "WORKFLOW");
    final Node workflowTaskInputs = workflowTask.getNode("inputs"); // an autocreated node - so, should exist
    final Node workflowTaskInPort = workflowTaskInputs.addNode("insertedSubWorkflowIn", "jecars:workflowtaskport");
    workflowTaskInPort.setProperty("jecars:nodetype", wflInputNodeType);
    workflowTaskInPort.setProperty("jecars:nodename", wflInputNodeName);
    final Node workflowTaskOutputs = workflowTask.getNode("outputs"); // an autocreated node - so, should exist
    final Node workflowTaskOutPort = workflowTaskOutputs.addNode("insertedSubWorkflowOut", "jecars:workflowtaskport");
    workflowTaskOutPort.setProperty("jecars:nodetype", ".*");
    workflowTaskOutPort.setProperty("jecars:nodename", ".*");

    // insert link from current tool to workflowTask & from workflowtask to Exit
    WFPT_InsertWorkflowHelper.insertLink("link2InsertedWfl" + seqNo, linksNode, taskNode, taskNodeOutPort, workflowTask, workflowTaskInPort);
    WFPT_InsertWorkflowHelper.insertLink("linkInsertedWfl2Exit" + seqNo, linksNode, workflowTask, workflowTaskOutPort, exit, exitInPort);
    pTool.save();

  }

  /**
   * add link to linksNode from fromTask to toTask
   *
   * @param linksNode
   * @param fromTask
   * @param toTask
   * @return
   * @throws RepositoryException
   */
  public static Node insertLink(String linkName, final Node linksNode, Node fromTask, Node fromTaskOutputPort, Node toTask, Node toTaskInputPort) throws RepositoryException {
    final Node link = linksNode.addNode(linkName, "jecars:workflowlink");
//        linksNode.save();
    final Node link_from = link.addNode("from", "jecars:workflowlinkendpoint");
    link_from.setProperty("jecars:endpoint", fromTask.getPath());
//        link.save();
    final Node outPort = link_from.addNode(fromTaskOutputPort.getName(), "jecars:workflowtaskportref");
    outPort.setProperty("jecars:portref", fromTaskOutputPort.getPath());
//        link_from.save();

    final Node link_to = link.addNode("to", "jecars:workflowlinkendpoint");
    link_to.setProperty("jecars:endpoint", toTask.getPath());
//        link.save();
    final Node inPort = link_to.addNode(toTaskInputPort.getName(), "jecars:workflowtaskportref");
    inPort.setProperty("jecars:portref", toTaskInputPort.getPath());
//        link_to.save();
    return link;
  }

  /**
   * return child of parent - parent must have exactly 1 child
   *
   * @param parent
   * @return child of parent
   * @throws Exception if parent does not exactly 1 child
   */
  public static Node getUniqueChild(Node parent) throws Exception {
    NodeIterator it = parent.getNodes();
    Node result = null;
    if (it.hasNext()) {
      result = it.nextNode();
      if (it.hasNext()) {
        throw new Exception("Founr >1 child for parent=" + parent.getPath());
      }
    } else {
      throw new Exception("No child for task=" + parent.getPath());
    }
    return result;
  }

}
