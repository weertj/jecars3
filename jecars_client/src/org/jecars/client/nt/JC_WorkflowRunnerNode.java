/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.client.nt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;

/**
 *
 * @author weert
 */
public class JC_WorkflowRunnerNode extends JC_DefaultNode {
  
  /** getContext
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_datafolderNode getContext() throws JC_Exception {
    return (JC_datafolderNode)getNode( "context" ).morphToNodeType();
  }

  public List<JC_RunnerContextNode> getContexts() throws JC_Exception {
    final List<JC_RunnerContextNode> dfns = new ArrayList<JC_RunnerContextNode>();
    for( JC_Nodeable n : getNodes() ) {
      if (n.morphToNodeType() instanceof JC_RunnerContextNode) {
        dfns.add( (JC_RunnerContextNode)n.morphToNodeType() );
      }
    }
    return dfns;
  }
  
  public JC_WorkflowNode getWorkflow() throws JC_Exception {
    return (JC_WorkflowNode)getParent().getParent().morphToNodeType();
  }
  
  public void restart() throws JC_Exception {
    refresh();
    setProperty( "jecars:COMMAND", "RESTART" );
    save();
    return;
  }

  public void singleStep() throws JC_Exception {
//    if (hasProperty( "jecars:SingleStep" )) {
//      setProperty( "jecars:SingleStep", getProperty( "jecars:SingleStep" ).getValueAsLong() + 1 );
//    } else {
//      setProperty( "jecars:SingleStep", 1 );
//    }
    refresh();
    setProperty( "jecars:COMMAND", "SINGLESTEP" );
    save();
    return;
  }
  
  public JC_WorkflowTaskNode getCurrentTask( final JC_WorkflowNode pWorkflow ) throws JC_Exception {
    String ct = getProperty( "jecars:currentTask" ).getValueString();
    if ("".equals(ct)) {
      return JC_WorkflowTaskNode.NULL;
    }
    return pWorkflow.getTask( ct.substring( ct.lastIndexOf( '/' )+1 ) );
  }

  public JC_WorkflowLinkNode getCurrentLink( final JC_WorkflowNode pWorkflow ) throws JC_Exception {
    String ct = getProperty( "jecars:currentLink" ).getValueString();
    if ("".equals(ct)) {
      return JC_WorkflowLinkNode.NULL;
    }
    return pWorkflow.getLink( ct.substring( ct.lastIndexOf( '/' )+1 ) );
  }

  public String getStarted() throws JC_Exception {
    try {
      return getProperty( "jecars:Started" ).getValueString();
    } catch( JC_Exception je ) {
      return "";
    }
  }

  public String getEnded() throws JC_Exception {
    try {
      return getProperty( "jecars:Ended" ).getValueString();
    } catch( JC_Exception je ) {
      return "";
    }
  }

  public double getProgress() throws JC_Exception {
    return getProperty( "jecars:Progress" ).getValueAsDouble();
  }

  
    @Override
    public String toString() {
      return getName();
    }
  
  
  
}
