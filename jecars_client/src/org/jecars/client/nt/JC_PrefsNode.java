/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.nt;

import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Propertyable;

/**
 *
 * @author weert
 */
public class JC_PrefsNode extends JC_DefaultNode {

  /** doesPrefsPathExists
   * 
   * @param pPath
   * @return
   */
  public boolean doesPrefsPathExists( final String pPath ) {
    try {
      getNode( pPath );
    } catch( JC_Exception e ) {
      return false;
    }
    return true;
  }

  /** setPrefValue
   *
   * @param pPath
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  public void setPrefValue( final String pPath, final String pName,  final double pValue ) throws JC_Exception {
    setPrefValue( pPath, pName, String.valueOf( pValue ) );
    return;
  }

  /** setPrefValue
   * 
   * @param pPath
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  public void setPrefValue( final String pPath, final String pName,  final int pValue ) throws JC_Exception {
    setPrefValue( pPath, pName, String.valueOf( pValue ) );
    return;
  }

  /** setPrefValue
   *
   * @param pPath
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  public void setPrefValue( final String pPath, final String pName,  final String pValue ) throws JC_Exception {
    setPrefValue( this, pPath, pName, pValue );
    return;
  }

  /** setPrefValue
   * 
   * @param pPath
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  public void setPrefValue( final String pPath, final String pName,  final boolean pValue ) throws JC_Exception {
    setPrefValue( this, pPath, pName, String.valueOf( pValue ) );
    return;
  }


  /** setPrefValue
   *
   * @param pNode
   * @param pPath
   * @param pName
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  protected void setPrefValue( final JC_Nodeable pNode, final String pPath, final String pName, final String pValue ) throws JC_Exception {
    if (pPath.indexOf('/')==-1) {
      if (!pNode.hasNode( pPath )) {
        pNode.addNode( pPath, "jecars:unstructured" );
      }
      pNode.getNode( pPath ).setProperty( pName, pValue );
    } else {
      String base = pPath.substring( 0, pPath.indexOf( '/' ) );
      if (!pNode.hasNode( base )) {
        pNode.addNode( base, "jecars:unstructured" );
      }
      setPrefValue( pNode.getNode( base ), pPath.substring( pPath.indexOf( '/' )+1 ), pName, pValue );
    }
    return;
  }

  /** getPrefValue
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public String getPrefValue( final String pPath, final String pName, final boolean pUserPref ) throws JC_Exception {
    JC_Propertyable p = getPrefProperty( pPath, pName, pUserPref );
    if (p!=null) {
      return p.getValueString();
    }
    return null;
  }

  /** getPrefValue
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @param pDefault
   * @return
   * @throws JC_Exception
   */
  public String getPrefValue( final String pPath, final String pName, final boolean pUserPref, final String pDefault ) throws JC_Exception {
    try {
      return getPrefValue( pPath, pName, pUserPref );
    } catch( JC_Exception je ) {
      return pDefault;
    }
  }

  /** getPrefValueDouble
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public double getPrefValueDouble( final String pPath, final String pName, final boolean pUserPref ) throws JC_Exception {
    final JC_Propertyable p = getPrefProperty( pPath, pName, pUserPref );
    return (Double)p.getValueAs( Double.class );
  }

  /** getPrefValueDouble
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @param pDefault
   * @return
   * @throws JC_Exception
   */
  public double getPrefValueDouble( final String pPath, final String pName, final boolean pUserPref, final double pDefault ) throws JC_Exception {
    try {
      return getPrefValueDouble( pPath, pName, pUserPref );
    } catch( JC_Exception je ) {
      return pDefault;
    }
  }

  /** getPrefValueInteger
   * 
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public int getPrefValueInteger( final String pPath, final String pName, final boolean pUserPref ) throws JC_Exception {
    JC_Propertyable p = getPrefProperty( pPath, pName, pUserPref );
    return (Integer)p.getValueAs( Integer.class );
  }

  /** getPrefValueInteger
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public int getPrefValueInteger( final String pPath, final String pName, final boolean pUserPref, final int pDefault ) throws JC_Exception {
    try {
      JC_Propertyable p = getPrefProperty( pPath, pName, pUserPref );
      return (Integer)p.getValueAs( Integer.class );
    } catch( JC_Exception je ) {
      return pDefault;
    }
  }

  
  /** getPrefValueBoolean
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @param pDefault
   * @return
   * @throws JC_Exception
   */
  public boolean getPrefValueBoolean( final String pPath, final String pName, final boolean pUserPref, final boolean pDefault ) throws JC_Exception {
    try {
      return getPrefValueBoolean( pPath, pName, pUserPref );
    } catch( JC_Exception je ) {
      return pDefault;
    }
  }

  /** getPrefValueBoolean
   * 
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean getPrefValueBoolean( final String pPath, final String pName, final boolean pUserPref ) throws JC_Exception {
    JC_Propertyable p = getPrefProperty( pPath, pName, pUserPref );
    return (Boolean)p.getValueAs( Boolean.class );
  }


  /** getPrefProperty
   *
   * @param pPath
   * @param pName
   * @param pUserPref
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_Propertyable getPrefProperty( final String pPath, final String pName, final boolean pUserPref ) throws JC_Exception {
    JC_Nodeable prefs = getPrefNode( this,  pPath );
    if (prefs!=null) {
      return prefs.getProperty( pName );
    }
    return null;
  }

  /** getPrefNode
   *
   * @param pNode
   * @param pPath
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_Nodeable getPrefNode( final JC_Nodeable pNode, final String pPath ) throws JC_Exception {
    if (pPath.indexOf('/')==-1) {
      return pNode.getNode( pPath );
    } else {
      String base = pPath.substring( 0, pPath.indexOf( '/' ) );
      return getPrefNode( pNode.getNode( base ), pPath.substring( pPath.indexOf( '/' )+1 ) );
    }
  }

}
