/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.tools.workflow;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.logging.Logger;
import javax.jcr.Node;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Main;
import org.jecars.CARS_RESTMethodHandled;
import org.jecars.apps.CARS_DefaultInterface;

/** WF_WorkflowThreadsCurrent
 *
 * @author weert
 */
public class WF_WorkflowThreadsCurrent extends CARS_DefaultInterface {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.tools.workflow" );

  
  /** WF_WorkflowThreadsCurrent
   * 
   */
  public WF_WorkflowThreadsCurrent() {
    return;
  }

  /** getNodes
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws CARS_RESTMethodHandled
   * @throws Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws CARS_RESTMethodHandled, Exception {
    {
      final CARS_ActionContext ac = pMain.getContext();
      ac.setErrorCode( HttpURLConnection.HTTP_OK );
      StringBuilder report = new StringBuilder( 128 );
      report.append( "InterfaceNode=WF_WorkflowThreadsCurrent\n" );
      final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
      ac.setContentsResultStream( bais, "text/plain" );
    }
    super.getNodes(pMain, pInterfaceNode, pParentNode, pLeaf);
    return;
  }



}
