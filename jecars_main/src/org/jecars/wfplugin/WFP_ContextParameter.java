/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.wfplugin;

import java.math.BigDecimal;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import org.jecars.CARS_Utils;

/**
 *
 * @author weert
 */
public class WFP_ContextParameter extends WFP_Node implements IWFP_ContextParameter{

  private Node mLinkedNode;
    
  public WFP_ContextParameter( Node pNode ) throws RepositoryException {
    super(pNode);
    mLinkedNode = CARS_Utils.getLinkedNode( pNode );
  }

  @Override
  protected Node getNode() {
    return mLinkedNode;
  }  
  
  @Override
  public String getStringValue() throws WFP_Exception {
    try {
      final Value[] values = getNode().getProperty( "jecars:string" ).getValues();
      if (values.length>0) {
        return values[0].getString();
      }
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
    return null;
  }    

  @Override
  public void setValue( final String pValue ) throws WFP_Exception {
    try {
      mLinkedNode.setProperty( "jecars:string", (Value[])null );
      CARS_Utils.addMultiProperty( mLinkedNode, "jecars:string", pValue, true );
    } catch( Exception e ) {
      throw new WFP_Exception( e );
    }
    return;
  }  
  
}
