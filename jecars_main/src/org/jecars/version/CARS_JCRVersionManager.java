/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.version;

import java.util.ArrayList;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;

/**
 * CARS_JCRVersionManager
 * 
 * @version $Id: CARS_JCRVersionManager.java,v 1.4 2009/06/23 22:39:24 weertj Exp $
 */
public class CARS_JCRVersionManager extends CARS_DefaultVersionManager {

  /** checkin the node
   * @param pNode node which will be checked in
   * @param pLabel optional label for the checked in version
   * @return CARS_Version the result of the checkin
   * @throws CARS_VersionException when an error occurs
   */
  @Override
  public CARS_Version checkin( final CARS_Main pMain, final Node pNode, final String pLabel ) throws CARS_VersionException {
    CARS_Version cv = null;
    try {
      if (!pNode.isNodeType( "mix:versionable" )) {
        pNode.addMixin( "mix:versionable" );
      }
      pNode.save();
      final Version v = pNode.checkin();
      if (pLabel!=null) {
        pNode.getVersionHistory().addVersionLabel( v.getName(), pLabel, false );
      }
      cv = new CARS_Version( v );
      CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pNode, null, "SYS", "CHECKIN",
              "Node " + pNode.getPath() + " checked in as " + v.getName() + " label = " + pLabel );
    } catch (Exception re) {
      CARS_Factory.getEventManager().addException( pMain, pMain.getLoginUser(), pNode, null, "SYS", "CHECKIN", re, null );
      throw new CARS_VersionException( re );
    }
    return cv;
  }

  /** checkout the node
   * @param pMain CARS_Main object
   * @param pNode node which will be checked out
   * @return The checkedout node
   * @throws CARS_VersionException when an error occurs
   */
  @Override
  public Node checkout( final CARS_Main pMain, final Node pNode ) throws CARS_VersionException {
    try {
      pNode.checkout();
      CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pNode, null, "SYS", "CHECKOUT",
              "Node " + pNode.getPath() + " checked out" );
    } catch (Exception re) {
      CARS_Factory.getEventManager().addException( pMain, pMain.getLoginUser(), pNode, null, "SYS", "CHECKOUT", re, null );
      throw new CARS_VersionException( re );
    }
    return pNode;
  }
  
  /** restore an version of a node
   * @param pMain CARS_Main object
   * @param pNode the node
   * @param pLabel node with this label will be restored
   * @return restored node
   * @throws CARS_VersionException when an error occurs
   */
  @Override
  public Node restore( final CARS_Main pMain, final Node pNode, final String pLabel ) throws CARS_VersionException {
    try {
      pNode.restoreByLabel( pLabel, true );      
      CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pNode, null, "SYS", "RESTORE",
              "Node " + pNode.getPath() + " restored by label " + pLabel );
    } catch (Exception re) {
      CARS_Factory.getEventManager().addException( pMain, pMain.getLoginUser(), pNode, null, "SYS", "RESTORE", re, null );
      throw new CARS_VersionException( re );
    }
    return pNode;
  }

  /** history
   * 
   * @param pNode
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public List<String> history( final Node pNode ) throws RepositoryException {
    final List<String> vlist = new ArrayList<String>();
    final VersionHistory vh = pNode.getVersionHistory();
    final String[] labels = vh.getVersionLabels();
    for( String label : labels) {
      vlist.add( label );
    }
    return vlist;
  }
  
  /** removeVersionByLabel
   * 
   * @param pLabel
   * @param pNode
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public void removeVersionByLabel( final String pLabel, final Node pNode ) throws RepositoryException {
    final VersionHistory vh = pNode.getVersionHistory();
    Version v = vh.getVersionByLabel( pLabel );
    if (v!=null) {
      vh.removeVersionLabel( pLabel );
      try {
        vh.removeVersion( v.getName() );
      } catch( ReferentialIntegrityException re ) {
      }
    }
    return;
  }


}
