/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.output;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_ActionContext;
import org.jecars.output.CARS_DefaultOutputGenerator;
import org.jecars.output.CARS_OutputGenerator;
import org.jecars.support.CARS_Buffer;
import org.jecars.tools.workflow.xml.WF_XmlHelper;
import org.jecars.tools.workflow.xml.WF_XmlWriter;

/**
 *
 * @author schulth
 */
public class CARS_OutputGenerator_WorkflowXML extends CARS_DefaultOutputGenerator implements CARS_OutputGenerator {
  /** Create the header of the message
   * @param pContext The action context
   * @param pMessage the string builder in which the header must be added
   */
  @Override
  public void createHeader( CARS_ActionContext pContext, final CARS_Buffer pMessage ) {
    pContext.setContentType( "application/xml" );
    return;
  }

  /** append XML presentation of pThisNode to pMessage
   * 
   * @param pContext
   * @param pMessage
   * @param pThisNode
   * @param pFromNode
   * @param pToNode
   * @throws RepositoryException
   * @throws Exception 
   */
    @Override
    public void addThisNodeEntry(CARS_ActionContext pContext, CARS_Buffer pMessage, Node pThisNode, long pFromNode, long pToNode) throws RepositoryException, Exception {
        WF_XmlWriter xmlWriter = new WF_XmlWriter();
        String xml = WF_XmlHelper.getXMLString(xmlWriter.getXmlDom(pThisNode));
        pMessage.append( xml);
    }

    @Override
    public boolean isFeed() {
        return false;
    }
    
    
}
